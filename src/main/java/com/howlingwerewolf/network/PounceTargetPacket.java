package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.phys.Vec3;

public record PounceTargetPacket(Vec3 position) implements CustomPacketPayload {
    public static final Type<PounceTargetPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("howlingwerewolf", "pounce_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PounceTargetPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> Vec3.STREAM_CODEC.encode(buf, packet.position()), buf -> new PounceTargetPacket(Vec3.STREAM_CODEC.decode(buf)));

    @Override public Type<PounceTargetPacket> type() { return TYPE; }

    public static void handle(PounceTargetPacket packet, ServerPlayNetworking.Context context) {
        PounceAbility.start(context.player(), packet.position());
    }
}
