/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Switches a learned claw ability on or off. The server checks the ability and its ownership. */
public record ToggleAbilityPacket(WerewolfAbility ability) implements CustomPacketPayload {
    public static final Type<ToggleAbilityPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "toggle_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAbilityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.ability()),
            buf -> new ToggleAbilityPacket(buf.readEnum(WerewolfAbility.class)));

    @Override
    public Type<ToggleAbilityPacket> type() {
        return TYPE;
    }

    public static void handle(ToggleAbilityPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.toggleAbility(packet.ability())) ModNetwork.sync(sender, data);
        });
    }
}
