package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetClawSlotPacket(int slot) {
    public static void encode(SetClawSlotPacket packet, FriendlyByteBuf buf) { buf.writeByte(packet.slot); }
    public static SetClawSlotPacket decode(FriendlyByteBuf buf) { return new SetClawSlotPacket(buf.readByte()); }
    public static void handle(SetClawSlotPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null && packet.slot >= 0 && packet.slot < 9) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.hasAbility(WerewolfAbility.EMPTY_CLAW_SLOT)) {
                data.setClawHotbarSlot(packet.slot);
                ModNetwork.sync(sender, data);
            }
        });
        context.get().setPacketHandled(true);
    }
}
