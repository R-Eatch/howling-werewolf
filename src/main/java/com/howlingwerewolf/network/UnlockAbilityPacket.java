package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGuide;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UnlockAbilityPacket(WerewolfAbility ability) implements CustomPacketPayload {
    public static final Type<UnlockAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "unlock_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockAbilityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.ability()),
            buf -> new UnlockAbilityPacket(buf.readEnum(WerewolfAbility.class)));

    @Override
    public Type<UnlockAbilityPacket> type() {
        return TYPE;
    }

    public static void handle(UnlockAbilityPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.unlockAbility(packet.ability())) {
                WerewolfGuide.firstPoint(sender);
                ModNetwork.sync(sender, data);
            }
        });
    }
}
