package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Holds the main-hand item between the quadruped's jaws. */
public final class QuadrupedHeldItemLayer extends RenderLayer<AvatarRenderState, QuadrupedWerewolfModel> {
    public QuadrupedHeldItemLayer(RenderLayerParent<AvatarRenderState, QuadrupedWerewolfModel> parent) { super(parent); }
    @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light,
                                 AvatarRenderState state, float yRot, float xRot) {
        var item = WerewolfRenderData.get(state).mouthItem();
        if (item.isEmpty()) return;
        pose.pushPose();
        getParentModel().translateToMouth(pose);
        pose.translate(0.0D, 0.02D, -0.38D);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(0.72F, 0.72F, 0.72F);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}
