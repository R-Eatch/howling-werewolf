package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UseAbilityPacket(WerewolfAbility ability, int targetId) {
    public UseAbilityPacket(WerewolfAbility ability) { this(ability, -1); }
    public static void encode(UseAbilityPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.ability);
        buf.writeVarInt(packet.targetId);
    }
    public static UseAbilityPacket decode(FriendlyByteBuf buf) {
        return new UseAbilityPacket(buf.readEnum(WerewolfAbility.class), buf.readVarInt());
    }
    public static void handle(UseAbilityPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfGameplayEvents.useAbility(sender, packet.ability, packet.targetId);
        context.get().setPacketHandled(true);
    }
}
