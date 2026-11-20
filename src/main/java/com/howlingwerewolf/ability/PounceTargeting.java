package com.howlingwerewolf.ability;

import com.howlingwerewolf.WerewolfForm;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PounceTargeting {
    private static final double MIN_TRAVEL = 0.85;
    private static final double MAX_DROP = 12;
    private static final float HORIZONTAL_DRAG = 0.91F;
    private static final float VERTICAL_DRAG = 0.98F;

    public static double range(WerewolfForm form) {
        return switch (form) {
            case QUADRUPED -> 30;
            case BEAST -> 27;
            default -> 25;
        };
    }

    public static double impactRadius(WerewolfForm form) {
        return switch (form) {
            case QUADRUPED -> 2.84;
            case BEAST -> 4.0;
            default -> 3.0;
        };
    }

    private static double lift(WerewolfForm form) {
        return switch (form) {
            case QUADRUPED -> 0.756;
            case BEAST -> 1.303;
            default -> 0.999;
        };
    }

    @Nullable
    public static Vec3 aim(Player player, WerewolfForm form) {
        Vec3 view = player.getViewVector(1);
        Vec3 eyes = player.getEyePosition();
        double limit = range(form);
        var hit = player.level().clip(new ClipContext(eyes, eyes.add(view.scale(limit + MAX_DROP)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 offset = hit.getLocation().subtract(player.position()).multiply(1, 0, 1);
        boolean hasHit = hit.getType() == HitResult.Type.BLOCK;
        if (hasHit && hit.getDirection() == Direction.UP && offset.lengthSqr() <= limit * limit) return hit.getLocation();

        double distance = Math.min(offset.length(), limit);
        if (hasHit && hit.getDirection().getAxis().isHorizontal()) distance = Math.max(0, distance - 0.4);
        if (offset.lengthSqr() < 0.001) return null;
        Vec3 column = player.position().add(offset.normalize().scale(distance));
        double highest = switch (form) {
            case QUADRUPED -> 4;
            case BEAST -> 10;
            default -> 6;
        };
        return floor(player, column.add(0, highest + 0.25, 0), column.subtract(0, MAX_DROP, 0));
    }

    @Nullable
    public static Vec3 launchVelocity(Player player, WerewolfForm form, Vec3 target) {
        if (!Double.isFinite(target.x) || !Double.isFinite(target.y) || !Double.isFinite(target.z)) return null;
        Vec3 start = player.position();
        Vec3 delta = target.subtract(start);
        double distance = delta.horizontalDistance();
        if (distance < MIN_TRAVEL || distance > range(form) + 0.02 || delta.y < -MAX_DROP || delta.y > 10) return null;
        if (!player.level().getWorldBorder().isWithinBounds(BlockPos.containing(target))) return null;
        if (!player.level().hasChunkAt(BlockPos.containing(target))) return null;
        Vec3 support = floor(player, target.add(0, 0.25, 0), target.subtract(0, 0.25, 0));
        if (support == null || Math.abs(support.y - target.y) > 0.05
                || !player.level().getFluidState(BlockPos.containing(support.add(0, 0.05, 0))).isEmpty()) return null;

        double initialLift = lift(form);
        double speedY = initialLift;
        double height = 0;
        double speedScale = 1;
        double travelScale = 0;
        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        boolean slowFalling = player.hasEffect(MobEffects.SLOW_FALLING);
        double[] heights = new double[PounceAbility.MAX_FLIGHT_TICKS];
        for (int step = 0; step < heights.length; step++) {
            height += speedY;
            travelScale += speedScale;
            heights[step] = height;
            if (speedY < 0 && height <= delta.y && step != 0) {
                if (heights[step - 1] <= delta.y) return null;
                return clearTrajectory(player, start, support, heights, step, travelScale)
                        ? new Vec3(delta.x / travelScale, initialLift, delta.z / travelScale) : null;
            }
            speedY = (speedY - (speedY <= 0 && slowFalling ? Math.min(gravity, 0.01) : gravity)) * VERTICAL_DRAG;
            speedScale *= HORIZONTAL_DRAG;
        }
        return null;
    }

    private static boolean clearTrajectory(Player player, Vec3 start, Vec3 end, double[] heights, int last, double total) {
        Vec3 previous = start;
        double scale = 0;
        double stepScale = 1;
        for (int step = 0; step <= last; step++) {
            scale += stepScale;
            double fraction = scale / total;
            Vec3 point = step == last ? end : new Vec3(
                    start.x + (end.x - start.x) * fraction, start.y + heights[step], start.z + (end.z - start.z) * fraction);
            int samples = Math.max(1, (int) Math.ceil(previous.distanceTo(point) / 0.45));
            for (int i = 1; i <= samples; i++) {
                Vec3 sample = previous.lerp(point, (double) i / samples);
                if (!player.level().hasChunkAt(BlockPos.containing(sample))) return false;
                var box = player.getBoundingBox().move(sample.subtract(start)).deflate(1.0E-5);
                if (player.level().getBlockCollisions(player, box).iterator().hasNext()) return false;
            }
            previous = point;
            stepScale *= HORIZONTAL_DRAG;
        }
        return true;
    }

    @Nullable
    private static Vec3 floor(Player player, Vec3 from, Vec3 to) {
        var hit = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.UP ? hit.getLocation() : null;
    }

    private PounceTargeting() {}
}
