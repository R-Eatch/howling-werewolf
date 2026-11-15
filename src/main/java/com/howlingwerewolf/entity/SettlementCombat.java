package com.howlingwerewolf.entity;

import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class SettlementCombat {
    public static boolean isHunter(LivingEntity entity) {
        return entity instanceof HunterEntity || entity instanceof SettlementLeaderEntity leader && leader.isWarden();
    }

    public static void defendPlayer(ServerPlayer player, Entity source) {
        if (!(source instanceof LivingEntity hunter) || !hunter.isAlive() || !isHunter(hunter)
                || !WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false)) return;
        var nearby = player.getBoundingBox().inflate(28, 8, 28);
        for (SettlementLeaderEntity chief : player.level().getEntitiesOfClass(SettlementLeaderEntity.class, nearby,
                leader -> leader.isAlive() && !leader.isWarden() && leader.hasSettlement() && leader.canAttack(hunter))) {
            chief.defend(hunter, player);
        }
        for (TribeWerewolfEntity resident : player.level().getEntitiesOfClass(TribeWerewolfEntity.class, nearby,
                ally -> ally.isAlive() && ally.hasSettlement() && ally.canAttack(hunter))) {
            resident.protectChief(hunter);
        }
    }

    private SettlementCombat() {}
}
