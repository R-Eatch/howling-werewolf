/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

/** Server hooks for companions; a monster's choice of target is filtered in MobMixin. */
public final class CompanionEvents {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CompanionManager::tick);
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof WerewolfCompanionEntity companion) companion.trackingStarted();
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof WerewolfCompanionEntity companion) companion.trackingEnded();
        });
        AttackEntityCallback.EVENT.register((player, level, hand, target, hit) -> {
            if (player instanceof ServerPlayer owner && target instanceof LivingEntity living && !level.isClientSide()) CompanionManager.defend(owner, living);
            return InteractionResult.PASS;
        });
        // Fabric reports a fatal hit through AFTER_DEATH only, so the victory line lives there.
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken <= 0 || blocked) return;
            if (entity instanceof ServerPlayer owner && source.getEntity() instanceof LivingEntity attacker) CompanionManager.defend(owner, attacker);
            if (source.getEntity() instanceof ServerPlayer owner) CompanionManager.defend(owner, entity);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof WerewolfCompanionEntity companion && companion.owner() != null) companion.sayOccasionally(companion.owner(), "victory");
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            CompanionSavedData data = CompanionSavedData.get(server);
            for (CompanionRecord record : data.ownedBy(player.getUUID())) {
                var entity = data.loaded(record.id);
                if (entity != null) { CompanionManager.remember(entity); entity.discard(); }
            }
            data.lastRequests.remove(player.getUUID());
            data.lastActions.remove(player.getUUID());
        });
        CompanionSorting.register();
    }
    private CompanionEvents() {}
}
