package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

public final class ModEntityEvents {
    @EventBusSubscriber(modid = HowlingWerewolf.MOD_ID)
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

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.HUNTER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityEvents::hunterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.WEREWOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityEvents::werewolfSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }

    /** Wild werewolves enter forests only beneath a full Overworld moon. / 野生狼人只在主世界满月的森林中出现。 */
    private static boolean werewolfSpawnRules(EntityType<WerewolfEntity> type, ServerLevelAccessor level,
                                              MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return true;
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || serverLevel.dimension() != net.minecraft.world.level.Level.OVERWORLD) return false;
        long time = Math.floorMod(serverLevel.getDayTime(), 24000L);
        if (time < 13000L || time >= 23000L || serverLevel.getMoonPhase() != 0) return false;
        return net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(
                type, level, reason, pos, random);
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
