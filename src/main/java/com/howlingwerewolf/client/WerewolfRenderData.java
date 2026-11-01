package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;

/** Cosmetic and animation inputs captured on the extraction thread. */
record WerewolfRenderData(WerewolfForm form, String skinId, boolean preview,
                          boolean armoredInstinct, boolean sprinting, ItemStackRenderState mouthItem) {
    static final ContextKey<WerewolfRenderData> KEY = new ContextKey<>(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "werewolf_render"));
    private static final WerewolfRenderData EMPTY = new WerewolfRenderData(WerewolfForm.HUMAN,
            WerewolfSkinIds.DEFAULT_ID, false, false, false, new ItemStackRenderState());

    static WerewolfRenderData get(HumanoidRenderState state) {
        return state.getRenderDataOrDefault(KEY, EMPTY);
    }

    static WerewolfRenderData forEntity(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayer player) {
            boolean preview = WerewolfSkinRenderContext.isPreview(player);
            return new WerewolfRenderData(WerewolfSkinRenderContext.getForm(player),
                    WerewolfSkinRenderContext.getSkinId(player), preview,
                    preview || WerewolfApi.get(player).map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT)).orElse(false),
                    player.isSprinting(), new ItemStackRenderState());
        }
        return new WerewolfRenderData(WerewolfForm.HUMAN, WerewolfSkinIds.DEFAULT_ID,
                false, false, entity.isSprinting(), new ItemStackRenderState());
    }
}
