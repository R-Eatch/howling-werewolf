package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import com.howlingwerewolf.client.ClientPouncePose;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PounceMotionPacket(int entityId, PounceAbility.Motion motion) {
    public static void encode(PounceMotionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId());
        buffer.writeEnum(packet.motion());
    }

    public static PounceMotionPacket decode(FriendlyByteBuf buffer) {
        return new PounceMotionPacket(buffer.readVarInt(), buffer.readEnum(PounceAbility.Motion.class));
    }

    public static void handle(PounceMotionPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPouncePose.receive(packet));
        context.get().setPacketHandled(true);
    }
}
