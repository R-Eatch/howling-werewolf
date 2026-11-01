package com.howlingwerewolf;

import com.howlingwerewolf.capability.WerewolfData;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

/** Common pose rules keep the observer, first-person camera and server eye origin in agreement. */
public final class WerewolfModelPose {
    public static boolean isBeastSprint(Player player, WerewolfData data) {
        return data.getModelSet() == WerewolfModelSet.MODERN && data.getForm() == WerewolfForm.BEAST
                && player.isSprinting() && !player.isCrouching() && !player.isPassenger()
                && !player.isInWater() && !player.isVisuallySwimming() && !player.isFallFlying()
                && !player.isAutoSpinAttack() && !player.isUsingItem();
    }

    public static float eyeHeight(WerewolfForm form, Pose pose, boolean sprinting) {
        if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK) return 0.4F;
        if (pose == Pose.SLEEPING) return 0.2F;
        if (form == WerewolfForm.QUADRUPED) return 0.6F;
        if (form == WerewolfForm.BEAST) return pose == Pose.CROUCHING ? 1.8F : sprinting ? 1.45F : 2.55F;
        return pose == Pose.CROUCHING ? 1.27F : 1.62F;
    }

    private WerewolfModelPose() {}
}
