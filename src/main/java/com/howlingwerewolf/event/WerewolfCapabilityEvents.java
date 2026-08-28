package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.capability.WerewolfProvider;
import com.howlingwerewolf.capability.WerewolfPersistence;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WerewolfCapabilityEvents {
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        // Attach exactly one provider to every newly constructed Player object. Forge 1.20.1
        // keeps the same ServerPlayer object for ordinary dimension travel; WerewolfProvider
        // therefore owns a revivable LazyOptional so the attached provider survives that
        // invalidate/revive lifecycle without losing its WerewolfData instance.
        if (event.getObject() instanceof Player) {
            WerewolfProvider provider = new WerewolfProvider();
            event.addCapability(WerewolfProvider.ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    /** Persist the live capability before Forge/Minecraft begins replacing the player. */
    @SubscribeEvent
    public static void beforeDimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WerewolfApi.get(player).ifPresent(data -> WerewolfPersistence.save(player, data));
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player replacement = event.getEntity();

        // First preserve the Forge persistent root. It is available even if the capability
        // on the old player has already been invalidated by the lifecycle.
        WerewolfPersistence.copyPersistentRoot(original, replacement);
        CompoundTag snapshot = WerewolfPersistence.readSnapshot(original);

        // Forge invalidates original player capabilities around cloning. Temporarily revive
        // them and prefer the live state over the NBT mirror whenever possible.
        original.reviveCaps();
        CompoundTag liveSnapshot = WerewolfApi.get(original)
                .map(WerewolfData::serializeNBT)
                .orElse(null);
        if (liveSnapshot != null) {
            snapshot = liveSnapshot;
        } else if (original instanceof ServerPlayer serverOriginal) {
            HowlingWerewolf.LOGGER.warn(
                    "Original werewolf capability unavailable while cloning {}; using PlayerPersisted fallback.",
                    serverOriginal.getGameProfile().getName());
        }

        final CompoundTag finalSnapshot = snapshot;
        net.minecraftforge.common.util.LazyOptional<WerewolfData> replacementOptional = WerewolfApi.get(replacement);
        if (!replacementOptional.isPresent()) {
            HowlingWerewolf.LOGGER.error(
                    "Replacement Player is missing the werewolf capability during Clone. Player={}",
                    replacement.getGameProfile().getName());
            original.invalidateCaps();
            return;
        }
        replacementOptional.ifPresent(newData -> {
            if (finalSnapshot != null) {
                newData.deserializeNBT(finalSnapshot);
            } else {
                WerewolfPersistence.load(replacement, newData);
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
        });
        original.invalidateCaps();
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
            WerewolfApi.get(target).ifPresent(data -> ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> observer),
                    new com.howlingwerewolf.network.SyncWerewolfDataPacket(target.getId(), data.serializeNBT())));
        }
    }

    private static void restoreAndSync(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        net.minecraftforge.common.util.LazyOptional<WerewolfData> optional = WerewolfApi.get(serverPlayer);
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
