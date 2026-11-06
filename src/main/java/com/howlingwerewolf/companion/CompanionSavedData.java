/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Server-wide and keyed by owner UUID, so neither player death nor a dimension unload loses followers. */
public final class CompanionSavedData extends SavedData {
    private static final String NAME = "howlingwerewolf_companions";
    private record Stored(List<CompanionRecord> companions, long pausedAt, Map<UUID, String> addresses) {}
    private static final Codec<Stored> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompanionRecord.CODEC.listOf().fieldOf("companions").forGetter(Stored::companions),
            Codec.LONG.optionalFieldOf("paused_at", -1L).forGetter(Stored::pausedAt),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING).optionalFieldOf("owner_addresses", Map.of()).forGetter(Stored::addresses)
    ).apply(instance, Stored::new));
    private final Map<UUID, CompanionRecord> records = new LinkedHashMap<>();
    // Loaded projections are transient references scoped to this server's saved data, never global statics.
    private final Map<UUID, WerewolfCompanionEntity> loaded = new HashMap<>();
    private final Set<WerewolfCompanionEntity> untracked = new HashSet<>();
    private final Set<WerewolfCompanionEntity> activeEntities = new HashSet<>();
    private final Map<UUID, Long> lastSpeech = new HashMap<>();
    private final Map<UUID, Long> lastIdleSpeech = new HashMap<>();
    private final Map<UUID, String> ownerAddresses = new HashMap<>();
    private long pausedAt = -1;
    public final Map<UUID, Long> lastRequests = new HashMap<>();
    public final Map<UUID, Long> lastActions = new HashMap<>();

    public CompanionSavedData() {}

    public static CompanionSavedData load(CompoundTag tag) {
        CompanionSavedData data = new CompanionSavedData();
        Stored stored = CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial(error ->
                HowlingWerewolf.LOGGER.error("Could not read companion records: {}", error)).orElse(null);
        if (stored == null) return data;
        stored.companions().forEach(data::attach);
        data.pausedAt = stored.pausedAt();
        stored.addresses().forEach((owner, address) -> {
            if (validAddress(address) && !address.isBlank()) data.ownerAddresses.put(owner, address.strip());
        });
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        var encoded = CODEC.encodeStart(NbtOps.INSTANCE, new Stored(List.copyOf(records.values()), pausedAt, Map.copyOf(ownerAddresses)))
                .resultOrPartial(error -> HowlingWerewolf.LOGGER.error("Could not save companion records: {}", error));
        if (encoded.isPresent() && encoded.get() instanceof CompoundTag compound) tag.merge(compound);
        return tag;
    }

    public static boolean validAddress(String value) {
        return value.codePointCount(0, value.length()) <= CompanionRules.MAX_ADDRESS_LENGTH
                && value.codePoints().noneMatch(code -> Character.isISOControl(code) || code == 0xA7);
    }
    public String address(UUID owner) { return ownerAddresses.getOrDefault(owner, ""); }
    public boolean setAddress(UUID owner, String value) {
        if (!validAddress(value)) return false;
        String address = value.strip();
        if (address.equals(address(owner))) return true;
        if (address.isEmpty()) ownerAddresses.remove(owner); else ownerAddresses.put(owner, address);
        setDirty();
        return true;
    }
    public void synchronizeAvailability(boolean enabled, long now) {
        if (!enabled && pausedAt < 0) { pausedAt = now; setDirty(); }
        else if (enabled && pausedAt >= 0) {
            long duration = Math.max(0, now - pausedAt);
            for (CompanionRecord record : records.values()) {
                if (record.mode == CompanionMode.HUNT) { record.huntStart += duration; record.huntEnd += duration; }
                if (record.huntCooldownEnd > pausedAt) record.huntCooldownEnd += duration;
            }
            pausedAt = -1;
            setDirty();
        }
    }
    public long clock(long now) { return pausedAt < 0 ? now : pausedAt; }
    public void track(WerewolfCompanionEntity entity) { activeEntities.add(entity); }
    public void untrack(WerewolfCompanionEntity entity) { activeEntities.remove(entity); untracked.remove(entity); }
    public List<WerewolfCompanionEntity> activeEntities() { return List.copyOf(activeEntities); }
    public boolean allowSpeech(UUID id, long now) {
        Long previous = lastSpeech.get(id);
        if (previous != null && now - previous < 40) return false;
        lastSpeech.put(id, now);
        return true;
    }
    public boolean allowIdleSpeech(UUID owner, long now) {
        Long previous = lastIdleSpeech.get(owner);
        if (previous != null && now - previous < 1200) return false;
        lastIdleSpeech.put(owner, now);
        return true;
    }
    public static CompanionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CompanionSavedData::load, CompanionSavedData::new, NAME);
    }
    public List<CompanionRecord> ownedBy(UUID owner) {
        return records.values().stream().filter(record -> record.belongsTo(owner)).toList();
    }
    @Nullable public CompanionRecord find(UUID id) { return records.get(id); }
    public void add(CompanionRecord record) { attach(record); setDirty(); }
    public void remove(CompanionRecord record) {
        if (records.remove(record.id, record)) {
            record.onInventoryChanged(() -> {});
            loaded.remove(record.id);
            lastSpeech.remove(record.id);
            setDirty();
        }
    }
    private void attach(CompanionRecord record) {
        if (records.putIfAbsent(record.id, record) != null) throw new IllegalStateException("Duplicate companion UUID");
        record.onInventoryChanged(this::setDirty);
    }
    @Nullable public WerewolfCompanionEntity loaded(UUID id) {
        WerewolfCompanionEntity entity = loaded.get(id);
        if (entity != null && entity.isRemoved()) { loaded.remove(id); return null; }
        return entity;
    }
    public boolean claim(CompanionRecord record, WerewolfCompanionEntity entity) {
        if (entity.isRemoved() || entity.record() != record || entity.generation() != record.generation || loaded(record.id) != null) return false;
        loaded.put(record.id, entity);
        return true;
    }
    public boolean isCurrent(CompanionRecord record, WerewolfCompanionEntity entity) {
        return record.generation == entity.generation() && loaded.get(record.id) == entity;
    }
    public void release(CompanionRecord record, WerewolfCompanionEntity entity) {
        if (isCurrent(record, entity)) loaded.remove(record.id);
    }
    public void queueUntracked(WerewolfCompanionEntity entity) { untracked.add(entity); }
    public void removeUntracked() {
        // Visibility changes iterate entity sections. Remove afterward, never from inside that iteration.
        var pending = List.copyOf(untracked);
        untracked.clear();
        for (WerewolfCompanionEntity entity : pending) {
            if (!entity.isRemoved()) { CompanionManager.remember(entity); entity.discard(); }
        }
    }
}
