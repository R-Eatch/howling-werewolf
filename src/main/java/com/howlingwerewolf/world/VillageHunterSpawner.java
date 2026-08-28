package com.howlingwerewolf.world;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.entity.HunterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Maintains a persistent 4–6 hunter patrol per visited village. / 为每个被访问的村庄维持一支 4–6 人持久巡猎队。 */
@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VillageHunterSpawner {
    private static final int CHECK_INTERVAL = 100;
    private static final int VILLAGE_SEARCH_CHUNKS = 5;
    private static final double ACTIVATION_DISTANCE_SQR = 96.0D * 96.0D;
    private static final long REPLENISH_INTERVAL = 24000L;
    private static final long FAILED_SPAWN_RETRY_INTERVAL = 1200L;

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % CHECK_INTERVAL != 0) return;
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;

        BlockPos village = level.findNearestMapStructure(
                StructureTags.VILLAGE, player.blockPosition(), VILLAGE_SEARCH_CHUNKS, false);
        if (village == null) return;
        BlockPos anchor = village;
        if (anchor.distSqr(player.blockPosition()) > ACTIVATION_DISTANCE_SQR) return;

        VillageHunterSavedData data = VillageHunterSavedData.get(level);
        long villageKey = anchor.asLong();
        int target = data.getOrCreateTarget(villageKey, level.random);

        AABB villageArea = new AABB(anchor).inflate(64.0D, 32.0D, 64.0D);
        int existing = level.getEntitiesOfClass(HunterEntity.class, villageArea,
                hunter -> hunter.belongsToVillage(villageKey)).size();
        if (!data.isComplete(villageKey)) {
            int needed = Math.max(0, target - existing);
            int spawned = spawnPatrolMembers(level, anchor, villageKey, needed);
            if (existing + spawned >= target) data.markComplete(villageKey);
            return;
        }

        if (existing >= target) {
            data.clearReplenishment(villageKey);
            return;
        }

        long gameTime = level.getGameTime();
        long nextTick = data.getNextReplenishmentTick(villageKey);
        if (nextTick <= 0L) {
            // Start the cooldown when a missing patrol member is first observed. This also migrates
            // completed villages from older saves without immediately creating replacement mobs.
            data.scheduleReplenishment(villageKey, gameTime + REPLENISH_INTERVAL);
            return;
        }
        if (gameTime < nextTick) return;

        int spawned = spawnPatrolMembers(level, anchor, villageKey, 1);
        if (spawned > 0 && existing + spawned >= target) {
            data.clearReplenishment(villageKey);
        } else {
            long retryDelay = spawned > 0 ? REPLENISH_INTERVAL : FAILED_SPAWN_RETRY_INTERVAL;
            data.scheduleReplenishment(villageKey, gameTime + retryDelay);
        }
    }

    private static int spawnPatrolMembers(ServerLevel level, BlockPos anchor, long villageKey, int count) {
        int spawned = 0;
        for (int member = 0; member < count; member++) {
            for (int attempt = 0; attempt < 32; attempt++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0D;
                double radius = 5.0D + level.random.nextDouble() * 15.0D;
                int x = anchor.getX() + (int)Math.round(Math.cos(angle) * radius);
                int z = anchor.getZ() + (int)Math.round(Math.sin(angle) * radius);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos pos = new BlockPos(x, y, z);
                if (!hasGroundAndSpace(level, pos)) continue;

                HunterEntity hunter = ModEntities.HUNTER.get().create(level);
                if (hunter == null) break;
                hunter.moveTo(x + 0.5D, y, z + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                if (!level.noCollision(hunter)) continue;
                DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
                hunter.finalizeSpawn(level, difficulty, MobSpawnType.EVENT, null, null);
                hunter.configureForVillagePatrol(villageKey);
                if (level.addFreshEntity(hunter)) {
                    spawned++;
                    break;
                }
            }
        }
        return spawned;
    }

    private static boolean hasGroundAndSpace(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isValidSpawn(level, below, ModEntities.HUNTER.get())
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    private VillageHunterSpawner() {}
}
