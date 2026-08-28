package com.howlingwerewolf.network;

import com.howlingwerewolf.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class BadgeActivationPacket {
    public static void encode(BadgeActivationPacket packet, FriendlyByteBuf buf) {}
    public static BadgeActivationPacket decode(FriendlyByteBuf buf) { return new BadgeActivationPacket(); }

    public static void handle(BadgeActivationPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandlers::showBadgeActivation);
        context.get().setPacketHandled(true);
    }
}
