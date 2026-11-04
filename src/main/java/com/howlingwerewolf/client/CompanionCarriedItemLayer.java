/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

/** Shows the stack a companion is carrying between containers. */
final class CompanionCarriedItemLayer extends RenderLayer<HumanoidRenderState, EntityModel<HumanoidRenderState>> {
    CompanionCarriedItemLayer(RenderLayerParent<HumanoidRenderState, EntityModel<HumanoidRenderState>> parent) { super(parent); }

    @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light, HumanoidRenderState state, float yRot, float xRot) {
        if (state.rightHandItemState.isEmpty() || !(getParentModel() instanceof ArmedModel<?> armed)) return;
        pose.pushPose();
        @SuppressWarnings("unchecked") ArmedModel<HumanoidRenderState> hand = (ArmedModel<HumanoidRenderState>) armed;
        hand.translateToHand(state, HumanoidArm.RIGHT, pose);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate(1 / 16F, 2 / 16F, -10 / 16F);
        state.rightHandItemState.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}
