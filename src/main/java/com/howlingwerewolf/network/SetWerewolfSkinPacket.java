package com.howlingwerewolf.network;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Requests a cosmetic change; the server validates and broadcasts the resulting state. */
public record SetWerewolfSkinPacket(String skinId) implements CustomPacketPayload {
    public static final Type<SetWerewolfSkinPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "set_werewolf_skin"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetWerewolfSkinPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.skinId(), WerewolfSkinIds.MAX_ID_LENGTH),
            buf -> new SetWerewolfSkinPacket(buf.readUtf(WerewolfSkinIds.MAX_ID_LENGTH)));

    public SetWerewolfSkinPacket(WerewolfSkin skin) {
        this(WerewolfSkinIds.normalize(skin.getId()));
    }

    @Override
    public Type<SetWerewolfSkinPacket> type() {
        return TYPE;
    }

    /** Registered on the default MAIN handler thread by ModNetwork. */
    public static void handle(SetWerewolfSkinPacket packet, IPayloadContext context) {
        String skinId = WerewolfSkinIds.canonicalize(packet.skinId());
        // The server stores an ID only; custom textures belong to each client's resource packs.
        if (context.player() instanceof ServerPlayer sender && skinId != null) {
            WerewolfApi.get(sender).ifPresent(data -> {
                if (!data.isWerewolf()) return;
                data.setSkinId(skinId);
                ModNetwork.sync(sender, data);
            });
        }
    }
}
