package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.capability.WerewolfPersistence;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingWerewolf.MOD_ID)
public final class WerewolfCapabilityEvents {
    /** Keep the legacy player-NBT mirror current before a dimension transition. */
    @SubscribeEvent
    public static void beforeDimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WerewolfApi.get(player).ifPresent(data -> WerewolfPersistence.save(player, data));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clonePlayer(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player replacement = event.getEntity();

        // NeoForge copies serializable attachments before this lowest-priority listener. Preserve
        // the Forge 1.20.1 PlayerPersisted mirror as an additional old-world recovery route.
        WerewolfPersistence.copyPersistentRoot(original, replacement);
        WerewolfData newData = replacement.getData(com.howlingwerewolf.capability.ModAttachments.WEREWOLF);
        if (newData.isDefaultState()) {
            CompoundTag legacySnapshot = WerewolfPersistence.readSnapshot(original);
            if (legacySnapshot != null) newData.deserializeNBT(legacySnapshot);
        }

        if (event.isWasDeath()) {
            if (replacement instanceof ServerPlayer serverPlayer) {
                WerewolfGameplayEvents.removeSpiritWolves(serverPlayer, newData);
            } else {
                newData.getSpiritWolfIds().clear();
                newData.setWolfSpiritExpireTime(0L);
            }
            newData.prepareAfterDeath();
            replacement.refreshDimensions();
        }

        WerewolfPersistence.save(replacement, newData);
    }

    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        restoreAndSync(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer player) {
            com.howlingwerewolf.trial.AlphaTrialManager.deliverPendingFailureMessage(player);
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        restoreAndSync(event.getEntity());
    }

    @SubscribeEvent
    public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        restoreAndSync(event.getEntity());
    }

    @SubscribeEvent
    public static void playerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WerewolfApi.get(player).ifPresent(data -> WerewolfPersistence.save(player, data));
        }
    }

    @SubscribeEvent
    public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.howlingwerewolf.trial.AlphaTrialManager.onPlayerLogout(player);
            WerewolfApi.get(player).ifPresent(data -> {
                WerewolfGameplayEvents.removeSpiritWolves(player, data);
                WerewolfPersistence.save(player, data);
            });
            WerewolfGameplayEvents.clearTransientTracking(player);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer target) {
            WerewolfApi.get(target).ifPresent(data -> PacketDistributor.sendToPlayer(observer,
                    new com.howlingwerewolf.network.SyncWerewolfDataPacket(
                            target.getId(), data.serializeNBT())));
        }
    }

    private static void restoreAndSync(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        java.util.Optional<WerewolfData> optional = WerewolfApi.get(serverPlayer);
        if (!optional.isPresent()) {
            HowlingWerewolf.LOGGER.error(
                    "Werewolf capability missing for {} after player lifecycle event. Dimension state cannot be restored.",
                    serverPlayer.getGameProfile().getName());
            return;
        }
        optional.ifPresent(data -> {
            // The capability serializer / Clone event is the live authority. The PlayerPersisted
            // mirror is only a recovery path for a genuinely fresh/default provider; never roll a
            // non-default live capability back to an older mirror on login, respawn or travel.
            WerewolfPersistence.recoverIfFresh(serverPlayer, data);
            WerewolfGameplayEvents.refreshWerewolfModifiers(serverPlayer, data);
            serverPlayer.refreshDimensions();
            ModNetwork.sync(serverPlayer, data);
        });
    }

    private WerewolfCapabilityEvents() {}
}
