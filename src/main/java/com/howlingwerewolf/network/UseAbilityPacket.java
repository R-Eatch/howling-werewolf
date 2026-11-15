package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseAbilityPacket(WerewolfAbility ability, int targetId) implements CustomPacketPayload {
    public static final Type<UseAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "use_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeEnum(packet.ability());
                buf.writeVarInt(packet.targetId());
            },
            buf -> new UseAbilityPacket(buf.readEnum(WerewolfAbility.class), buf.readVarInt()));

    public UseAbilityPacket(WerewolfAbility ability) { this(ability, -1); }

    @Override
    public Type<UseAbilityPacket> type() {
        return TYPE;
    }

    public static void handle(UseAbilityPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender) || packet.targetId() < -1) return;
        if (packet.ability() != WerewolfAbility.SUMMON_WOLF_SPIRIT
                && packet.ability() != WerewolfAbility.BLOODY_BITE
                && packet.ability() != WerewolfAbility.MOONBLOOD_SURGE) return;
        if (packet.ability() != WerewolfAbility.BLOODY_BITE && packet.targetId() != -1) return;
        WerewolfGameplayEvents.useAbility(sender, packet.ability(), packet.targetId());
    }
}
