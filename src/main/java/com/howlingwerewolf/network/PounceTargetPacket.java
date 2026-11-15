package com.howlingwerewolf.network;

import com.howlingwerewolf.ability.PounceAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PounceTargetPacket(Vec3 position) {
    public static void encode(PounceTargetPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.position.x).writeDouble(packet.position.y).writeDouble(packet.position.z);
    }

    public static PounceTargetPacket decode(FriendlyByteBuf buf) {
        return new PounceTargetPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public static void handle(PounceTargetPacket packet, Supplier<NetworkEvent.Context> context) {
        var sender = context.get().getSender();
        if (sender != null) PounceAbility.start(sender, packet.position);
        context.get().setPacketHandled(true);
    }
}
