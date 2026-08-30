package com.howlingwerewolf.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private static final Factory<VillageHunterSavedData> FACTORY = new Factory<>(
            VillageHunterSavedData::new, VillageHunterSavedData::load);
    private final Map<Long, Integer> patrolTargets = new HashMap<>();
    private final Set<Long> completedVillages = new HashSet<>();
    private final Map<Long, Long> nextReplenishmentTicks = new HashMap<>();

    public static VillageHunterSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static VillageHunterSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        VillageHunterSavedData data = new VillageHunterSavedData();
        ListTag entries = tag.getList("Villages", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            long key = entry.getLong("Key");
            data.patrolTargets.put(key, entry.getInt("Target"));
            if (entry.getBoolean("Complete")) data.completedVillages.add(key);
            if (entry.contains("NextReplenishmentTick", Tag.TAG_LONG)) {
                long nextTick = entry.getLong("NextReplenishmentTick");
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
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
