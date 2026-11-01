package com.howlingwerewolf.world;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Records patrol targets, initial completion, and replacement cooldowns per village structure.
 * / 按村庄结构记录巡猎队目标数量、首次完成状态与补员冷却。
 */
public final class VillageHunterSavedData extends SavedData {
    private static final String DATA_NAME = "howlingwerewolf_village_hunters";
    private static final Codec<VillageHunterSavedData> CODEC = CompoundTag.CODEC.xmap(
            VillageHunterSavedData::load, VillageHunterSavedData::save);
    private static final SavedDataType<VillageHunterSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("howlingwerewolf", "village_hunters"),
            VillageHunterSavedData::new, CODEC, null);
    private final Map<Long, Integer> patrolTargets = new HashMap<>();
    private final Set<Long> completedVillages = new HashSet<>();
    private final Map<Long, Long> nextReplenishmentTicks = new HashMap<>();

    public static VillageHunterSavedData get(ServerLevel level) {
        VillageHunterSavedData existing = level.getDataStorage().get(TYPE);
        if (existing != null) return existing;
        // 26.1 moved saved data into namespace directories. Import the 1.1.3 file
        // once so existing village patrol targets and replenishment cooldowns survive.
        java.nio.file.Path legacy = level.getServer().getWorldPath(
                net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("data").resolve(DATA_NAME + ".dat");
        if (java.nio.file.Files.isRegularFile(legacy)) {
            try {
                CompoundTag root = net.minecraft.nbt.NbtIo.readCompressed(legacy,
                        net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                VillageHunterSavedData migrated = load(root.getCompoundOrEmpty("data"));
                migrated.setDirty();
                level.getDataStorage().set(TYPE, migrated);
                return migrated;
            } catch (java.io.IOException exception) {
                com.howlingwerewolf.HowlingWerewolf.LOGGER.warn(
                        "Could not import legacy village hunter data {}", legacy, exception);
            }
        }
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static VillageHunterSavedData load(CompoundTag tag) {
        VillageHunterSavedData data = new VillageHunterSavedData();
        ListTag entries = tag.getListOrEmpty("Villages");
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompoundOrEmpty(i);
            long key = entry.getLongOr("Key", 0L);
            data.patrolTargets.put(key, entry.getIntOr("Target", 0));
            if (entry.getBooleanOr("Complete", false)) data.completedVillages.add(key);
            if (entry.getLong("NextReplenishmentTick").isPresent()) {
                long nextTick = entry.getLongOr("NextReplenishmentTick", 0L);
                if (nextTick > 0L) data.nextReplenishmentTicks.put(key, nextTick);
            }
        }
        return data;
    }

    public int getOrCreateTarget(long villageKey, net.minecraft.util.RandomSource random) {
        Integer target = patrolTargets.get(villageKey);
        if (target != null) return target;
        int created = 4 + random.nextInt(3);
        patrolTargets.put(villageKey, created);
        setDirty();
        return created;
    }

    public boolean isComplete(long villageKey) {
        return completedVillages.contains(villageKey);
    }

    public void markComplete(long villageKey) {
        if (completedVillages.add(villageKey)) setDirty();
    }

    public long getNextReplenishmentTick(long villageKey) {
        return nextReplenishmentTicks.getOrDefault(villageKey, 0L);
    }

    public void scheduleReplenishment(long villageKey, long gameTick) {
        nextReplenishmentTicks.put(villageKey, gameTick);
        setDirty();
    }

    public void clearReplenishment(long villageKey) {
        if (nextReplenishmentTicks.remove(villageKey) != null) setDirty();
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for (Map.Entry<Long, Integer> village : patrolTargets.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Key", village.getKey());
            entry.putInt("Target", village.getValue());
            entry.putBoolean("Complete", completedVillages.contains(village.getKey()));
            Long nextTick = nextReplenishmentTicks.get(village.getKey());
            if (nextTick != null && nextTick > 0L) {
                entry.putLong("NextReplenishmentTick", nextTick);
            }
            entries.add(entry);
        }
        tag.put("Villages", entries);
        return tag;
    }
}
