package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.client.ClientPacketHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BadgeActivationPacket() implements CustomPacketPayload {
    public static final Type<BadgeActivationPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "badge_activation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BadgeActivationPacket> STREAM_CODEC =
            StreamCodec.unit(new BadgeActivationPacket());

    @Override
    public Type<BadgeActivationPacket> type() {
        return TYPE;
    }

    public static void handle(BadgeActivationPacket packet, IPayloadContext context) {
        ClientPacketHandlers.showBadgeActivation();
    }
}
