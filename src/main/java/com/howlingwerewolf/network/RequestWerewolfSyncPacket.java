package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server resync request used after login / dimension transitions.
 * The client intentionally carries no state; the server remains authoritative.
 */
public record RequestWerewolfSyncPacket() implements CustomPacketPayload {
    public static final Type<RequestWerewolfSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "request_werewolf_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWerewolfSyncPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestWerewolfSyncPacket());

    @Override
    public Type<RequestWerewolfSyncPacket> type() {
        return TYPE;
    }

    public static void handle(RequestWerewolfSyncPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) {
            WerewolfApi.get(sender).ifPresent(data -> ModNetwork.sync(sender, data));
        }
    }
}
