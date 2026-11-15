package com.howlingwerewolf.entity;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.content.PeltArmor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side settlement speech. Contextual triggers share a cooldown, and only the closest visitor hears ambient lines. */
final class SettlementDialogue {
    private final SettlementLeaderEntity leader;
    private final Map<UUID, Long> recentTrades = new HashMap<>();
    private long nextAmbient;
    private long nextCombat;
    long nextHealShout;
    private long fullMoonDay = -1;

    SettlementDialogue(SettlementLeaderEntity leader) { this.leader = leader; }

    void greet(ServerPlayer player) {
        if (!allowTradeSpeech(player, 60)) return;
        var data = WerewolfApi.get(player).orElseThrow();
        String context;
        int alternatives = 2;
        if (leader.isWarden()) {
            if (data.isInfected() || data.isWerewolf() && !data.isTransformed()) context = "infected";
            else if (PeltArmor.wearingSet(player)) context = "hunter_gear";
            else { context = "human"; alternatives = 3; }
        } else {
            context = switch (data.getForm()) {
                case BEAST -> "alpha";
                case QUADRUPED -> "quadruped";
                case WEREWOLF -> data.hasDefeatedAlpha() ? "alpha" : "werewolf";
                default -> "human";
            };
        }
        say(player, "greet." + context + "." + leader.getRandom().nextInt(alternatives));
        say(player, "trade.open");
    }

    void traded(ServerPlayer player) {
        if (allowTradeSpeech(player, 100)) say(player, "trade.done");
    }

    private boolean allowTradeSpeech(ServerPlayer player, long cooldown) {
        long now = leader.level().getGameTime();
        recentTrades.values().removeIf(until -> until <= now);
        return recentTrades.putIfAbsent(player.getUUID(), now + cooldown) == null;
    }

    void fighting(LivingEntity target) {
        if (leader.level().getGameTime() < nextCombat) return;
        ServerPlayer player = target instanceof ServerPlayer recipient ? recipient : nearestPlayer(16);
        if (player == null) return;
        String cue = leader.isWarden() ? "combat.spot_werewolf." + leader.getRandom().nextInt(2) : "combat.defend.0";
        if (leader.isWarden() && !HunterEntity.isWerewolfTarget(target)) return;
        nextCombat = leader.level().getGameTime() + 300;
        say(player, cue);
    }

    void defendPlayer(ServerPlayer player) {
        if (leader.level().getGameTime() < nextCombat) return;
        nextCombat = leader.level().getGameTime() + 300;
        say(player, "combat.defend_player");
    }

    void healSquad() {
        long now = leader.level().getGameTime();
        if (now < nextHealShout || now < nextCombat) return;
        ServerPlayer player = nearestPlayer(14);
        if (player == null) return;
        nextHealShout = now + 20 * 60;
        nextCombat = now + 200;
        say(player, "combat.heal_squad");
    }

    void idle() {
        long now = leader.level().getGameTime();
        if (leader.isTrading() || now < nextAmbient) return;
        if (!leader.isWarden() && (!leader.hasSettlement() || !leader.isWithinRestriction())) return;
        if (nextAmbient == 0) {
            nextAmbient = now + 1200 + leader.getRandom().nextInt(1201);
            return;
        }
        long day = Math.floorDiv(leader.level().getDayTime(), 24000);
        boolean fullMoon = leader.level().getMoonPhase() == 0;
        if (fullMoon && (!leader.level().isNight() || fullMoonDay == day)) return;
        ServerPlayer player = nearestPlayer(10);
        if (player == null) return;
        nextAmbient = now + 2400 + leader.getRandom().nextInt(1201);
        if (fullMoon) fullMoonDay = day;
        say(player, fullMoon ? "ambient.full_moon" : "ambient.idle." + leader.getRandom().nextInt(4));
    }

    private ServerPlayer nearestPlayer(double distance) {
        return leader.level().getNearestPlayer(leader.getX(), leader.getY(), leader.getZ(), distance,
                player -> player.isAlive() && !player.isSpectator()) instanceof ServerPlayer player ? player : null;
    }

    private void say(ServerPlayer player, String key) {
        String faction = leader.isWarden() ? "warden" : "chief";
        player.sendSystemMessage(Component.literal("<").append(leader.getName()).append("> ")
                .append(Component.translatable("dialogue.howlingwerewolf." + faction + "." + key)));
    }
}
