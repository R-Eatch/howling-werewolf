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
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

/** Cosmetic and animation inputs captured on the extraction thread. */
record WerewolfRenderData(WerewolfForm form, WerewolfModelSet modelSet, String skinId, boolean preview,
                          boolean modernPlayerModel, boolean armoredInstinct, boolean sprinting, float beastRunAmount, float beastProwlAmount,
                          float beastLeapAmount, ItemStackRenderState mouthItem, ClientPouncePose.Sample pounce) {
    private static final RenderStateDataKey<WerewolfRenderData> KEY = RenderStateDataKey.create(() -> "howlingwerewolf:werewolf_render");
    private static final WerewolfRenderData EMPTY = new WerewolfRenderData(WerewolfForm.HUMAN,
            WerewolfModelSet.CLASSIC, WerewolfSkinIds.DEFAULT_ID, false, false, false, false, 0, 0, 0, new ItemStackRenderState(), null);

    static WerewolfRenderData get(HumanoidRenderState state) {
        return ((FabricRenderState) state).getDataOrDefault(KEY, EMPTY);
    }

    static void set(HumanoidRenderState state, WerewolfRenderData data) {
        ((FabricRenderState) state).setData(KEY, data);
    }

    static HumanoidArm swingArm(HumanoidRenderState state) {
        return state.currentSwing == null ? state.mainArm : state.currentSwing.hand().asArm(state.mainArm);
    }

    static WerewolfRenderData forEntity(LivingEntity entity) {
        return forEntity(entity, 1.0F);
    }

    static WerewolfRenderData forEntity(LivingEntity entity, float partialTick) {
        if (entity instanceof com.howlingwerewolf.entity.WerewolfNpcAppearance npc) {
            return new WerewolfRenderData(npc.getNpcForm(), npc.getNpcModelSet(), npc.getNpcSkinId(),
                    false, false, false, entity.isSprinting(), 0, 0, 0, new ItemStackRenderState(), null);
        }
        if (entity instanceof AbstractClientPlayer player) {
            boolean preview = WerewolfSkinRenderContext.isPreview(player);
            var motion = preview ? new ModernBeastRunState.Sample(0, 0, 0) : ModernBeastRunState.sample(player, partialTick);
            return new WerewolfRenderData(WerewolfSkinRenderContext.getForm(player),
                    WerewolfSkinRenderContext.getModelSet(player), WerewolfSkinRenderContext.getSkinId(player), preview,
                    WerewolfSkinRenderContext.usesModernPlayerModel(player),
                    preview || WerewolfApi.get(player).map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT) && data.showsEquipment()).orElse(false),
                    player.isSprinting(), motion.run(), motion.prowl(), motion.leap(), new ItemStackRenderState(), preview ? null : ClientPouncePose.sample(player, partialTick));
        }
        return new WerewolfRenderData(WerewolfForm.HUMAN, WerewolfModelSet.CLASSIC, WerewolfSkinIds.DEFAULT_ID,
                false, false, false, entity.isSprinting(), 0, 0, 0, new ItemStackRenderState(), null);
    }
}
