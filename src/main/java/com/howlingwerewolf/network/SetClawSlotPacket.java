package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetClawSlotPacket(int slot) implements CustomPacketPayload {
    public static final Type<SetClawSlotPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "set_claw_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetClawSlotPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeByte(packet.slot()),
            buf -> new SetClawSlotPacket(buf.readByte()));

    @Override
    public Type<SetClawSlotPacket> type() {
        return TYPE;
    }

    public static void handle(SetClawSlotPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender && packet.slot() >= 0 && packet.slot() < 9) {
            WerewolfApi.get(sender).ifPresent(data -> {
            if (data.hasAbility(WerewolfAbility.EMPTY_CLAW_SLOT)) {
                data.setClawHotbarSlot(packet.slot());
                ModNetwork.sync(sender, data);
            }
        });
        }
    }
}
