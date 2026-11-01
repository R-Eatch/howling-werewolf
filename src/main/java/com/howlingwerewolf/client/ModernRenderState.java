package com.howlingwerewolf.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

/** Frame-local inputs for the imported rig on the entity-based rendering API. */
final class ModernRenderState {
    float ageInTicks, walkAnimationPos, walkAnimationSpeed, xRot, attackTime, beastRunAmount, partialTick;
    boolean isCrouching, isPassenger, isInWater, isFallFlying, isVisuallySwimming, isAutoSpinAttack, isUsingItem;
    HumanoidArm attackArm;
    ItemStack leftHandItemStack, rightHandItemStack;

    static ModernRenderState sample(LivingEntity entity, float limb, float speed, float age, float pitch, float attack) {
        ModernRenderState state = new ModernRenderState();
        state.partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        state.ageInTicks = age;
        state.walkAnimationPos = limb;
        state.walkAnimationSpeed = speed;
        state.xRot = pitch;
        state.attackTime = attack;
        state.isCrouching = entity.isCrouching();
        state.isPassenger = entity.isPassenger();
        state.isInWater = entity.isInWater();
        state.isFallFlying = entity.isFallFlying();
        state.isVisuallySwimming = entity.isVisuallySwimming();
        state.isAutoSpinAttack = entity.isAutoSpinAttack();
        state.isUsingItem = entity.isUsingItem();
        state.attackArm = entity.swingingArm == InteractionHand.OFF_HAND ? entity.getMainArm().getOpposite() : entity.getMainArm();
        boolean right = entity.getMainArm() == HumanoidArm.RIGHT;
        state.rightHandItemStack = right ? entity.getMainHandItem() : entity.getOffhandItem();
        state.leftHandItemStack = right ? entity.getOffhandItem() : entity.getMainHandItem();
        state.beastRunAmount = entity instanceof AbstractClientPlayer player && !WerewolfSkinRenderContext.isPreview(player)
                ? ModernBeastRunState.sample(player, state.partialTick) : 0;
        return state;
    }
}
