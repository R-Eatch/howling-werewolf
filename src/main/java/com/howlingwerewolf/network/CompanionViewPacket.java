/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.network;

import com.howlingwerewolf.client.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Private owner-only summaries. Inventory stacks travel through the native container protocol. */
public record CompanionViewPacket(CompoundTag data, boolean open) {
    public static void encode(CompanionViewPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.data);
        buf.writeBoolean(packet.open);
    }
    public static CompanionViewPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new CompanionViewPacket(tag == null ? new CompoundTag() : tag, buf.readBoolean());
    }
    public static void handle(CompanionViewPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.companionView(packet));
        context.get().setPacketHandled(true);
    }
}
