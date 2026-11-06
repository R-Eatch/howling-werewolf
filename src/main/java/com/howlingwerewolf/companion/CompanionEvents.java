/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID)
public final class CompanionEvents {
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) CompanionManager.tick(event.getServer());
    }
    @SubscribeEvent public static void attacked(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer owner && event.getTarget() instanceof LivingEntity target) CompanionManager.defend(owner, target);
    }
    @SubscribeEvent public static void targetChanged(LivingChangeTargetEvent event) {
        if (WerewolfCompanionEntity.ignoresFollowingCompanion(event.getEntity(), event.getNewTarget())) {
            // Replacing with null also clears a previous target; canceling would retain it.
            event.setNewTarget(null);
        }
    }
    @SubscribeEvent public static void hurt(LivingDamageEvent event) {
        if (event.getAmount() <= 0) return;
        if (event.getEntity() instanceof ServerPlayer owner && event.getSource().getEntity() instanceof LivingEntity attacker) CompanionManager.defend(owner, attacker);
        if (event.getSource().getEntity() instanceof ServerPlayer owner) CompanionManager.defend(owner, event.getEntity());
    }
    @SubscribeEvent public static void killed(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof WerewolfCompanionEntity companion && companion.owner() != null) {
            companion.sayOccasionally(companion.owner(), "victory");
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        for (CompanionRecord record : data.ownedBy(player.getUUID())) {
            var entity = data.loaded(record.id);
            if (entity != null) { CompanionManager.remember(entity); entity.discard(); }
        }
        data.lastRequests.remove(player.getUUID());
        data.lastActions.remove(player.getUUID());
    }
    private CompanionEvents() {}
}
