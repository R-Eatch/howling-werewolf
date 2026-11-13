package com.howlingwerewolf.capability;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * Vanilla player-NBT mirror for werewolf data.
 *
 * The Fabric attachment holds live player data. This mirror supports old Forge-family saves
 * and recovery when the replacement player has only default data.
 */
public final class WerewolfPersistence {
    private static final String DATA_KEY = HowlingWerewolf.MOD_ID + "_werewolf_data_v2";

    public static void save(Player player, WerewolfData data) {
        if (player.level().isClientSide()) return;
        CompoundTag root = player.getAttachedOrCreate(ModAttachments.LEGACY);
        CompoundTag persisted = root.contains("PlayerPersisted", Tag.TAG_COMPOUND)
                ? root.getCompound("PlayerPersisted")
                : new CompoundTag();
        persisted.put(DATA_KEY, data.serializeNBT());
        root.put("PlayerPersisted", persisted);
    }

    public static boolean load(Player player, WerewolfData data) {
        CompoundTag snapshot = readSnapshot(player);
        if (snapshot == null) return false;
        data.deserializeNBT(snapshot);
        return true;
    }

    public static CompoundTag readSnapshot(Player player) {
        CompoundTag root = player.getAttachedOrCreate(ModAttachments.LEGACY);
        if (!root.contains("PlayerPersisted", Tag.TAG_COMPOUND)) return null;
        CompoundTag persisted = root.getCompound("PlayerPersisted");
        if (!persisted.contains(DATA_KEY, Tag.TAG_COMPOUND)) return null;
        return persisted.getCompound(DATA_KEY).copy();
    }

    public static void writeSnapshot(Player player, CompoundTag snapshot) {
        if (player.level().isClientSide() || snapshot == null) return;
        CompoundTag root = player.getAttachedOrCreate(ModAttachments.LEGACY);
        CompoundTag persisted = root.contains("PlayerPersisted", Tag.TAG_COMPOUND)
                ? root.getCompound("PlayerPersisted")
                : new CompoundTag();
        persisted.put(DATA_KEY, snapshot.copy());
        root.put("PlayerPersisted", persisted);
    }

    public static void copyPersistentRoot(Player original, Player replacement) {
        CompoundTag root = original.getAttachedOrCreate(ModAttachments.LEGACY);
        if (!root.contains("PlayerPersisted", Tag.TAG_COMPOUND)) return;
        replacement.getAttachedOrCreate(ModAttachments.LEGACY).put("PlayerPersisted",
                root.getCompound("PlayerPersisted").copy());
    }

    /**
     * Recover only when the freshly attached data is still at its default state.
     */
    public static boolean recoverIfFresh(Player player, WerewolfData data) {
        if (!data.isDefaultState()) {
            save(player, data);
            return false;
        }
        CompoundTag snapshot = readSnapshot(player);
        if (snapshot == null) return false;
        WerewolfData persisted = new WerewolfData();
        persisted.deserializeNBT(snapshot);
        if (persisted.isDefaultState()) return false;
        data.copyFrom(persisted);
        return true;
    }

    private WerewolfPersistence() {}
}
