package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleBeastModePacket() implements CustomPacketPayload {
    public static final Type<ToggleBeastModePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "toggle_beast_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleBeastModePacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleBeastModePacket());

    @Override
    public Type<ToggleBeastModePacket> type() {
        return TYPE;
    }

    public static void handle(ToggleBeastModePacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfGameplayEvents.toggleBeastMode(sender);
    }
}
