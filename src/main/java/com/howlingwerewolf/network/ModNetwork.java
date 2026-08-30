package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.capability.WerewolfPersistence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String PROTOCOL = "8";

    /** Registers all gameplay payloads; handlers run on the main game thread by default. */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(SyncWerewolfDataPacket.TYPE, SyncWerewolfDataPacket.STREAM_CODEC,
                SyncWerewolfDataPacket::handle);
        registrar.playToClient(BadgeActivationPacket.TYPE, BadgeActivationPacket.STREAM_CODEC,
                BadgeActivationPacket::handle);
        registrar.playToServer(TransformRequestPacket.TYPE, TransformRequestPacket.STREAM_CODEC,
                TransformRequestPacket::handle);
        registrar.playToServer(UpgradeTreeSkillPacket.TYPE, UpgradeTreeSkillPacket.STREAM_CODEC,
                UpgradeTreeSkillPacket::handle);
        registrar.playToServer(UnlockAbilityPacket.TYPE, UnlockAbilityPacket.STREAM_CODEC,
                UnlockAbilityPacket::handle);
        registrar.playToServer(ToggleBeastModePacket.TYPE, ToggleBeastModePacket.STREAM_CODEC,
                ToggleBeastModePacket::handle);
        registrar.playToServer(ToggleQuadrupedModePacket.TYPE, ToggleQuadrupedModePacket.STREAM_CODEC,
                ToggleQuadrupedModePacket::handle);
        registrar.playToServer(ToggleNightVisionPacket.TYPE, ToggleNightVisionPacket.STREAM_CODEC,
                ToggleNightVisionPacket::handle);
        registrar.playToServer(UseAbilityPacket.TYPE, UseAbilityPacket.STREAM_CODEC,
                UseAbilityPacket::handle);
        registrar.playToServer(SetClawSlotPacket.TYPE, SetClawSlotPacket.STREAM_CODEC,
                SetClawSlotPacket::handle);
        registrar.playToServer(ResetProgressionPacket.TYPE, ResetProgressionPacket.STREAM_CODEC,
                ResetProgressionPacket::handle);
        registrar.playToServer(RequestWerewolfSyncPacket.TYPE, RequestWerewolfSyncPacket.STREAM_CODEC,
                RequestWerewolfSyncPacket::handle);
    }

    public static void sync(ServerPlayer player, WerewolfData data) {
        WerewolfPersistence.save(player, data);
        CompoundTag syncData = data.serializeNBT();
        syncData.putInt("NetworkMaxLevel", WerewolfData.getMaxLevel());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SyncWerewolfDataPacket(player.getId(), syncData));
    }

    public static void showBadgeActivation(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new BadgeActivationPacket());
    }

    private ModNetwork() {}
}
