/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = HowlingWerewolf.MOD_ID)
public final class CompanionEvents {
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) { CompanionManager.tick(event.getServer()); }
    @SubscribeEvent public static void attacked(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer owner && event.getTarget() instanceof LivingEntity target) CompanionManager.defend(owner, target);
    }
    @SubscribeEvent public static void targetChanged(LivingChangeTargetEvent event) {
        if (WerewolfCompanionEntity.ignoresFollowingCompanion(event.getEntity(), event.getNewAboutToBeSetTarget())) {
            // Replacing with null also clears a previous target; canceling would retain it.
            event.setNewAboutToBeSetTarget(null);
        }
    }
    @SubscribeEvent public static void hurt(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0) return;
        if (event.getSource().getEntity() instanceof com.howlingwerewolf.entity.WerewolfCompanionEntity companion
                && event.getEntity().isDeadOrDying() && companion.owner() != null) companion.sayOccasionally(companion.owner(), "victory");
        if (event.getEntity() instanceof ServerPlayer owner && event.getSource().getEntity() instanceof LivingEntity attacker) CompanionManager.defend(owner, attacker);
        if (event.getSource().getEntity() instanceof ServerPlayer owner) CompanionManager.defend(owner, event.getEntity());
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompanionSavedData data = CompanionSavedData.get(player.level().getServer());
        for (CompanionRecord record : data.ownedBy(player.getUUID())) {
            var entity = data.loaded(record.id);
            if (entity != null) { CompanionManager.remember(entity); entity.discard(); }
        }
        data.lastRequests.remove(player.getUUID());
        data.lastActions.remove(player.getUUID());
    }
    private CompanionEvents() {}
}
