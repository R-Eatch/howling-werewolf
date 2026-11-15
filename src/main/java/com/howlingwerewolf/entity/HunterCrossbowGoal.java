package com.howlingwerewolf.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.CrossbowItem;

import java.util.EnumSet;

/** A patrol hunter uses the natural crossbow charge and firing animation on either loader. */
final class HunterCrossbowGoal extends Goal {
    private static final double RANGE_SQUARED = 18 * 18;

    private final HunterEntity hunter;
    private int visibleTicks;
    private long fireAt;

    HunterCrossbowGoal(HunterEntity hunter) {
        this.hunter = hunter;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean canUse() {
        var target = hunter.getTarget();
        return hunter.holdsHunterCrossbow() && target != null && target.isAlive();
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }

    @Override public void stop() {
        hunter.getNavigation().stop();
        if (hunter.isUsingItem()) hunter.stopUsingItem();
        visibleTicks = 0;
    }

    @Override public void tick() {
        var target = hunter.getTarget();
        var stack = hunter.getMainHandItem();
        if (target == null || !(stack.getItem() instanceof CrossbowItem)) return;

        visibleTicks = hunter.getSensing().hasLineOfSight(target) ? visibleTicks + 1 : 0;
        boolean inRange = hunter.distanceToSqr(target) <= RANGE_SQUARED && visibleTicks >= 5;
        if (inRange) hunter.getNavigation().stop();
        else if (hunter.tickCount % 10 == 0) hunter.getNavigation().moveTo(target, hunter.isUsingItem() ? 0.55 : 1.0);
        hunter.getLookControl().setLookAt(target, 30, 30);

        long now = hunter.level().getGameTime();
        if (hunter.isUsingItem()) {
            if (hunter.getTicksUsingItem() >= CrossbowItem.getChargeDuration(stack)) {
                hunter.releaseUsingItem();
                fireAt = now + 10 + hunter.getRandom().nextInt(11);
            }
        } else if (inRange && now >= fireAt) {
            if (CrossbowItem.isCharged(stack)) {
                CrossbowItem.performShooting(hunter.level(), hunter, InteractionHand.MAIN_HAND, stack,
                        3.15F, 14 - hunter.level().getDifficulty().getId() * 4);
                fireAt = now + 10;
            } else {
                hunter.startUsingItem(InteractionHand.MAIN_HAND);
            }
        }
    }
}
