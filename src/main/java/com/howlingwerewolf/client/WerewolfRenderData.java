package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;

/** Cosmetic and animation inputs captured on the extraction thread. */
record WerewolfRenderData(WerewolfForm form, WerewolfModelSet modelSet, String skinId, boolean preview,
                          boolean armoredInstinct, boolean sprinting, float beastRunAmount, float beastProwlAmount,
                          float beastLeapAmount, ItemStackRenderState mouthItem) {
    static final ContextKey<WerewolfRenderData> KEY = new ContextKey<>(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "werewolf_render"));
    private static final WerewolfRenderData EMPTY = new WerewolfRenderData(WerewolfForm.HUMAN,
            WerewolfModelSet.CLASSIC, WerewolfSkinIds.DEFAULT_ID, false, false, false, 0, 0, 0, new ItemStackRenderState());

    static WerewolfRenderData get(HumanoidRenderState state) {
        return state.getRenderDataOrDefault(KEY, EMPTY);
    }

    static WerewolfRenderData forEntity(LivingEntity entity) {
        return forEntity(entity, 1.0F);
    }

    static WerewolfRenderData forEntity(LivingEntity entity, float partialTick) {
        if (entity instanceof com.howlingwerewolf.entity.WerewolfNpcAppearance npc) {
            return new WerewolfRenderData(npc.getNpcForm(), npc.getNpcModelSet(), npc.getNpcSkinId(),
                    false, false, entity.isSprinting(), 0, 0, 0, new ItemStackRenderState());
        }
        if (entity instanceof AbstractClientPlayer player) {
            boolean preview = WerewolfSkinRenderContext.isPreview(player);
            var motion = preview ? new ModernBeastRunState.Sample(0, 0, 0) : ModernBeastRunState.sample(player, partialTick);
            return new WerewolfRenderData(WerewolfSkinRenderContext.getForm(player),
                    WerewolfSkinRenderContext.getModelSet(player), WerewolfSkinRenderContext.getSkinId(player), preview,
                    preview || WerewolfApi.get(player).map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT) && data.showsEquipment()).orElse(false),
                    player.isSprinting(), motion.run(), motion.prowl(), motion.leap(), new ItemStackRenderState());
        }
        return new WerewolfRenderData(WerewolfForm.HUMAN, WerewolfModelSet.CLASSIC, WerewolfSkinIds.DEFAULT_ID,
                false, false, entity.isSprinting(), 0, 0, 0, new ItemStackRenderState());
    }
}
