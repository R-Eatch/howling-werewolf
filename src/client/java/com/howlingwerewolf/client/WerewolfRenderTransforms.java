package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Shared whole-body transforms that a PlayerRenderer normally supplies. */
final class WerewolfRenderTransforms {
    static void applyPlayerTravelPose(AbstractClientPlayer player, PoseStack poseStack, float partialTick) {
        applyPlayerTravelPose(player, poseStack, partialTick, true);
    }

    static void applyQuadrupedTravelPose(AbstractClientPlayer player, PoseStack poseStack,
                                         float partialTick) {
        // A quadruped already fits its 0.85-block-tall collision box. Vanilla still marks a player
        // forced through a one-block gap as visually swimming; applying that land-crawl rotation
        // would tip the already-horizontal wolf onto an incorrect second horizontal axis.
        applyPlayerTravelPose(player, poseStack, partialTick, player.isInWater());
    }

    private static void applyPlayerTravelPose(AbstractClientPlayer player, PoseStack poseStack,
                                              float partialTick, boolean applySwimmingPose) {
        if (player.getFallFlyingTicks() > 4) {
            float flyingTicks = player.getFallFlyingTicks() + partialTick;
            float blend = Mth.clamp((flyingTicks * flyingTicks) / 100.0F, 0.0F, 1.0F);
            if (!player.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(blend * (-90.0F - player.getXRot())));
            }

            Vec3 view = player.getViewVector(partialTick);
            Vec3 movement = player.getDeltaMovementLerped(partialTick);
            double movementHorizontal = movement.horizontalDistanceSqr();
            double viewHorizontal = view.horizontalDistanceSqr();
            if (movementHorizontal > 1.0E-7D && viewHorizontal > 1.0E-7D) {
                double dot = (movement.x * view.x + movement.z * view.z)
                        / Math.sqrt(movementHorizontal * viewHorizontal);
                dot = Mth.clamp(dot, -1.0D, 1.0D);
                double cross = movement.x * view.z - movement.z * view.x;
                poseStack.mulPose(Axis.YP.rotation((float) (Math.signum(cross) * Math.acos(dot))));
            }
            return;
        }

        if (!applySwimmingPose) return;

        float swimAmount = player.getSwimAmount(partialTick);
        if (swimAmount <= 0.0F) return;

        boolean activeSwimming = player.isVisuallySwimming();
        float targetPitch = activeSwimming ? -90.0F - player.getXRot() : -90.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(swimAmount, 0.0F, targetPitch)));
        if (activeSwimming) {
            // Bring the custom model down and forward to the same visual lane as a vanilla player.
            poseStack.translate(0.0D, -1.0D, 0.30D);
        }
    }

    private WerewolfRenderTransforms() {}
}
