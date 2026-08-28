package com.howlingwerewolf.network;

import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleQuadrupedModePacket {
    public static void encode(ToggleQuadrupedModePacket packet, FriendlyByteBuf buf) {}
    public static ToggleQuadrupedModePacket decode(FriendlyByteBuf buf) {
        return new ToggleQuadrupedModePacket();
    }

    public static void handle(ToggleQuadrupedModePacket packet,
                              Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfGameplayEvents.toggleQuadrupedMode(sender);
        context.get().setPacketHandled(true);
    }
}
