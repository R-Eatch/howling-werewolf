package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.ability.PounceAbility;
import com.howlingwerewolf.network.PounceMotionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public final class ClientPouncePose {
    private record Phase(PounceAbility.Motion motion, long startedAt) {}
    private static final Map<LivingEntity, Phase> MOTION = new WeakHashMap<>();

    public static void receive(PounceMotionPacket packet) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null || !(level.getEntity(packet.entityId()) instanceof Player player)) return;
        // Vanilla's entity motion packet clips each component to 3.9 blocks per tick.
        if (packet.motion() == PounceAbility.Motion.FLIGHT && player == minecraft.player) {
            player.setDeltaMovement(packet.velocity());
        }
        if (packet.motion() == PounceAbility.Motion.CLEAR) MOTION.remove(player);
        else MOTION.put(player, new Phase(packet.motion(), level.getGameTime()));
    }

    static boolean active(LivingEntity entity) { return MOTION.containsKey(entity); }

    static void apply(LivingEntity entity, float age, WerewolfForm form, ModelPart center, ModelPart head,
                      ModelPart right, ModelPart left, ModelPart rightLeg, ModelPart leftLeg) {
        Phase phase = MOTION.get(entity);
        if (phase == null) return;
        float ticks = entity.level().getGameTime() - phase.startedAt() + (age - entity.tickCount);
        if (ticks < 0 || ticks > (phase.motion() == PounceAbility.Motion.LAND ? 8 : PounceAbility.MAX_FLIGHT_TICKS)) {
            MOTION.remove(entity);
            return;
        }
        float anticipation = phase.motion() == PounceAbility.Motion.WINDUP ? Mth.clamp((ticks + 1) / 3, 0, 1) : 0;
        float recovery = phase.motion() == PounceAbility.Motion.LAND ? 1 - smooth(ticks / 8) : 0;
        float launch = phase.motion() == PounceAbility.Motion.FLIGHT ? smooth(ticks / 2 + 0.25F) : 0;
        switch (form) {
            case WEREWOLF -> {
                center.xRot += 0.42F * anticipation + 0.34F * launch + 0.54F * recovery;
                head.xRot -= (0.26F * anticipation + 0.28F * launch + 0.25F * recovery);
                right.xRot = Mth.lerp(launch, right.xRot + 0.72F * anticipation, -1.86F);
                left.xRot = Mth.lerp(launch, left.xRot + 0.62F * anticipation, -1.51F);
                right.xRot -= 0.48F * recovery;
                rightLeg.xRot = Mth.lerp(launch, rightLeg.xRot - 0.50F * recovery, 0.64F);
                leftLeg.xRot = Mth.lerp(launch, leftLeg.xRot + 0.33F * recovery, 0.31F);
            }
            case QUADRUPED -> {
                center.y += 1.35F * anticipation + 1.0F * recovery;
                head.y += 0.80F * anticipation + 0.75F * recovery;
                center.xRot += 0.16F * launch + 0.18F * recovery;
                head.xRot -= 0.15F * anticipation + 0.20F * launch;
                right.xRot = Mth.lerp(launch, right.xRot + 0.38F * anticipation - 0.42F * recovery, -1.24F);
                left.xRot = Mth.lerp(launch, left.xRot + 0.38F * anticipation - 0.36F * recovery, -1.10F);
                rightLeg.xRot = Mth.lerp(launch, rightLeg.xRot - 0.39F * anticipation + 0.38F * recovery, 1.10F);
                leftLeg.xRot = Mth.lerp(launch, leftLeg.xRot - 0.39F * anticipation + 0.38F * recovery, 0.98F);
            }
            case BEAST -> {
                float strike = launch * smooth((ticks - 6) / 10);
                center.xRot += 0.19F * anticipation - 0.16F * launch + 0.38F * strike + 0.40F * recovery;
                head.xRot -= 0.13F * anticipation + 0.23F * strike + 0.25F * recovery;
                right.xRot = Mth.lerp(launch, right.xRot + 0.46F * anticipation, Mth.lerp(strike, -2.82F, -0.83F));
                left.xRot = Mth.lerp(launch, left.xRot + 0.46F * anticipation, Mth.lerp(strike, -2.94F, -0.83F));
                right.xRot -= 0.90F * recovery;
                left.xRot -= 0.90F * recovery;
                rightLeg.xRot = Mth.lerp(launch, -0.36F * recovery, 0.55F);
                leftLeg.xRot = Mth.lerp(launch, -0.30F * recovery, 0.49F);
            }
            default -> { }
        }
    }

    private static float smooth(float value) {
        float x = Mth.clamp(value, 0, 1);
        return x * x * (3 - 2 * x);
    }

    private ClientPouncePose() {}
}
