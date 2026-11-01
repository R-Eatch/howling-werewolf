package com.howlingwerewolf.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Alternates pressure and evasive footwork instead of blindly holding forward. */
final class AlphaCombatGoal extends Goal {
    private static final int ATTACK_INTERVAL_TICKS = 40;
    private static final int PROWL_DURATION_TICKS = 120;
    private static final int PROWL_SPEED_DURATION_TICKS = 60;
    private final AlphaWerewolfEntity alpha;
    private int attackCooldown;
    private int retreatTicks;

    AlphaCombatGoal(AlphaWerewolfEntity alpha) {
        this.alpha = alpha;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = alpha.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        // Update steering every game tick. Explicit 40/120-tick timers preserve the old Goal's
        // two-second attacks and six-second prowl when it updated every other game tick.
        // 每游戏刻更新移动；旧版 Goal 隔刻执行，现用 40/120 游戏刻保留 2 秒攻击间隔和 6 秒游猎。
        return true;
    }

    @Override
    public void stop() {
        alpha.getNavigation().stop();
        retreatTicks = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = alpha.getTarget();
        if (target == null || alpha.isSummoning()) return;
        if (attackCooldown > 0) attackCooldown--;
        if (alpha.hasRevived()) retreatTicks = 0;
        alpha.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distanceSqr = alpha.distanceToSqr(target);
        if (retreatTicks > 0) {
            retreatTicks--;
            Vec3 away = alpha.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() < 0.01D) {
                away = new Vec3(Mth.cos(alpha.getYRot() * Mth.DEG_TO_RAD), 0.0D,
                        Mth.sin(alpha.getYRot() * Mth.DEG_TO_RAD));
            }
            away = away.normalize();
            double facingYaw = Mth.atan2(target.getZ() - alpha.getZ(),
                    target.getX() - alpha.getX()) * Mth.RAD_TO_DEG - 90.0D;
            alpha.getNavigation().stop();
            alpha.setYRot((float)facingYaw);
            alpha.setYBodyRot((float)facingYaw);
            alpha.setYHeadRot((float)facingYaw);
            alpha.getLookControl().setLookAt(target, 180.0F, 180.0F);
            alpha.tryTraversalJump(away);
            // Backpedal relative to the target-facing yaw instead of turning around to flee.
            // 始终面向玩家，并以负向前进输入后撤，而不是转身逃跑。
            alpha.getMoveControl().strafe(-1.0F, 0.0F);
            return;
        }

        // Add two full blocks to the original linear melee reach. / 在原有线性近战距离上增加整整两格。
        double linearReach = alpha.getBbWidth() * 2.0F + 2.0D;
        double reach = linearReach * linearReach
                + target.getBbWidth();
        if (distanceSqr <= reach && attackCooldown <= 0 && alpha.hasLineOfSight(target)) {
            alpha.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            // Before revival, a successful melee hit has a 25% chance to start a six-second prowl.
            // Speed III lasts only the first three seconds, matching the old potion duration.
            // 复活前成功近战后有 25% 概率游猎后撤 6 秒；仅前 3 秒附带迅捷 III，与旧版实际时长一致。
            if (alpha.doHurtTarget((net.minecraft.server.level.ServerLevel) alpha.level(), target) && !alpha.hasRevived()
                    && alpha.getRandom().nextFloat() < 0.25F) {
                retreatTicks = PROWL_DURATION_TICKS;
                alpha.addEffect(new MobEffectInstance(MobEffects.SPEED,
                        PROWL_SPEED_DURATION_TICKS, 2, true, true, true));
            }
            // Forty game ticks between melee attempts at 20 TPS; summoning pauses this timer.
            // 20 TPS 时近战尝试间隔为 40 游戏刻（2 秒）；召唤吟唱期间暂停计时。
            attackCooldown = ATTACK_INTERVAL_TICKS;
        } else {
            alpha.tryTraversalJump(target.position().subtract(alpha.position()));
            alpha.getNavigation().moveTo(target, 1.12D);
        }
    }
}
