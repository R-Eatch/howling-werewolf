package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;

/** Shared whole-body transforms normally supplied by AvatarRenderer. */
final class WerewolfRenderTransforms {
    static void applyPlayerTravelPose(AvatarRenderState state, PoseStack pose) {
        applyTravelPose(state, pose, true);
    }
    static void applyQuadrupedTravelPose(AvatarRenderState state, PoseStack pose) {
        applyTravelPose(state, pose, state.isInWater);
    }
    private static void applyTravelPose(AvatarRenderState state, PoseStack pose, boolean swimming) {
        if (state.isFallFlying) {
            if (!state.isAutoSpinAttack) {
                pose.mulPose(Axis.XP.rotationDegrees(state.fallFlyingScale() * (-90.0F - state.xRot)));
            }
            if (state.shouldApplyFlyingYRot) pose.mulPose(Axis.YP.rotation(state.flyingYRot));
        } else if (swimming && state.swimAmount > 0.0F) {
            float pitch = state.isVisuallySwimming ? -90.0F - state.xRot : -90.0F;
            pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(state.swimAmount, 0.0F, pitch)));
            if (state.isVisuallySwimming) pose.translate(0.0D, -1.0D, 0.30D);
        }
    }
    private WerewolfRenderTransforms() {}
}
