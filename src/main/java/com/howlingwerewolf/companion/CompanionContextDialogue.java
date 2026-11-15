/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.world.WerewolfTribe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Tracks the player's whereabouts even while companions are catching up or being rebuilt. */
final class CompanionContextDialogue {
    private static final String[] AMBIENT = {"player_low_hp", "player_beast", "full_moon", "sunrise", "rain", "water"};
    private BlockPos lastPosition;
    private ServerLevel lastLevel;
    private final Set<String> pendingEntries = new LinkedHashSet<>();
    private int previousAmbient;
    private long nextAmbient;

    void tick(ServerPlayer player, ServerLevel level, CompanionSavedData data, List<CompanionRecord> records,
              long now, boolean readAmbient) {
        BlockPos pos = player.blockPosition();
        if (lastLevel != level || !pos.equals(lastPosition)) {
            if (!level.hasChunkAt(pos)) return;
            lastLevel = level;
            lastPosition = pos.immutable();
            Map<String, String> current = entriesAt(level, pos);
            Map<String, String> previous = data.updateVisits(player.getUUID(), current);
            pendingEntries.removeIf(scene -> !Objects.equals(previous.get(scene), current.get(scene)));
            if (!current.equals(previous) && records.stream().anyMatch(CompanionContextDialogue::travelsWithPlayer)) {
                current.forEach((scene, site) -> {
                    if (!site.equals(previous.get(scene))) pendingEntries.add(scene);
                });
            }
        }

        int changedAmbient = readAmbient ? sampleAmbient(player, level) : 0;
        if (player.containerMenu != player.inventoryMenu) return;
        if (!pendingEntries.isEmpty()) {
            if (records.stream().noneMatch(CompanionContextDialogue::travelsWithPlayer)) pendingEntries.clear();
            var speaker = findSpeaker(player, data, records, true, false);
            if (speaker != null) {
                for (String scene : pendingEntries) CompanionDialogue.sendLine(player, speaker.record(), scene, data);
                pendingEntries.clear();
                speaker.deferIdleDialogue();
                nextAmbient = now + 1200;
            }
        }
        if (changedAmbient == 0 || now < nextAmbient) return;
        var speaker = findSpeaker(player, data, records, false, (changedAmbient & 1) != 0);
        if (speaker != null) {
            nextAmbient = now + 1200;
            speaker.deferIdleDialogue();
            speaker.sayTo(player, AMBIENT[Integer.numberOfTrailingZeros(changedAmbient)]);
        }
    }

    private static Map<String, String> entriesAt(ServerLevel level, BlockPos pos) {
        Map<String, String> scenes = new LinkedHashMap<>();
        String dimension = level.dimension().location().toString();
        if (level.dimension().equals(Level.NETHER)) scenes.put("biome.nether", dimension);
        if (level.dimension().equals(Level.END)) scenes.put("biome.end", dimension);
        var biome = level.getBiome(pos);
        if (biome.is(Biomes.TAIGA) || biome.is(Biomes.SNOWY_TAIGA)
                || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA) || biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA)) {
            scenes.put("biome.taiga", dimension);
        }
        // Whole structure bounds include the paths between buildings. Keep each site's identity across teleports.
        addStructure(scenes, "location.village", level, pos, StructureTags.VILLAGE, dimension);
        addStructure(scenes, "location.tribe", level, pos, WerewolfTribe.TAG, dimension);
        return scenes;
    }

    private static void addStructure(Map<String, String> scenes, String scene, ServerLevel level,
                                     BlockPos pos, TagKey<Structure> tag, String dimension) {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var structures = registry.getTag(tag).orElse(null);
        if (structures == null) return;
        var manager = level.structureManager();
        for (Structure structure : manager.getAllStructuresAt(pos).keySet()) {
            if (!structures.contains(registry.wrapAsHolder(structure))) continue;
            var start = manager.getStructureAt(pos, structure);
            if (!start.isValid()) continue;
            scenes.put(scene, dimension + "/" + start.getChunkPos().toLong());
            break;
        }
    }

    private int sampleAmbient(ServerPlayer player, ServerLevel level) {
        long hour = Math.floorMod(level.getDayTime(), 24000);
        boolean overworld = level.dimension().equals(Level.OVERWORLD);
        int current = 0;
        if (player.getHealth() <= player.getMaxHealth() * 0.25F) current |= 1;
        if (WerewolfApi.get(player).map(data -> data.isBeastMode()).orElse(false)) current |= 1 << 1;
        if (overworld && hour >= 13000 && hour < 23000 && level.getMoonPhase() == 0) current |= 1 << 2;
        if (overworld && hour < 900) current |= 1 << 3;
        if (overworld && level.isRainingAt(player.blockPosition())) current |= 1 << 4;
        if (player.isInWater()) current |= 1 << 5;
        int changed = current & ~previousAmbient;
        previousAmbient = current;
        return changed;
    }

    private static boolean travelsWithPlayer(CompanionRecord record) {
        return record.mode == CompanionMode.FOLLOW || record.mode == CompanionMode.GUARD;
    }

    @Nullable
    private static WerewolfCompanionEntity findSpeaker(ServerPlayer player, CompanionSavedData data,
                                                        List<CompanionRecord> records, boolean travelingOnly,
                                                        boolean canSpeakInCombat) {
        WerewolfCompanionEntity speaker = null;
        double nearest = 12 * 12;
        for (CompanionRecord record : records) {
            if (!record.mode.hasEntity() || travelingOnly && !travelsWithPlayer(record)) continue;
            var entity = data.loaded(record.id);
            if (entity == null || !entity.isAlive() || entity.level() != player.level()
                    || !canSpeakInCombat && entity.getTarget() != null) continue;
            double distance = entity.distanceToSqr(player);
            if (distance <= nearest) {
                speaker = entity;
                nearest = distance;
            }
        }
        return speaker;
    }
}
