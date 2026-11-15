package com.howlingwerewolf.world;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class SettlementPositions {
    private static final int HUNTER_EXCLUSION = 48;
    private static final int MAX_TRIBE_RADIUS = 72 + 20;
    private static final ResourceKey<StructureSet> TRIBES = ResourceKey.create(Registries.STRUCTURE_SET,
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "werewolf_tribes"));
    private static final String SCHEDULE_PREFIX = HowlingWerewolf.MOD_ID + ":schedule/";

    /** Check native start bounds. Compact per-start caching avoids a locate search on every spawn attempt. */
    public static boolean blocksHunterSpawn(ServerLevel level, BlockPos pos) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        var set = registry.get(TRIBES).map(holder -> holder.value()).orElse(null);
        if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement spread)) return false;
        int reach = HUNTER_EXCLUSION + MAX_TRIBE_RADIUS;
        int minX = Math.floorDiv((pos.getX() - reach) >> 4, spread.spacing());
        int maxX = Math.floorDiv((pos.getX() + reach) >> 4, spread.spacing());
        int minZ = Math.floorDiv((pos.getZ() - reach) >> 4, spread.spacing());
        int maxZ = Math.floorDiv((pos.getZ() + reach) >> 4, spread.spacing());
        SettlementData data = SettlementData.get(level);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                ChunkPos start = spread.getPotentialStructureChunk(level.getSeed(), x * spread.spacing(), z * spread.spacing());
                if (Math.abs(start.getMinBlockX() - pos.getX()) > reach
                        || Math.abs(start.getMinBlockZ() - pos.getZ()) > reach) continue;
                List<BoundingBox> bounds = data.tribeBounds.computeIfAbsent(start.pack(),
                        ignored -> tribeBounds(level, start, set.structures()));
                for (BoundingBox box : bounds) {
                    int dx = Math.max(0, Math.max(box.minX() - pos.getX(), pos.getX() - box.maxX()));
                    int dz = Math.max(0, Math.max(box.minZ() - pos.getZ(), pos.getZ() - box.maxZ()));
                    if (dx * dx + dz * dz <= HUNTER_EXCLUSION * HUNTER_EXCLUSION) return true;
                }
            }
        }
        return false;
    }

    private static List<BoundingBox> tribeBounds(ServerLevel level, ChunkPos start, List<StructureSet.StructureSelectionEntry> entries) {
        var chunk = level.getChunk(start.x(), start.z(), ChunkStatus.STRUCTURE_STARTS);
        return entries.stream().map(entry -> chunk.getStartForStructure(entry.structure().value()))
                .filter(result -> result != null && result.isValid()).flatMap(result -> result.getPieces().stream())
                .map(piece -> piece.getBoundingBox()).toList();
    }

    static Map<String, List<BlockPos>> schedule(ServerLevel level, BlockPos marker, boolean tribe) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        HolderSet<Structure> allowed = tribe ? registry.get(WerewolfTribe.TAG).orElse(null)
                : registry.get(VillageWatchtower.KEY).map(HolderSet::direct).orElse(null);
        if (allowed == null) return Map.of();
        StructureManager manager = level.structureManager();
        Predicate<Structure> matches = structure -> allowed.contains(registry.wrapAsHolder(structure));
        ChunkPos chunk = ChunkPos.containing(marker);
        var starts = manager.startsForStructure(chunk.x(), chunk.z(), matches);
        Map<String, List<BlockPos>> result = new HashMap<>();
        for (StructureStart start : starts) {
            for (var piece : start.getPieces()) {
                if (!(piece instanceof PoolElementStructurePiece pool)) continue;
                for (var entry : pool.getElement().getShuffledJigsawBlocks(level.getStructureTemplateManager(), pool.getPosition(),
                        pool.getRotation(), RandomSource.create(0))) {
                    String name = entry.name().toString();
                    if (!name.startsWith(SCHEDULE_PREFIX)) continue;
                    result.computeIfAbsent(name.substring(SCHEDULE_PREFIX.length()), key -> new java.util.ArrayList<>())
                            .add(entry.pos().above());
                }
            }
        }
        result.replaceAll((key, points) -> points.stream().distinct().sorted().toList());
        return result;
    }

    public static BlockPos trainingYard(ServerLevel level, BlockPos marker) {
        SettlementData.Site site = SettlementData.get(level).siteAt(marker);
        if (site == null) return null;
        if (site.schedule == null) site.schedule = schedule(level, marker, false);
        return point(site.schedule, "warden_training", 0, null);
    }

    public record Routine(BlockPos home, BlockPos bedside, BlockPos work, BlockPos plaza, BlockPos ritual, BlockPos tower) {
        static Routine from(Map<String, List<BlockPos>> points, BlockPos site, boolean tribe, int slot) {
            if (!tribe) return new Routine(point(points, "warden_home", 0, site.above()), null,
                    point(points, "warden_work", 0, site.above()), site.above(), null, point(points, "tower_rest", 0, site.above()));
            boolean chief = slot == 0;
            String role = chief ? "chief" : "resident";
            BlockPos home = point(points, role + "_bed", chief ? 0 : slot - 1, null);
            BlockPos bedside = home == null ? null : points.getOrDefault(role + "_bedside", List.of()).stream()
                    .filter(pos -> pos.distSqr(home) <= 4)
                    .min(Comparator.comparingDouble(home::distSqr)).orElse(null);
            BlockPos plaza = point(points, "plaza", 0, site.above());
            var jobs = points.getOrDefault(chief ? "chief_work" : "resident_work", List.of());
            BlockPos work = jobs.isEmpty() ? plaza : jobs.get(chief ? 0 : (slot - 1) % jobs.size());
            return new Routine(home, bedside, work, plaza, point(points, "ritual", 0, plaza), null);
        }

        public net.minecraft.nbt.CompoundTag save() {
            var output = new net.minecraft.nbt.CompoundTag();
            savePos(output, "Home", home);
            savePos(output, "Bedside", bedside);
            savePos(output, "Work", work);
            savePos(output, "Plaza", plaza);
            savePos(output, "Ritual", ritual);
            savePos(output, "Tower", tower);
            return output;
        }

        public static Routine load(net.minecraft.nbt.CompoundTag tag, BlockPos site) {
            return new Routine(readPos(tag, "Home"), readPos(tag, "Bedside"), readPos(tag, "Work", site.above()),
                    readPos(tag, "Plaza", site.above()), readPos(tag, "Ritual"), readPos(tag, "Tower"));
        }

        private static void savePos(net.minecraft.nbt.CompoundTag tag, String key, BlockPos pos) {
            if (pos != null) tag.putLong(key, pos.asLong());
        }

        private static BlockPos readPos(net.minecraft.nbt.CompoundTag tag, String key) {
            return readPos(tag, key, null);
        }

        private static BlockPos readPos(net.minecraft.nbt.CompoundTag tag, String key, BlockPos fallback) {
            return tag.getLong(key).map(BlockPos::of).orElse(fallback);
        }
    }

    private static BlockPos point(Map<String, List<BlockPos>> points, String key, int index, BlockPos fallback) {
        return points.getOrDefault(key, List.of()).stream().skip(index).findFirst().orElse(fallback);
    }

    static Map<Long, List<BoundingBox>> boundsCache() {
        return new LinkedHashMap<>(64, 0.75F, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Long, List<BoundingBox>> eldest) { return size() > 512; }
        };
    }

    public static boolean isDay(net.minecraft.world.level.Level level) {
        long time = Math.floorMod(level.getOverworldClockTime(), SettlementData.DAY);
        return time < 13000 || time >= 23000;
    }

    private SettlementPositions() {}
}
