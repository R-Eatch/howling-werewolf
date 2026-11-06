package com.howlingwerewolf.network;

import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requests a cosmetic change; the server validates and broadcasts the resulting state. */
public record SetWerewolfSkinPacket(String modelSetId, String skinId, boolean showEquipment) {
    public SetWerewolfSkinPacket(String modelSetId, String skinId) {
        this(modelSetId, skinId, true);
    }
    public SetWerewolfSkinPacket(WerewolfSkin skin) {
        this(WerewolfModelSet.CLASSIC.id(), WerewolfSkinIds.normalize(skin.getId()));
    }

    public static void encode(SetWerewolfSkinPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.modelSetId, 16);
        buf.writeUtf(packet.skinId, WerewolfSkinIds.MAX_ID_LENGTH);
        buf.writeBoolean(packet.showEquipment);
    }

    public static SetWerewolfSkinPacket decode(FriendlyByteBuf buf) {
        return new SetWerewolfSkinPacket(buf.readUtf(16), buf.readUtf(WerewolfSkinIds.MAX_ID_LENGTH), buf.readBoolean());
    }

    public static void handle(SetWerewolfSkinPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer sender = ctx.getSender();
        String skinId = WerewolfSkinIds.canonicalize(packet.skinId);
        // The server stores an ID only; custom textures belong to each client's resource packs.
        WerewolfModelSet modelSet = WerewolfModelSet.parse(packet.modelSetId);
        if (sender != null && modelSet != null && WerewolfSkinIds.matchesModelSet(skinId, modelSet)) {
            WerewolfApi.get(sender).ifPresent(data -> {
                if (!data.isWerewolf()) return;
                data.setAppearance(modelSet, skinId);
                data.setEquipmentShown(packet.showEquipment);
                sender.refreshDimensions();
                ModNetwork.sync(sender, data);
            });
        }
        ctx.setPacketHandled(true);
    }
}
