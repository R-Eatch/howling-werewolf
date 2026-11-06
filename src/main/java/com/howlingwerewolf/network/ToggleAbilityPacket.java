/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Switches a learned claw ability on or off. The server checks the ability and its ownership. */
public record ToggleAbilityPacket(WerewolfAbility ability) {
    public static void encode(ToggleAbilityPacket packet, FriendlyByteBuf buf) { buf.writeEnum(packet.ability); }
    public static ToggleAbilityPacket decode(FriendlyByteBuf buf) { return new ToggleAbilityPacket(buf.readEnum(WerewolfAbility.class)); }
    public static void handle(ToggleAbilityPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.toggleAbility(packet.ability)) ModNetwork.sync(sender, data);
        });
        context.get().setPacketHandled(true);
    }
}
