package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import com.howlingwerewolf.client.ClientPouncePose;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PounceMotionPacket(int entityId, PounceAbility.Motion motion, Vec3 velocity) {
    public static void encode(PounceMotionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId());
        buffer.writeEnum(packet.motion());
        if (packet.motion() == PounceAbility.Motion.FLIGHT) {
            buffer.writeDouble(packet.velocity().x).writeDouble(packet.velocity().y).writeDouble(packet.velocity().z);
        }
    }

    public static PounceMotionPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        var motion = buffer.readEnum(PounceAbility.Motion.class);
        Vec3 velocity = motion == PounceAbility.Motion.FLIGHT
                ? new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()) : Vec3.ZERO;
        return new PounceMotionPacket(entityId, motion, velocity);
    }

    public static void handle(PounceMotionPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPouncePose.receive(packet));
        context.get().setPacketHandled(true);
    }
}
