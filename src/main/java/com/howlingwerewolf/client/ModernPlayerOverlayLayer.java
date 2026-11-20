/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

/** Draws accessories using the poses already supplied by the player's renderer. */
public final class ModernPlayerOverlayLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public ModernPlayerOverlayLayer(AvatarRenderer<?> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yaw, float pitch) {
        var data = WerewolfRenderData.get(state);
        if (state.isInvisible || !data.modernPlayerModel()) return;
        var parts = ModernModelResources.playerParts();
        if (parts == null) return;
        var model = getParentModel();
        Identifier texture = WerewolfSkinCatalog.texture(data.skinId(), WerewolfModelSet.MODERN, WerewolfForm.WEREWOLF);
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
        draw(model.head, parts.head(), pose, collector, texture, light, overlay);
        float strength = state.isCrouching ? 0.55F : 1.0F;
        parts.tail().resetPose();
        parts.tailTip().resetPose();
        ModernPlayerModel.swayTail(parts.tail(), 1, state.ageInTicks, state.walkAnimationPos, state.walkAnimationSpeed, strength);
        ModernPlayerModel.swayTail(parts.tailTip(), 2, state.ageInTicks, state.walkAnimationPos, state.walkAnimationSpeed, strength);
        draw(model.body, parts.tail(), pose, collector, texture, light, overlay);
        draw(model.rightArm, parts.rightArm(), pose, collector, texture, light, overlay);
        draw(model.leftArm, parts.leftArm(), pose, collector, texture, light, overlay);
    }

    public static void renderArm(PoseStack pose, SubmitNodeCollector collector, int light, AbstractClientPlayer player,
                                 ModelPart arm, HumanoidArm side) {
        if (player == null || player.isInvisible() || !WerewolfSkinRenderContext.usesModernPlayerModel(player)) return;
        var parts = ModernModelResources.playerParts();
        if (parts == null) return;
        Identifier texture = WerewolfSkinCatalog.texture(WerewolfSkinRenderContext.getSkinId(player),
                WerewolfModelSet.MODERN, WerewolfForm.WEREWOLF);
        draw(arm, parts.arm(side), pose, collector, texture, light, OverlayTexture.NO_OVERLAY);
    }

    private static void draw(ModelPart parent, ModelPart part, PoseStack pose, SubmitNodeCollector collector,
                             Identifier texture, int light, int overlay) {
        if (!parent.visible) return;
        pose.pushPose();
        try {
            parent.translateAndRotate(pose);
            collector.submitModelPart(part, pose, RenderTypes.entityCutout(texture), light, overlay, null);
        } finally {
            pose.popPose();
        }
    }
}
