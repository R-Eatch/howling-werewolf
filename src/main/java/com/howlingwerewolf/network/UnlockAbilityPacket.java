package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGuide;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UnlockAbilityPacket(WerewolfAbility ability) {
    public static void encode(UnlockAbilityPacket packet, FriendlyByteBuf buf) { buf.writeEnum(packet.ability); }
    public static UnlockAbilityPacket decode(FriendlyByteBuf buf) {
        return new UnlockAbilityPacket(buf.readEnum(WerewolfAbility.class));
    }
    public static void handle(UnlockAbilityPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.unlockAbility(packet.ability)) {
                WerewolfGuide.firstPoint(sender);
                ModNetwork.sync(sender, data);
            }
        });
        context.get().setPacketHandled(true);
    }
}
