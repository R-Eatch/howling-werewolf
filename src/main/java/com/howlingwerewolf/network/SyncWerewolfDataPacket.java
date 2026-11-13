package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncWerewolfDataPacket(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncWerewolfDataPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "sync_werewolf_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWerewolfDataPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId());
                buf.writeNbt(packet.data());
            },
            buf -> {
                int entityId = buf.readVarInt();
                CompoundTag tag = buf.readNbt();
                return new SyncWerewolfDataPacket(entityId, tag == null ? new CompoundTag() : tag);
            });

    @Override
    public Type<SyncWerewolfDataPacket> type() {
        return TYPE;
    }

}
