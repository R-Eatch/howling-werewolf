package com.howlingwerewolf.network;

import com.howlingwerewolf.client.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncWerewolfDataPacket(int entityId, CompoundTag data) {
    public static void encode(SyncWerewolfDataPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeNbt(packet.data);
    }

    public static SyncWerewolfDataPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        CompoundTag tag = buf.readNbt();
        return new SyncWerewolfDataPacket(entityId, tag == null ? new CompoundTag() : tag);
    }

    public static void handle(SyncWerewolfDataPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.applySync(packet));
        context.get().setPacketHandled(true);
    }
}
