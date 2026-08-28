package com.howlingwerewolf.network;

import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleNightVisionPacket {
    public static void encode(ToggleNightVisionPacket packet, FriendlyByteBuf buf) {}
    public static ToggleNightVisionPacket decode(FriendlyByteBuf buf) { return new ToggleNightVisionPacket(); }
    public static void handle(ToggleNightVisionPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfGameplayEvents.toggleNightVision(sender);
        context.get().setPacketHandled(true);
    }
}
