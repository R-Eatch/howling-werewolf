package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import com.howlingwerewolf.client.ClientPouncePose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PounceMotionPacket(int entityId, PounceAbility.Motion motion) implements CustomPacketPayload {
    public static final Type<PounceMotionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("howlingwerewolf", "pounce_motion"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PounceMotionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> { buf.writeVarInt(packet.entityId()); buf.writeEnum(packet.motion()); },
            buf -> new PounceMotionPacket(buf.readVarInt(), buf.readEnum(PounceAbility.Motion.class)));

    @Override public Type<PounceMotionPacket> type() { return TYPE; }

    public static void handle(PounceMotionPacket packet, IPayloadContext context) {
        ClientPouncePose.receive(packet);
    }
}
