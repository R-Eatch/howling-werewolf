package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Requests a cosmetic change; the server validates and broadcasts the resulting state. */
public record SetWerewolfSkinPacket(String modelSetId, String skinId) implements CustomPacketPayload {
    public static final Type<SetWerewolfSkinPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "set_werewolf_skin"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetWerewolfSkinPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.modelSetId(), 16);
                buf.writeUtf(packet.skinId(), WerewolfSkinIds.MAX_ID_LENGTH);
            },
            buf -> new SetWerewolfSkinPacket(buf.readUtf(16), buf.readUtf(WerewolfSkinIds.MAX_ID_LENGTH)));

    public SetWerewolfSkinPacket(WerewolfSkin skin) {
        this(WerewolfModelSet.CLASSIC.id(), WerewolfSkinIds.normalize(skin.getId()));
    }

    @Override
    public Type<SetWerewolfSkinPacket> type() {
        return TYPE;
    }

    /** Registered on the default MAIN handler thread by ModNetwork. */
    public static void handle(SetWerewolfSkinPacket packet, ServerPlayNetworking.Context context) {
        String skinId = WerewolfSkinIds.canonicalize(packet.skinId());
        WerewolfModelSet modelSet = WerewolfModelSet.parse(packet.modelSetId());
        // The server stores an ID only; custom textures belong to each client's resource packs.
        ServerPlayer sender = context.player();
        if (modelSet != null
                && WerewolfSkinIds.matchesModelSet(skinId, modelSet)) {
            WerewolfApi.get(sender).ifPresent(data -> {
                if (!data.isWerewolf()) return;
                data.setAppearance(modelSet, skinId);
                sender.refreshDimensions();
                ModNetwork.sync(sender, data);
            });
        }
    }
}
