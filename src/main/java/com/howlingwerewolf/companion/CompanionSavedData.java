/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/** Server-wide and keyed by owner UUID, so neither player death nor a dimension unload loses followers. */
public final class CompanionSavedData extends SavedData {
    public static final Codec<CompanionSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompanionRecord.CODEC.listOf().fieldOf("companions").forGetter(data -> List.copyOf(data.records.values())),
            Codec.LONG.optionalFieldOf("paused_at", -1L).forGetter(data -> data.pausedAt),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING).optionalFieldOf("owner_addresses", Map.of())
                    .forGetter(data -> Map.copyOf(data.ownerAddresses)),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.unboundedMap(Codec.STRING, Codec.STRING))
                    .optionalFieldOf("owner_visits", Map.of()).forGetter(data -> Map.copyOf(data.ownerVisits))
    ).apply(instance, CompanionSavedData::new));
    private static final SavedDataType<CompanionSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("howlingwerewolf", "companions"), CompanionSavedData::new, CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private final Map<UUID, CompanionRecord> records = new LinkedHashMap<>();
    private final Map<UUID, List<CompanionRecord>> recordsByOwner = new HashMap<>();
    private final Map<UUID, Map<String, String>> ownerVisits = new HashMap<>();
    private final Map<UUID, CompanionContextDialogue> contextDialogues = new HashMap<>();
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
    private CompanionSavedData(List<CompanionRecord> entries, long pausedAt, Map<UUID, String> addresses,
                               Map<UUID, Map<String, String>> visits) {
        entries.forEach(this::attach);
        this.pausedAt = pausedAt;
        ownerVisits.putAll(visits);
        addresses.forEach((owner, address) -> {
            if (validAddress(address) && !address.isBlank()) ownerAddresses.put(owner, address.strip());
        });
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
        var storage = server.getDataStorage();
        CompanionSavedData current = storage.get(TYPE);
        if (current != null) return current;
        var legacy = server.getWorldPath(LevelResource.DATA).resolve("howlingwerewolf_companions.dat");
        if (Files.isRegularFile(legacy)) {
            try {
                var root = NbtIo.readCompressed(legacy, NbtAccounter.unlimitedHeap());
                var migrated = CODEC.parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        root.getCompoundOrEmpty("data")).resultOrPartial(error ->
                        HowlingWerewolf.LOGGER.error("Could not read old companion records: {}", error));
                if (migrated.isPresent()) {
                    storage.set(TYPE, migrated.get());
                    return migrated.get();
                }
            } catch (IOException exception) {
                HowlingWerewolf.LOGGER.warn("Could not import old companion records {}", legacy, exception);
            }
        }
        return storage.computeIfAbsent(TYPE);
    }
    void tickDialogue(ServerPlayer player, ServerLevel level, List<CompanionRecord> roster,
                      long now, boolean readAmbient) {
        UUID owner = player.getUUID();
        if (roster.isEmpty() && !ownerVisits.containsKey(owner)) {
            contextDialogues.remove(owner);
            return;
        }
        contextDialogues.computeIfAbsent(owner, id -> new CompanionContextDialogue())
                .tick(player, level, this, roster, now, readAmbient);
    }

    Map<String, String> updateVisits(UUID owner, Map<String, String> current) {
        Map<String, String> previous = ownerVisits.getOrDefault(owner, Map.of());
        if (!current.equals(previous)) {
            if (current.isEmpty()) ownerVisits.remove(owner);
            else ownerVisits.put(owner, Map.copyOf(current));
            setDirty();
        }
        return previous;
    }

    public List<CompanionRecord> ownedBy(UUID owner) {
        return recordsByOwner.getOrDefault(owner, List.of());
    }
    @Nullable public CompanionRecord find(UUID id) { return records.get(id); }
    public void add(CompanionRecord record) { attach(record); setDirty(); }
    public void remove(CompanionRecord record) {
        if (records.remove(record.id, record)) {
            record.onInventoryChanged(() -> {});
            loaded.remove(record.id);
            lastSpeech.remove(record.id);
            var remaining = ownedBy(record.owner).stream().filter(other -> other != record).toList();
            if (remaining.isEmpty()) recordsByOwner.remove(record.owner);
            else recordsByOwner.put(record.owner, remaining);
            setDirty();
        }
    }
    private void attach(CompanionRecord record) {
        if (records.putIfAbsent(record.id, record) != null) throw new IllegalStateException("Duplicate companion UUID");
        var owned = new ArrayList<>(ownedBy(record.owner));
        owned.add(record);
        recordsByOwner.put(record.owner, List.copyOf(owned));
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
