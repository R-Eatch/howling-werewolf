/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

/** Additional geometry aligned to the pose already calculated by the player renderer. */
public final class ModernPlayerOverlayLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    ModernPlayerOverlayLayer(PlayerRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbAmount, float partialTick, float age, float yaw, float pitch) {
        if (player.isInvisible() || !WerewolfSkinRenderContext.usesModernPlayerModel(player)) return;
        var parts = ModernModelResources.playerParts();
        if (parts == null) return;
        var model = getParentModel();
        VertexConsumer coat = coatBuffer(player, buffers);
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0);
        draw(model.head, parts.head(), pose, coat, light, overlay);
        float tailStrength = player.isCrouching() ? 0.55F : 1.0F;
        parts.tail().resetPose();
        parts.tailTip().resetPose();
        ModernPlayerModel.swayTail(parts.tail(), 1, age, limbSwing, limbAmount, tailStrength);
        ModernPlayerModel.swayTail(parts.tailTip(), 2, age, limbSwing, limbAmount, tailStrength);
        draw(model.body, parts.tail(), pose, coat, light, overlay);
        draw(model.rightArm, parts.rightArm(), pose, coat, light, overlay);
        draw(model.leftArm, parts.leftArm(), pose, coat, light, overlay);
    }

    public static void renderArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                 ModelPart arm, HumanoidArm side) {
        if (player.isInvisible() || !WerewolfSkinRenderContext.usesModernPlayerModel(player)) return;
        var parts = ModernModelResources.playerParts();
        if (parts != null) draw(arm, parts.arm(side), pose, coatBuffer(player, buffers), light, OverlayTexture.NO_OVERLAY);
    }

    private static VertexConsumer coatBuffer(AbstractClientPlayer player, MultiBufferSource buffers) {
        var texture = WerewolfSkinCatalog.texture(WerewolfSkinRenderContext.getSkinId(player),
                WerewolfModelSet.MODERN, WerewolfForm.WEREWOLF);
        return buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
    }

    private static void draw(ModelPart parent, ModelPart part, PoseStack pose, VertexConsumer coat, int light, int overlay) {
        if (!parent.visible) return;
        pose.pushPose();
        try {
            parent.translateAndRotate(pose);
            part.render(pose, coat, light, overlay);
        } finally {
            pose.popPose();
        }
    }
}
