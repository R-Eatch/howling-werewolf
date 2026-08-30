package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResetProgressionPacket() implements CustomPacketPayload {
    public static final Type<ResetProgressionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "reset_progression"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetProgressionPacket> STREAM_CODEC =
            StreamCodec.unit(new ResetProgressionPacket());

    @Override
    public Type<ResetProgressionPacket> type() {
        return TYPE;
    }

    public static void handle(ResetProgressionPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender) WerewolfApi.get(sender).ifPresent(data -> {
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
    }
}
