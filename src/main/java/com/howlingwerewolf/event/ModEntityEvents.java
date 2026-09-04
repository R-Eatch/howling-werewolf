package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ModEntityEvents {
    @Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void attributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.HUNTER.get(), HunterEntity.createAttributes().build());
            event.put(ModEntities.ALPHA_WEREWOLF.get(),
                    com.howlingwerewolf.entity.AlphaWerewolfEntity.createAttributes().build());
            event.put(ModEntities.ALPHA_MINION.get(),
                    com.howlingwerewolf.entity.AlphaMinionEntity.createAttributes().build());
            event.put(ModEntities.WEREWOLF.get(), WerewolfEntity.createAttributes().build());
        }
    }

    public static void registerSpawnPlacements() {
        SpawnPlacements.register(ModEntities.HUNTER.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityEvents::hunterSpawnRules);
        SpawnPlacements.register(ModEntities.WEREWOLF.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityEvents::werewolfSpawnRules);
    }

    /** Keep an ineligible werewolf entry from consuming a weighted natural-spawn selection. */
    public static void filterPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER) return;
        if (event.getLevel() instanceof ServerLevel serverLevel && isFeralWerewolfSpawnWindow(serverLevel)) return;
        for (int i = event.getSpawnerDataList().size() - 1; i >= 0; i--) {
            var spawn = event.getSpawnerDataList().get(i);
            if (spawn.type == ModEntities.WEREWOLF.get()) event.removeSpawnerData(spawn);
        }
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

    private static boolean isFeralWerewolfSpawnWindow(ServerLevel level) {
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
