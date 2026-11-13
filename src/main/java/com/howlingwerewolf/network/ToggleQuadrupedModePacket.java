package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record ToggleQuadrupedModePacket() implements CustomPacketPayload {
    public static final Type<ToggleQuadrupedModePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "toggle_quadruped_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleQuadrupedModePacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleQuadrupedModePacket());

    @Override
    public Type<ToggleQuadrupedModePacket> type() {
        return TYPE;
    }

    public static void handle(ToggleQuadrupedModePacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer sender = context.player(); WerewolfGameplayEvents.toggleQuadrupedMode(sender);
    }
}
