/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The rhythm of an attack belongs to the form, so each observed entity keeps its own strike clock
 * beside the vanilla swing timer. A click during a strike waits for that strike's commit point,
 * which keeps fast clicking in time; the strike before it finishes settling underneath.
 */
final class WerewolfStrikeState {
    private static final Map<LivingEntity, Clock> CLOCKS = new WeakHashMap<>();
    /** A strike within this many ticks of the last one continues the combo. */
    private static final int CHAIN_GAP = 24;

    /** Times run 0 to 1 and are 0 when idle. {@code index} counts a chain from its first strike. */
    record Sample(float time, boolean leadRight, int index, boolean dig, float previousTime, boolean previousLeadRight, int previousIndex) {
        static final Sample NONE = new Sample(0, true, 0, false, 0, true, 0);
        boolean active() { return time > 0 || previousTime > 0; }
    }

    /** Length of a whole strike, in ticks. */
    static float duration(WerewolfForm form) {
        return switch (form) {
            case BEAST -> 13;
            case QUADRUPED -> 8;
            default -> 9;
        };
    }
    /** How far through a strike the next one may begin. */
    private static float commit(WerewolfForm form) {
        return switch (form) {
            case BEAST -> 0.62F;
            case QUADRUPED -> 0.6F;
            default -> 0.56F;
        };
    }

    static Sample sample(LivingEntity entity, WerewolfForm form, float partialTick) {
        Clock clock = CLOCKS.computeIfAbsent(entity, ignored -> new Clock());
        float now = entity.tickCount + Mth.clamp(partialTick, 0, 1);
        boolean restarted = entity.swinging && clock.swinging && entity.swingTime < clock.swingTime;
        boolean fresh = entity.swinging && !clock.swinging || restarted;
        // A held attack restarts the vanilla timer every few ticks: that is digging, not striking.
        if (fresh) clock.rapid = restarted ? clock.rapid + 1 : 0;
        clock.swinging = entity.swinging;
        clock.swingTime = entity.swingTime;
        float length = duration(form), elapsed = (now - clock.start) / length;
        if (fresh) {
            if (elapsed >= commit(form) || elapsed < 0) begin(clock, entity, now);
            else clock.queued = true;
        } else if (clock.queued && elapsed >= commit(form)) {
            clock.queued = false;
            begin(clock, entity, now);
        }
        return new Sample(progress(now, clock.start, length), clock.leadRight, clock.index, digging(entity, clock),
                progress(now, clock.previousStart, length), clock.previousLeadRight, clock.previousIndex);
    }

    private static void begin(Clock clock, LivingEntity entity, float now) {
        clock.previousStart = clock.start;
        clock.previousLeadRight = clock.leadRight;
        clock.previousIndex = clock.index;
        clock.index = now - clock.start <= CHAIN_GAP ? clock.index + 1 : 0;
        clock.start = now;
        HumanoidArm main = entity.getMainArm();
        // Two free claws trade the lead down a chain; a held item always leads from its own hand.
        boolean bothFree = entity.getMainHandItem().isEmpty() && entity.getOffhandItem().isEmpty();
        HumanoidArm lead = bothFree && clock.index % 2 == 1 ? main.getOpposite() : main;
        clock.leadRight = lead == HumanoidArm.RIGHT;
    }

    private static float progress(float now, float start, float length) {
        float value = (now - start) / length;
        return value > 0 && value < 1 ? value : 0;
    }

    private static boolean digging(LivingEntity entity, Clock clock) {
        Minecraft minecraft = Minecraft.getInstance();
        if (entity instanceof LocalPlayer && minecraft.gameMode != null) return minecraft.gameMode.isDestroying();
        return clock.rapid >= 2;
    }

    private static final class Clock {
        boolean swinging, queued, leadRight = true, previousLeadRight = true;
        int swingTime, rapid, index, previousIndex;
        float start = -1000, previousStart = -1000;
    }

    private WerewolfStrikeState() {}
}
