package com.howlingwerewolf.entity;

import com.howlingwerewolf.world.SettlementPositions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Supplier;

/** Daytime work and visits, with an assigned bed per tribe member. */
final class SettlementRoutineGoal extends Goal {
    private final PathfinderMob mob;
    private final Supplier<SettlementPositions.Routine> routine;
    private BlockPos destination;
    private BlockPos bedside;
    private boolean goingToBed;
    private Path path;

    SettlementRoutineGoal(PathfinderMob mob, Supplier<SettlementPositions.Routine> routine) {
        this.mob = mob;
        this.routine = routine;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override public boolean canUse() {
        var route = routine.get();
        if (route == null || mob.isSleeping() || busy() || (mob.tickCount + mob.getId()) % 20 != 0) return false;
        goingToBed = !SettlementPositions.isDay(mob.level());
        if (goingToBed) {
            destination = route.home();
            bedside = route.bedside();
            if (!usableBed()) return false;
            if (nearBed()) return true;
            // The template marks a clear standing tile inside the room next to the bed.
            path = mob.getNavigation().createPath(bedside, 0, 48);
            return path != null && path.getNodeCount() > 1;
        }
        if (mob.getRandom().nextInt(3) != 0) return false;
        destination = switch (mob.getRandom().nextInt(5)) {
            case 0 -> route.ritual();
            case 1, 2 -> route.work();
            default -> route.plaza();
        };
        if (destination == null || bedside == null || !mob.level().hasChunkAt(destination)) return false;
        if (destination.equals(route.plaza()) || destination.equals(route.ritual())) {
            Vec3 center = Vec3.atBottomCenterOf(destination);
            Vec3 offset = LandRandomPos.getPosTowards(mob, 5, 2, center);
            if (offset != null && offset.distanceToSqr(center) <= 36) destination = BlockPos.containing(offset);
        }
        path = null;
        return mob.distanceToSqr(Vec3.atBottomCenterOf(destination)) > 2.25;
    }

    private boolean busy() { return mob.getTarget() != null && mob.getTarget().isAlive(); }

    private boolean usableBed() {
        if (destination == null || !mob.level().hasChunkAt(destination)) return false;
        var state = mob.level().getBlockState(destination);
        return state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD
                && !state.getValue(BedBlock.OCCUPIED);
    }

    @Override public void start() {
        if (goingToBed && nearBed()) tick();
        else if (goingToBed) mob.getNavigation().moveTo(path, 1.05);
        else move();
    }

    private void move() {
        Vec3 target = Vec3.atBottomCenterOf(destination);
        mob.getNavigation().moveTo(target.x, target.y, target.z, 0.95);
    }

    @Override public boolean canContinueToUse() {
        return !busy() && !mob.isSleeping() && (goingToBed
                ? !SettlementPositions.isDay(mob.level()) && usableBed() && (!mob.getNavigation().isDone() || nearBed())
                : SettlementPositions.isDay(mob.level()) && !mob.getNavigation().isDone());
    }

    private boolean nearBed() { return mob.distanceToSqr(Vec3.atBottomCenterOf(bedside)) <= 1.0; }

    @Override public void tick() {
        if (goingToBed && nearBed()) {
            mob.getNavigation().stop();
            mob.startSleeping(destination);
        }
    }

    @Override public void stop() {
        mob.getNavigation().stop();
        path = null;
    }

    static boolean wakeForActivity(PathfinderMob mob) {
        if (!mob.isSleeping()) return false;
        if (SettlementPositions.isDay(mob.level()) || mob.getTarget() != null || mob.getSleepingPos().filter(mob.level()::hasChunkAt)
                .map(pos -> !(mob.level().getBlockState(pos).getBlock() instanceof BedBlock)).orElse(true)) {
            mob.stopSleeping();
        } else {
            mob.getNavigation().stop();
        }
        return true;
    }
}
