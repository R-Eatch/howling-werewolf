package com.howlingwerewolf.capability;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * Vanilla player-NBT mirror for werewolf data.
 *
 * The Forge capability remains the live source of truth while the player entity exists.
 * This mirror exists so that a replacement player created by death or dimension travel can
 * recover even if the old capability has already been invalidated by the entity lifecycle.
 */
public final class WerewolfPersistence {
    private static final String DATA_KEY = HowlingWerewolf.MOD_ID + "_werewolf_data_v2";

    public static void save(Player player, WerewolfData data) {
        if (player.level().isClientSide) return;
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(Player.PERSISTED_NBT_TAG)
                : new CompoundTag();
        persisted.put(DATA_KEY, data.serializeNBT());
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static boolean load(Player player, WerewolfData data) {
        CompoundTag snapshot = readSnapshot(player);
        if (snapshot == null) return false;
        data.deserializeNBT(snapshot);
        return true;
    }

    public static CompoundTag readSnapshot(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return null;
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(DATA_KEY, Tag.TAG_COMPOUND)) return null;
        return persisted.getCompound(DATA_KEY).copy();
    }

    public static void writeSnapshot(Player player, CompoundTag snapshot) {
        if (player.level().isClientSide || snapshot == null) return;
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(Player.PERSISTED_NBT_TAG)
                : new CompoundTag();
        persisted.put(DATA_KEY, snapshot.copy());
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void copyPersistentRoot(Player original, Player replacement) {
        CompoundTag root = original.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return;
        replacement.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                root.getCompound(Player.PERSISTED_NBT_TAG).copy());
    }

    /**
     * Recover only when the freshly attached capability is still at its default state.
     * This prevents an old mirror from rolling back a live non-default capability.
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
