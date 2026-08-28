package com.howlingwerewolf.network;

import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class TransformRequestPacket {
    public static void encode(TransformRequestPacket packet, FriendlyByteBuf buf) {}
    public static TransformRequestPacket decode(FriendlyByteBuf buf) { return new TransformRequestPacket(); }

    public static void handle(TransformRequestPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) {
            WerewolfGameplayEvents.requestTransform(sender);
        }
        context.get().setPacketHandled(true);
    }
}
