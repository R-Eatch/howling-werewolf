package com.howlingwerewolf.capability;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Vanilla player-NBT mirror for werewolf data.
 *
 * The NeoForge attachment remains the live source of truth while the player entity exists.
 * This mirror exists so that a replacement player created by death or dimension travel can
 * recover after the original player has been replaced by the entity lifecycle.
 */
public final class WerewolfPersistence {
    private static final String DATA_KEY = HowlingWerewolf.MOD_ID + "_werewolf_data_v2";

    public static void save(Player player, WerewolfData data) {
        if (player.level().isClientSide()) return;
        CompoundTag root = player.getAttachedOrCreate(ModAttachments.LEGACY);
        CompoundTag persisted = root.contains("PlayerPersisted")
                ? root.getCompoundOrEmpty("PlayerPersisted")
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
        if (!root.contains("PlayerPersisted")) return null;
        CompoundTag persisted = root.getCompoundOrEmpty("PlayerPersisted");
        if (!persisted.contains(DATA_KEY)) return null;
        return persisted.getCompoundOrEmpty(DATA_KEY).copy();
    }

    public static void writeSnapshot(Player player, CompoundTag snapshot) {
        if (player.level().isClientSide() || snapshot == null) return;
        CompoundTag root = player.getAttachedOrCreate(ModAttachments.LEGACY);
        CompoundTag persisted = root.contains("PlayerPersisted")
                ? root.getCompoundOrEmpty("PlayerPersisted")
                : new CompoundTag();
        persisted.put(DATA_KEY, snapshot.copy());
        root.put("PlayerPersisted", persisted);
    }

    public static void copyPersistentRoot(Player original, Player replacement) {
        CompoundTag root = original.getAttachedOrCreate(ModAttachments.LEGACY);
        if (!root.contains("PlayerPersisted")) return;
        replacement.getAttachedOrCreate(ModAttachments.LEGACY).put("PlayerPersisted",
                root.getCompoundOrEmpty("PlayerPersisted").copy());
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
