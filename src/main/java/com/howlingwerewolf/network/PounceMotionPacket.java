package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record PounceMotionPacket(int entityId, PounceAbility.Motion motion, Vec3 velocity) implements CustomPacketPayload {
    public static final Type<PounceMotionPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("howlingwerewolf", "pounce_motion"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PounceMotionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId());
                buf.writeEnum(packet.motion());
                if (packet.motion() == PounceAbility.Motion.FLIGHT) Vec3.STREAM_CODEC.encode(buf, packet.velocity());
            },
            buf -> {
                int entityId = buf.readVarInt();
                var motion = buf.readEnum(PounceAbility.Motion.class);
                return new PounceMotionPacket(entityId, motion,
                        motion == PounceAbility.Motion.FLIGHT ? Vec3.STREAM_CODEC.decode(buf) : Vec3.ZERO);
            });

    @Override public Type<PounceMotionPacket> type() { return TYPE; }

}
