package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.network.SyncWerewolfDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientPacketHandlers {
    /**
     * Dimension/respawn packets can arrive while the client is between LocalPlayer instances.
     * Keep those authoritative snapshots briefly instead of silently discarding them.
     */
    private static final Map<Integer, CompoundTag> PENDING_SYNCS = new HashMap<>();

    public static void applySync(SyncWerewolfDataPacket packet) {
        if (!tryApply(packet.entityId(), packet.data())) {
            PENDING_SYNCS.put(packet.entityId(), packet.data().copy());
        }
    }

    public static void clearPendingSyncs() {
        PENDING_SYNCS.clear();
    }

    public static void applyPendingSyncs() {
        if (PENDING_SYNCS.isEmpty()) return;
        Iterator<Map.Entry<Integer, CompoundTag>> iterator = PENDING_SYNCS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, CompoundTag> entry = iterator.next();
            if (tryApply(entry.getKey(), entry.getValue())) iterator.remove();
        }
        // A stale remote-player entity id should not grow the map forever.
        if (PENDING_SYNCS.size() > 64) {
            HowlingWerewolf.LOGGER.warn("Clearing {} stale client werewolf sync snapshots", PENDING_SYNCS.size());
            PENDING_SYNCS.clear();
        }
    }

    private static boolean tryApply(int entityId, CompoundTag tag) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return false;
        Entity entity = minecraft.player != null && minecraft.player.getId() == entityId
                ? minecraft.player : minecraft.level.getEntity(entityId);
        if (!(entity instanceof Player player)) return false;

        final boolean[] applied = {false};
        WerewolfApi.get(player).ifPresent(data -> {
            data.deserializeNBT(tag);
            player.refreshDimensions();
            applied[0] = true;
        });
        return applied[0];
    }

    public static void showBadgeActivation() {
        Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get()));
    }

    private ClientPacketHandlers() {}
}
