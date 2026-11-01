package com.howlingwerewolf.client;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.WerewolfModelPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.WeakHashMap;

/** Each observed player owns a short transition; a shared renderer never owns a player's gait. */
final class ModernBeastRunState {
    private static final Map<AbstractClientPlayer, Transition> TRANSITIONS = new WeakHashMap<>();

    static float sample(AbstractClientPlayer player, float partialTick) {
        boolean running = WerewolfApi.get(player).map(data -> WerewolfModelPose.isBeastSprint(player, data)).orElse(false);
        Transition motion = TRANSITIONS.computeIfAbsent(player, ignored -> new Transition(player.tickCount));
        if (motion.tick != player.tickCount) {
            int elapsed = Math.clamp(player.tickCount - motion.tick, 1, 20);
            motion.previous = motion.amount;
            motion.amount = Mth.clamp(motion.amount + (running ? 0.2F : -0.2F) * elapsed, 0, 1);
            motion.tick = player.tickCount;
        }
        return Mth.lerp(Mth.clamp(partialTick, 0, 1), motion.previous, motion.amount);
    }

    private static final class Transition {
        int tick;
        float previous;
        float amount;
        Transition(int tick) { this.tick = tick; }
    }

    private ModernBeastRunState() {}
}
