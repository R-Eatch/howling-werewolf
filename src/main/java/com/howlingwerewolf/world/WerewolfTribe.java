package com.howlingwerewolf.world;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModStructures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.List;
import java.util.Optional;

public final class WerewolfTribe extends Structure {
    public static final TagKey<Structure> TAG = TagKey.create(Registries.STRUCTURE,
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "werewolf_tribes"));
    public static final MapCodec<WerewolfTribe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance), StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(tribe -> tribe.startPool)
    ).apply(instance, WerewolfTribe::new));

    private static final int CLEARANCE = 5;
    private static final int CLEAR_HEIGHT = 24;
    private final Holder<StructureTemplatePool> startPool;

    private WerewolfTribe(StructureSettings settings, Holder<StructureTemplatePool> startPool) {
        super(settings);
        this.startPool = startPool;
    }

    @Override public StructureType<?> type() { return ModStructures.WEREWOLF_TRIBE.get(); }

    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // Native pool pieces have a ground delta of 1. Recess the bottom two foundation layers.
        BlockPos origin = context.chunkPos().getWorldPosition().below(2);
        return JigsawPlacement.addPieces(context, startPool, Optional.empty(), 1, origin, false,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG), new net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.MaxDistance(72, 72), PoolAliasLookup.EMPTY,
                DimensionPadding.ZERO, LiquidSettings.IGNORE_WATERLOGGING);
    }

    @Override public void afterPlace(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                                     RandomSource random, BoundingBox chunk, ChunkPos chunkPos, PiecesContainer container) {
        List<BoundingBox> footprints = container.pieces().stream().map(StructurePiece::getBoundingBox).toList();
        int floor = footprints.getFirst().minY() + 3;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = chunk.minX(); x <= chunk.maxX(); x++) {
            for (int z = chunk.minZ(); z <= chunk.maxZ(); z++) {
                int distance = Integer.MAX_VALUE;
                for (BoundingBox box : footprints) distance = Math.min(distance, distanceToFootprint(box, x, z));
                if (distance > CLEARANCE) continue;
                int naturalY = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                        level, level.getLevel().getChunkSource().randomState());
                int clearFrom = Math.min(floor, naturalY);
                for (int y = clearFrom; y <= floor + CLEAR_HEIGHT; y++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (isNaturalVegetation(level, pos, state)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        for (StructurePiece piece : container.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece part)) continue;
            String template = part.getElement().toString();
            if (template.contains("courtyard")) {
                for (int z = 15; z <= 17; z++) {
                    extendApproach(level, chunk, footprints, part, new BlockPos(0, 1, z), Direction.WEST);
                    extendApproach(level, chunk, footprints, part, new BlockPos(26, 1, z), Direction.EAST);
                }
                continue;
            }
            if (template.contains("/ritual")) continue;
            int localDoorX = (part.getRotation().rotate(Direction.EAST).getAxis() == Direction.Axis.X
                    ? part.getBoundingBox().getXSpan() : part.getBoundingBox().getZSpan()) / 2;
            int depth = part.getRotation().rotate(Direction.SOUTH).getAxis() == Direction.Axis.Z
                    ? part.getBoundingBox().getZSpan() : part.getBoundingBox().getXSpan();
            extendApproach(level, chunk, footprints, part, new BlockPos(localDoorX, 3, depth - 1), Direction.SOUTH);
        }
    }

    private static void extendApproach(WorldGenLevel level, BoundingBox chunk, List<BoundingBox> footprints,
                                       PoolElementStructurePiece part, BlockPos localEdge, Direction localOutward) {
        BlockPos edge = part.getPosition().offset(localEdge.rotate(part.getRotation()));
        Direction outward = part.getRotation().rotate(localOutward);
        for (int step = 1; step <= CLEARANCE; step++) {
            BlockPos at = edge.relative(outward, step).below(step - 1);
            if (footprints.stream().anyMatch(box -> distanceToFootprint(box, at.getX(), at.getZ()) == 0)) break;
            if (!chunk.isInside(at) || level.getBlockState(at).isCollisionShapeFullBlock(level, at)) continue;
            level.setBlock(at, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, outward.getOpposite()), 2);
            for (int support = 1; support <= 4 && at.getY() - support >= level.getMinY(); support++) {
                BlockPos below = at.below(support);
                if (level.getBlockState(below).isCollisionShapeFullBlock(level, below)) break;
                level.setBlock(below, Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }
    }

    private static boolean isNaturalVegetation(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.LEAVES)) return !state.getOptionalValue(BlockStateProperties.PERSISTENT).orElse(false);
        if (state.is(Blocks.VINE)) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(pos.relative(side)).is(Blocks.STRIPPED_SPRUCE_WOOD)) return false;
            }
        }
        return state.is(BlockTags.LOGS) && !state.getBlock().builtInRegistryHolder().key().identifier().getPath().startsWith("stripped_")
                || state.is(BlockTags.REPLACEABLE_BY_TREES) && state.getFluidState().isEmpty();
    }

    private static int distanceToFootprint(BoundingBox box, int x, int z) {
        int dx = Math.max(0, Math.max(box.minX() - x, x - box.maxX()));
        int dz = Math.max(0, Math.max(box.minZ() - z, z - box.maxZ()));
        return Math.max(dx, dz);
    }
}
