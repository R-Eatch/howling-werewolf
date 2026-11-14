package com.howlingwerewolf.world;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VillageWatchtower extends Structure {
    public static final ResourceKey<Structure> KEY = ResourceKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "hunter_watchtower"));
    public static final MapCodec<VillageWatchtower> CODEC = simpleCodec(VillageWatchtower::new);

    private static final int[] DISTANCES = {30, 38, 46};
    private static final BlockPos TEMPLATE_CENTER = new BlockPos(6, 0, 6);
    private static final List<Module> MODULES = List.of(new Module("watchtower", BlockPos.ZERO),
            new Module("armory", new BlockPos(13, 0, 0)), new Module("training_yard", new BlockPos(-11, 0, 0)));

    public VillageWatchtower(StructureSettings settings) { super(settings); }

    @Override public StructureType<?> type() { return ModStructures.HUNTER_WATCHTOWER.get(); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var villageSet = context.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(BuiltinStructureSets.VILLAGES);
        if (villageSet == null || !(villageSet.placement() instanceof RandomSpreadStructurePlacement placement)
                || !placement.getPotentialStructureChunk(context.seed(), context.chunkPos().x, context.chunkPos().z).equals(context.chunkPos())) {
            return Optional.empty();
        }

        RandomSource towerRandom = RandomSource.create(context.seed() ^ context.chunkPos().toLong() ^ 0x57385332L);
        if (towerRandom.nextInt(4) == 0) return Optional.empty();
        return findVillage(context, villageSet).flatMap(village -> findTower(context, village, towerRandom));
    }

    /** Follow the village structure set's weighted draw, including its fallback on invalid biomes. */
    private static Optional<Village> findVillage(GenerationContext context, StructureSet villageSet) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0));
        random.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        var entries = new ArrayList<>(villageSet.structures());
        int totalWeight = entries.stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();

        while (!entries.isEmpty()) {
            int draw = random.nextInt(totalWeight);
            int index = 0;
            while ((draw -= entries.get(index).weight()) >= 0) index++;
            var entry = entries.remove(index);
            totalWeight -= entry.weight();
            var structure = entry.structure().value();
            var villageContext = new GenerationContext(context.registryAccess(), context.chunkGenerator(), context.biomeSource(),
                    context.randomState(), context.structureTemplateManager(), context.seed(), context.chunkPos(),
                    context.heightAccessor(), structure.biomes()::contains);
            var start = structure.findValidGenerationPoint(villageContext);
            if (start.isPresent()) {
                var id = entry.structure().unwrapKey().orElseThrow().location();
                if (!id.getNamespace().equals("minecraft")) return Optional.empty();
                return Optional.of(new Village(start.get(), id.getPath()));
            }
        }
        return Optional.empty();
    }

    private static Optional<GenerationStub> findTower(GenerationContext context, Village village, RandomSource random) {
        String variant = switch (village.type()) {
            case "village_taiga", "village_snowy" -> "taiga";
            case "village_desert" -> "desert";
            case "village_savanna" -> "savanna";
            default -> "plains";
        };
        var pieces = village.start().getPiecesBuilder();
        var elements = MODULES.stream().map(module -> StructurePoolElement.single(
                HowlingWerewolf.MOD_ID + ":hunter/" + module.template() + "/" + variant).apply(StructureTemplatePool.Projection.RIGID)).toList();
        BlockPos center = village.start().position();
        int offset = random.nextInt(16);
        for (int radius : DISTANCES) {
            for (int i = 0; i < 16; i++) {
                double angle = (i + offset) * (Math.PI / 8);
                int x = center.getX() + (int) Math.round(radius * Math.cos(angle));
                int z = center.getZ() + (int) Math.round(radius * Math.sin(angle));
                int y = height(context, Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (!fitsTerrain(context, x, y, z, center.getY())) continue;
                Rotation rotation = Rotation.values()[Math.floorMod((int) Math.round((i + offset) / 4.0) + 3, 4)];
                BlockPos origin = new BlockPos(x, y - 4, z).subtract(TEMPLATE_CENTER.rotate(rotation));
                var modules = new ArrayList<PoolElementStructurePiece>(MODULES.size());
                var templates = context.structureTemplateManager();
                for (int j = 0; j < MODULES.size(); j++) {
                    var element = elements.get(j);
                    BlockPos moduleOrigin = origin.offset(MODULES.get(j).offset().rotate(rotation));
                    BoundingBox box = element.getBoundingBox(templates, moduleOrigin, rotation);
                    BoundingBox footprint = new BoundingBox(box.minX() - 1, context.heightAccessor().getMinBuildHeight(), box.minZ() - 1,
                            box.maxX() + 1, context.heightAccessor().getMaxBuildHeight(), box.maxZ() + 1);
                    if (pieces.findCollisionPiece(footprint) != null || (j != 0 && !fitsAnnex(context, box, y))) break;
                    modules.add(new PoolElementStructurePiece(templates, element, moduleOrigin, 4, rotation, box, LiquidSettings.IGNORE_WATERLOGGING));
                }
                if (modules.size() == MODULES.size()) return Optional.of(new GenerationStub(center, builder -> modules.forEach(builder::addPiece)));
            }
        }
        return Optional.empty();
    }

    private static boolean fitsTerrain(GenerationContext context, int x, int y, int z, int villageY) {
        if (Math.abs(y - villageY) > 6) return false;
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int dz = -5; dz <= 5; dz += 5) {
                int surface = height(context, Heightmap.Types.WORLD_SURFACE_WG, x + dx, z + dz);
                if (surface < y - 2 || surface > y + 3
                        || surface != height(context, Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz)) return false;
            }
        }
        return true;
    }

    private static int height(GenerationContext context, Heightmap.Types type, int x, int z) {
        return context.chunkGenerator().getBaseHeight(x, z, type, context.heightAccessor(), context.randomState());
    }

    private static boolean fitsAnnex(GenerationContext context, BoundingBox box, int floor) {
        for (int x : new int[]{box.minX() + 1, (box.minX() + box.maxX()) / 2, box.maxX() - 1}) {
            for (int z : new int[]{box.minZ() + 1, (box.minZ() + box.maxZ()) / 2, box.maxZ() - 1}) {
                int surface = height(context, Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (surface < floor - 3 || surface > floor + 3
                        || surface != height(context, Heightmap.Types.OCEAN_FLOOR_WG, x, z)) return false;
            }
        }
        return true;
    }

    private record Village(GenerationStub start, String type) {}
    private record Module(String template, BlockPos offset) {}
}
