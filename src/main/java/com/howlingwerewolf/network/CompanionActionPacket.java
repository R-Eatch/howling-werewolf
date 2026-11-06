/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.network;

import com.howlingwerewolf.companion.CompanionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Requests carry only intent and identity. Costs, ownership, time and rewards are server-owned. */
public record CompanionActionPacket(Action action, UUID target, String value) {
    public enum Action { OPEN, REFRESH, RECRUIT, FOLLOW, GUARD, REST, HUNT, RECALL, UPGRADE, REVIVE, INVENTORY, RENAME, SKIN, ADDRESS, SORT, DISMISS }
    public CompanionActionPacket(Action action, UUID target) { this(action, target, ""); }
    public static final UUID NO_TARGET = new UUID(0, 0);

    public static void encode(CompanionActionPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.action);
        buf.writeUUID(packet.target);
        buf.writeUtf(packet.value, 64);
    }
    public static CompanionActionPacket decode(FriendlyByteBuf buf) {
        return new CompanionActionPacket(buf.readEnum(Action.class), buf.readUUID(), buf.readUtf(64));
    }
    public static void handle(CompanionActionPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) CompanionManager.handle(sender, packet);
        context.get().setPacketHandled(true);
    }
}
