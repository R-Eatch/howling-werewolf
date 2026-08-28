package com.howlingwerewolf.network;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ResetProgressionPacket() {
    public static void encode(ResetProgressionPacket packet, FriendlyByteBuf buf) {}
    public static ResetProgressionPacket decode(FriendlyByteBuf buf) { return new ResetProgressionPacket(); }

    public static void handle(ResetProgressionPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) WerewolfApi.get(sender).ifPresent(data -> {
            if (!data.canResetProgression()) {
                sender.sendSystemMessage(Component.translatable("message.howlingwerewolf.reset_unavailable")
                        .withStyle(ChatFormatting.RED));
                return;
            }
            WerewolfGameplayEvents.removeSpiritWolves(sender, data);
            if (!data.resetProgressionWithLevelCost()) return;
            WerewolfGameplayEvents.refreshWerewolfModifiers(sender, data);
            sender.refreshDimensions();
            ModNetwork.sync(sender, data);
            sender.sendSystemMessage(Component.translatable("message.howlingwerewolf.reset_success",
                    data.getLevel(), data.getAvailableSkillPoints(), data.getAvailableTreePoints())
                    .withStyle(ChatFormatting.GREEN));
        });
        context.get().setPacketHandled(true);
    }
}
