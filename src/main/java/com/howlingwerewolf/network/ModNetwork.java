package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.capability.WerewolfPersistence;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetwork {
    private static final String PROTOCOL = "22";

    public static void register() {
        PayloadTypeRegistry.playS2C().register(BadgeActivationPacket.TYPE, BadgeActivationPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestWerewolfSyncPacket.TYPE, RequestWerewolfSyncPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestWerewolfSyncPacket.TYPE, (packet, context) -> context.server().execute(() -> RequestWerewolfSyncPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(ResetProgressionPacket.TYPE, ResetProgressionPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ResetProgressionPacket.TYPE, (packet, context) -> context.server().execute(() -> ResetProgressionPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(SetClawSlotPacket.TYPE, SetClawSlotPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetClawSlotPacket.TYPE, (packet, context) -> context.server().execute(() -> SetClawSlotPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(SetWerewolfSkinPacket.TYPE, SetWerewolfSkinPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetWerewolfSkinPacket.TYPE, (packet, context) -> context.server().execute(() -> SetWerewolfSkinPacket.handle(packet, context)));
        PayloadTypeRegistry.playS2C().register(SyncWerewolfDataPacket.TYPE, SyncWerewolfDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleBeastModePacket.TYPE, ToggleBeastModePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleBeastModePacket.TYPE, (packet, context) -> context.server().execute(() -> ToggleBeastModePacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(ToggleNightVisionPacket.TYPE, ToggleNightVisionPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleNightVisionPacket.TYPE, (packet, context) -> context.server().execute(() -> ToggleNightVisionPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(ToggleQuadrupedModePacket.TYPE, ToggleQuadrupedModePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleQuadrupedModePacket.TYPE, (packet, context) -> context.server().execute(() -> ToggleQuadrupedModePacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(TransformRequestPacket.TYPE, TransformRequestPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TransformRequestPacket.TYPE, (packet, context) -> context.server().execute(() -> TransformRequestPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(UnlockAbilityPacket.TYPE, UnlockAbilityPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UnlockAbilityPacket.TYPE, (packet, context) -> context.server().execute(() -> UnlockAbilityPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(UpgradeTreeSkillPacket.TYPE, UpgradeTreeSkillPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UpgradeTreeSkillPacket.TYPE, (packet, context) -> context.server().execute(() -> UpgradeTreeSkillPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(UseAbilityPacket.TYPE, UseAbilityPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleAbilityPacket.TYPE, ToggleAbilityPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleAbilityPacket.TYPE, (packet, context) -> context.server().execute(() -> ToggleAbilityPacket.handle(packet, context)));
        PayloadTypeRegistry.playC2S().register(CompanionActionPacket.TYPE, CompanionActionPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CompanionActionPacket.TYPE, (packet, context) -> context.server().execute(() -> CompanionActionPacket.handle(packet, context)));
        PayloadTypeRegistry.playS2C().register(CompanionViewPacket.TYPE, CompanionViewPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseAbilityPacket.TYPE, (packet, context) -> context.server().execute(() -> UseAbilityPacket.handle(packet, context)));
    }

    public static void sendToPlayer(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload packet) {
        if (ServerPlayNetworking.canSend(player, packet.type())) ServerPlayNetworking.send(player, packet);
    }
    public static void sync(ServerPlayer player, WerewolfData data) {
        WerewolfPersistence.save(player, data);
        var packet = createSyncPacket(player.getId(), data);
        sendToPlayer(player, packet);
        for (ServerPlayer observer : PlayerLookup.tracking(player)) sendToPlayer(observer, packet);
    }

    /** Every recipient, including new observers, needs the authoritative progression limit. */
    public static SyncWerewolfDataPacket createSyncPacket(int entityId, WerewolfData data) {
        CompoundTag syncData = data.serializeNBT();
        syncData.putInt("NetworkMaxLevel", WerewolfData.getMaxLevel());
        return new SyncWerewolfDataPacket(entityId, syncData);
    }

    public static void showBadgeActivation(ServerPlayer player) {
        sendToPlayer(player, new BadgeActivationPacket());
    }

    private ModNetwork() {}
}
