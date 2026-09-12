package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requests a cosmetic change; the server validates and broadcasts the resulting state. */
public record SetWerewolfSkinPacket(String skinId) {
    public SetWerewolfSkinPacket(WerewolfSkin skin) {
        this(WerewolfSkinIds.normalize(skin.getId()));
    }

    public static void encode(SetWerewolfSkinPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.skinId, WerewolfSkinIds.MAX_ID_LENGTH);
    }

    public static SetWerewolfSkinPacket decode(FriendlyByteBuf buf) {
        return new SetWerewolfSkinPacket(buf.readUtf(WerewolfSkinIds.MAX_ID_LENGTH));
    }

    public static void handle(SetWerewolfSkinPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer sender = ctx.getSender();
        String skinId = WerewolfSkinIds.canonicalize(packet.skinId);
        // The server stores an ID only; custom textures belong to each client's resource packs.
        if (sender != null && skinId != null) {
            WerewolfApi.get(sender).ifPresent(data -> {
                if (!data.isWerewolf()) return;
                data.setSkinId(skinId);
                ModNetwork.sync(sender, data);
            });
        }
        ctx.setPacketHandled(true);
    }
}
