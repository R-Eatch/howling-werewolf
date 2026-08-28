package com.howlingwerewolf.network;

import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleBeastModePacket {
    public static void encode(ToggleBeastModePacket packet, FriendlyByteBuf buf) {}
    public static ToggleBeastModePacket decode(FriendlyByteBuf buf) { return new ToggleBeastModePacket(); }
    public static void handle(ToggleBeastModePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfGameplayEvents.toggleBeastMode(sender);
        context.get().setPacketHandled(true);
    }
}
