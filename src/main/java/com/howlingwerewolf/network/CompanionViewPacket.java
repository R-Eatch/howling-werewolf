/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Private owner-only summaries. Inventory stacks travel through the native container protocol. */
public record CompanionViewPacket(CompoundTag data, boolean open) implements CustomPacketPayload {
    public static final Type<CompanionViewPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("howlingwerewolf", "companion_view"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionViewPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> { buf.writeNbt(packet.data); buf.writeBoolean(packet.open); },
            buf -> { CompoundTag tag = buf.readNbt(); return new CompanionViewPacket(tag == null ? new CompoundTag() : tag, buf.readBoolean()); });
    @Override public Type<CompanionViewPacket> type() { return TYPE; }
}
