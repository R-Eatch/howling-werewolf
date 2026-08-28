package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import com.howlingwerewolf.event.WerewolfGuide;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpgradeTreeSkillPacket(WerewolfTreeSkill skill) {
    public static void encode(UpgradeTreeSkillPacket packet, FriendlyByteBuf buf) { buf.writeEnum(packet.skill); }
    public static UpgradeTreeSkillPacket decode(FriendlyByteBuf buf) {
        return new UpgradeTreeSkillPacket(buf.readEnum(WerewolfTreeSkill.class));
    }
    public static void handle(UpgradeTreeSkillPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfApi.get(sender).ifPresent(data -> {
            if (data.upgradeTreeSkill(packet.skill)) {
                WerewolfGameplayEvents.refreshWerewolfModifiers(sender, data);
                WerewolfGuide.firstPoint(sender);
                ModNetwork.sync(sender, data);
            }
        });
        context.get().setPacketHandled(true);
    }
}
