package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.capability.WerewolfPersistence;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "8";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(HowlingWerewolf.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();
    private static int nextId;
    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(SyncWerewolfDataPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncWerewolfDataPacket::encode).decoder(SyncWerewolfDataPacket::decode)
                .consumerMainThread(SyncWerewolfDataPacket::handle).add();
        CHANNEL.messageBuilder(TransformRequestPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TransformRequestPacket::encode).decoder(TransformRequestPacket::decode)
                .consumerMainThread(TransformRequestPacket::handle).add();
        CHANNEL.messageBuilder(UpgradeTreeSkillPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpgradeTreeSkillPacket::encode).decoder(UpgradeTreeSkillPacket::decode)
                .consumerMainThread(UpgradeTreeSkillPacket::handle).add();
        CHANNEL.messageBuilder(UnlockAbilityPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UnlockAbilityPacket::encode).decoder(UnlockAbilityPacket::decode)
                .consumerMainThread(UnlockAbilityPacket::handle).add();
        CHANNEL.messageBuilder(ToggleBeastModePacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleBeastModePacket::encode).decoder(ToggleBeastModePacket::decode)
                .consumerMainThread(ToggleBeastModePacket::handle).add();
        CHANNEL.messageBuilder(ToggleQuadrupedModePacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleQuadrupedModePacket::encode).decoder(ToggleQuadrupedModePacket::decode)
                .consumerMainThread(ToggleQuadrupedModePacket::handle).add();
        CHANNEL.messageBuilder(ToggleNightVisionPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleNightVisionPacket::encode).decoder(ToggleNightVisionPacket::decode)
                .consumerMainThread(ToggleNightVisionPacket::handle).add();
        CHANNEL.messageBuilder(UseAbilityPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UseAbilityPacket::encode).decoder(UseAbilityPacket::decode)
                .consumerMainThread(UseAbilityPacket::handle).add();
        CHANNEL.messageBuilder(SetClawSlotPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetClawSlotPacket::encode).decoder(SetClawSlotPacket::decode)
                .consumerMainThread(SetClawSlotPacket::handle).add();
        CHANNEL.messageBuilder(ResetProgressionPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ResetProgressionPacket::encode).decoder(ResetProgressionPacket::decode)
                .consumerMainThread(ResetProgressionPacket::handle).add();
        CHANNEL.messageBuilder(BadgeActivationPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BadgeActivationPacket::encode).decoder(BadgeActivationPacket::decode)
                .consumerMainThread(BadgeActivationPacket::handle).add();
        CHANNEL.messageBuilder(RequestWerewolfSyncPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestWerewolfSyncPacket::encode).decoder(RequestWerewolfSyncPacket::decode)
                .consumerMainThread(RequestWerewolfSyncPacket::handle).add();
    }

    public static void sync(ServerPlayer player, WerewolfData data) {
        WerewolfPersistence.save(player, data);
        CompoundTag syncData = data.serializeNBT();
        syncData.putInt("NetworkMaxLevel", WerewolfData.getMaxLevel());
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncWerewolfDataPacket(player.getId(), syncData));
    }

    public static void showBadgeActivation(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BadgeActivationPacket());
    }

    private ModNetwork() {}
}
