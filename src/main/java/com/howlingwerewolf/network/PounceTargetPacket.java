package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PounceTargetPacket(Vec3 position) implements CustomPacketPayload {
    public static final Type<PounceTargetPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("howlingwerewolf", "pounce_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PounceTargetPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVec3(packet.position()), buf -> new PounceTargetPacket(buf.readVec3()));

    @Override public Type<PounceTargetPacket> type() { return TYPE; }

    public static void handle(PounceTargetPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) PounceAbility.start(player, packet.position());
    }
}
