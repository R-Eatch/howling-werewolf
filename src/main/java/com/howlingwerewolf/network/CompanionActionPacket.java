/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.network;

import com.howlingwerewolf.companion.CompanionManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** Requests carry only intent and identity. Costs, ownership, time and rewards are server-owned. */
public record CompanionActionPacket(Action action, UUID target, String value) implements CustomPacketPayload {
    public enum Action { OPEN, REFRESH, RECRUIT, FOLLOW, GUARD, REST, HUNT, RECALL, UPGRADE, REVIVE, INVENTORY, RENAME, SKIN, ADDRESS, SORT }
    public CompanionActionPacket(Action action, UUID target) { this(action, target, ""); }
    public static final UUID NO_TARGET = new UUID(0, 0);
    public static final Type<CompanionActionPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("howlingwerewolf", "companion_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionActionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> { buf.writeEnum(packet.action); buf.writeUUID(packet.target); buf.writeUtf(packet.value, 64); },
            buf -> new CompanionActionPacket(buf.readEnum(Action.class), buf.readUUID(), buf.readUtf(64)));
    @Override public Type<CompanionActionPacket> type() { return TYPE; }
    public static void handle(CompanionActionPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) CompanionManager.handle(player, packet);
    }
}
