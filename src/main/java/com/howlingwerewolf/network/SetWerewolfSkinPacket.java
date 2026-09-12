package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requests a cosmetic change; the server validates and broadcasts the resulting state. */
public record SetWerewolfSkinPacket(String skinId) {
    private static final int MAX_ID_LENGTH = 16;

    public SetWerewolfSkinPacket(WerewolfSkin skin) {
        this(skin.getId());
    }

    public static void encode(SetWerewolfSkinPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.skinId, MAX_ID_LENGTH);
    }

    public static SetWerewolfSkinPacket decode(FriendlyByteBuf buf) {
        return new SetWerewolfSkinPacket(buf.readUtf(MAX_ID_LENGTH));
    }

    public static void handle(SetWerewolfSkinPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer sender = ctx.getSender();
        WerewolfSkin skin = WerewolfSkin.byId(packet.skinId);
        // Save loading has a fallback; a request must match a registered ID exactly.
        if (sender != null && skin.getId().equals(packet.skinId)) {
            WerewolfApi.get(sender).ifPresent(data -> {
                if (!data.isWerewolf()) return;
                data.setSkin(skin);
                ModNetwork.sync(sender, data);
            });
        }
        ctx.setPacketHandled(true);
    }
}
