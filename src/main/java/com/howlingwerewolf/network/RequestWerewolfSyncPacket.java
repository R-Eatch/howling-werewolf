package com.howlingwerewolf.network;

import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client-to-server resync request used after login / dimension transitions.
 * The client intentionally carries no state; the server remains authoritative.
 */
public final class RequestWerewolfSyncPacket {
    public static void encode(RequestWerewolfSyncPacket packet, FriendlyByteBuf buf) {
        // no payload
    }

    public static RequestWerewolfSyncPacket decode(FriendlyByteBuf buf) {
        return new RequestWerewolfSyncPacket();
    }

    public static void handle(RequestWerewolfSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) {
            WerewolfApi.get(sender).ifPresent(data -> ModNetwork.sync(sender, data));
        }
        context.get().setPacketHandled(true);
    }
}
