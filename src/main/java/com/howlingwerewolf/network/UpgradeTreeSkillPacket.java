package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import com.howlingwerewolf.event.WerewolfGuide;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpgradeTreeSkillPacket(WerewolfTreeSkill skill) implements CustomPacketPayload {
    public static final Type<UpgradeTreeSkillPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "upgrade_tree_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeTreeSkillPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.skill()),
            buf -> new UpgradeTreeSkillPacket(buf.readEnum(WerewolfTreeSkill.class)));

    @Override
    public Type<UpgradeTreeSkillPacket> type() {
        return TYPE;
    }

    public static void handle(UpgradeTreeSkillPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.upgradeTreeSkill(packet.skill())) {
                WerewolfGameplayEvents.refreshWerewolfModifiers(sender, data);
                WerewolfGuide.firstPoint(sender);
                ModNetwork.sync(sender, data);
            }
        });
    }
}
