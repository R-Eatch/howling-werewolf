package com.howlingwerewolf.event;

import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntityEvents {

    public static void register() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ModEntities.HUNTER.get(), HunterEntity.createAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ModEntities.WEREWOLF.get(), WerewolfEntity.createAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ModEntities.ALPHA_WEREWOLF.get(), com.howlingwerewolf.entity.AlphaWerewolfEntity.createAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ModEntities.ALPHA_MINION.get(), com.howlingwerewolf.entity.AlphaMinionEntity.createAttributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ModEntities.WEREWOLF_COMPANION.get(), com.howlingwerewolf.entity.WerewolfCompanionEntity.createAttributes());
        net.minecraft.world.entity.SpawnPlacements.register(ModEntities.HUNTER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityEvents::hunterSpawnRules);
        net.minecraft.world.entity.SpawnPlacements.register(ModEntities.WEREWOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityEvents::werewolfSpawnRules);
        com.howlingwerewolf.content.FabricWorldGeneration.register();
    }

    /** Wild werewolves enter forests only beneath a full Overworld moon. / 野生狼人只在主世界满月的森林中出现。 */
    private static boolean werewolfSpawnRules(EntityType<WerewolfEntity> type, ServerLevelAccessor level,
                                              MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return true;
        if (!(level instanceof ServerLevel serverLevel) || !isFeralWerewolfSpawnWindow(serverLevel)) return false;
        // Keep darkness limits, without vanilla's additional random light rejection.
        int light = serverLevel.isThundering()
                ? level.getMaxLocalRawBrightness(pos, 10) : level.getMaxLocalRawBrightness(pos);
        return net.minecraft.world.entity.monster.Monster.checkAnyLightMonsterSpawnRules(
                type, level, reason, pos, random)
                && level.getBrightness(LightLayer.BLOCK, pos) <= level.dimensionType().monsterSpawnBlockLightLimit()
                && light <= level.dimensionType().monsterSpawnLightTest().getMaxValue();
    }

    public static boolean isFeralWerewolfSpawnWindow(ServerLevel level) {
        long time = Math.floorMod(level.getDayTime(), 24000L);
        return level.dimension() == net.minecraft.world.level.Level.OVERWORLD
                && time >= 13000L && time < 23000L && level.getMoonPhase() == 0;
    }

    private static boolean hunterSpawnRules(EntityType<HunterEntity> type, ServerLevelAccessor level,
                                            MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || !MonsterSpawnChecks.hasGroundAndSpace(type, level, pos)) return false;
        // Natural rarity is controlled by the biome modifier's low spawn weight. Applying another
        // random gate here made already-selected wild hunter spawns almost always fail.
        return true;
    }

    /** Small local replacement for hostile-mob darkness checks: hunters may patrol in daylight. */
    private static final class MonsterSpawnChecks {
        private static boolean hasGroundAndSpace(EntityType<?> type, LevelAccessor level, BlockPos pos) {
            BlockPos below = pos.below();
            return level.getBlockState(below).isValidSpawn(level, below, type)
                    && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
        }
    }

    private ModEntityEvents() {}
}
