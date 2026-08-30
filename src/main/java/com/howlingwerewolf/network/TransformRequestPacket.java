package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TransformRequestPacket() implements CustomPacketPayload {
    public static final Type<TransformRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "transform_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TransformRequestPacket> STREAM_CODEC =
            StreamCodec.unit(new TransformRequestPacket());

    @Override
    public Type<TransformRequestPacket> type() {
        return TYPE;
    }

    public static void handle(TransformRequestPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfGameplayEvents.requestTransform(sender);
    }
}
