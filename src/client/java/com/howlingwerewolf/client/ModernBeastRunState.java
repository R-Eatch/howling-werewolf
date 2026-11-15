package com.howlingwerewolf.client;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.WerewolfModelPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.WeakHashMap;

/** Each observed player owns its short transitions; a shared renderer never owns a player's gait. */
final class ModernBeastRunState {
    private static final Map<AbstractClientPlayer, Motion> MOTIONS = new WeakHashMap<>();
    record Sample(float run, float prowl, float leap) {}

    static Sample sample(AbstractClientPlayer player, float partialTick) {
        boolean running = WerewolfApi.get(player).map(data -> WerewolfModelPose.isBeastSprint(player, data)).orElse(false);
        boolean prowling = WerewolfApi.get(player).map(data -> WerewolfModelPose.isBeastProwl(player, data)).orElse(false);
        Motion motion = MOTIONS.computeIfAbsent(player, ignored -> new Motion(player.tickCount));
        if (motion.tick != player.tickCount) {
            float elapsed = Math.clamp(player.tickCount - motion.tick, 1, 20);
            motion.run.advance(running, 0.2F * elapsed);
            motion.prowl.advance(prowling, 0.2F * elapsed);
            motion.leap.advance(running && !player.onGround(), 0.25F * elapsed);
            motion.tick = player.tickCount;
        }
        float blend = Mth.clamp(partialTick, 0, 1);
        return new Sample(motion.run.at(blend), motion.prowl.at(blend), motion.leap.at(blend));
    }

    private static final class Blend {
        float previous, amount;
        void advance(boolean active, float step) {
            previous = amount;
            amount = Mth.clamp(amount + (active ? step : -step), 0, 1);
        }
        float at(float partial) { return Mth.lerp(partial, previous, amount); }
    }
    private static final class Motion {
        int tick;
        final Blend run = new Blend(), prowl = new Blend(), leap = new Blend();
        Motion(int tick) { this.tick = tick; }
    }

    private ModernBeastRunState() {}
}
