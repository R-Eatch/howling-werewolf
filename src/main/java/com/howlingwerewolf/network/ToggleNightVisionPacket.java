package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleNightVisionPacket() implements CustomPacketPayload {
    public static final Type<ToggleNightVisionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "toggle_night_vision"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleNightVisionPacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleNightVisionPacket());

    @Override
    public Type<ToggleNightVisionPacket> type() {
        return TYPE;
    }

    public static void handle(ToggleNightVisionPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfGameplayEvents.toggleNightVision(sender);
    }
}
