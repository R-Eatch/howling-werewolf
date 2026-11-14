package com.howlingwerewolf.world;

import com.howlingwerewolf.content.ModBlockEntities;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.howlingwerewolf.entity.TribeWerewolfEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SettlementMarkerBlockEntity extends BlockEntity {
    private Long villageKey;

    public SettlementMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SETTLEMENT_MARKER.get(), pos, state);
    }

    static void tick(Level level, BlockPos pos, BlockState state, SettlementMarkerBlockEntity marker) {
        if (!(level instanceof ServerLevel server) || !level.dimension().equals(Level.OVERWORLD)
                || (level.getGameTime() + pos.asLong()) % 40L != 0
                || !server.hasNearbyAlivePlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 72)) return;

        boolean tribe = state.getValue(SettlementMarkerBlock.TRIBE);
        if (!tribe && marker.villageKey == null) {
            var structures = server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            var tower = structures.get(VillageWatchtower.KEY);
            var village = tower == null ? net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START
                    : server.structureManager().getStructureWithPieceAt(pos, tower);
            if (!village.isValid()) village = server.structureManager().getStructureWithPieceAt(pos, StructureTags.VILLAGE);
            if (!village.isValid()) return;
            marker.villageKey = village.getChunkPos().getWorldPosition().asLong();
            marker.setChanged();
        }

        SettlementData data = SettlementData.get(server);
        SettlementData.Site site = data.getOrCreate(pos, tribe, server.random);
        int slots = tribe ? site.members.length : 1;
        for (int slot = 0; slot < slots; slot++) {
            if (site.members[slot] != null || server.getGameTime() < site.replacementAt[slot]) continue;
            boolean replacement = site.replacementAt[slot] != 0;
            EntityType<? extends Mob> type = slot == 0
                    ? (tribe ? ModEntities.TRIBE_CHIEF.get() : ModEntities.HUNTER_CAPTAIN.get()) : ModEntities.TRIBE_WEREWOLF.get();
            Mob mob = type.create(server);
            if (mob == null || !findSpawnPosition(server, pos, mob)) continue;
            if (mob instanceof SettlementLeaderEntity leader) {
                leader.settlementSpawn(pos, marker.villageKey, site.coat);
                if (!site.tradeStock.isEmpty()) leader.restoreTradeStock(site.tradeStock);
            } else if (mob instanceof TribeWerewolfEntity resident) {
                resident.settlementSpawn(pos, site.coat);
            }
            if (server.addFreshEntity(mob)) {
                site.members[slot] = mob.getUUID();
                site.replacementAt[slot] = 0;
                if (slot == 0) site.tradeStock = new CompoundTag();
                data.setDirty();
                if (replacement) break;
            }
        }
    }

    private static boolean findSpawnPosition(ServerLevel level, BlockPos marker, Mob mob) {
        for (BlockPos spot : BlockPos.spiralAround(marker.above(), 4, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.SOUTH)) {
            if (!level.hasChunkAt(spot) || !level.getBlockState(spot.below()).isFaceSturdy(level, spot.below(), net.minecraft.core.Direction.UP)
                    || !level.getFluidState(spot).isEmpty() || !level.getBlockState(spot).getCollisionShape(level, spot).isEmpty()) continue;
            Vec3 feet = Vec3.atBottomCenterOf(spot);
            mob.moveTo(feet.x, feet.y, feet.z, level.random.nextFloat() * 360, 0);
            if (level.noCollision(mob)) return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (villageKey != null) tag.putLong("VillageKey", villageKey);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        villageKey = tag.contains("VillageKey") ? tag.getLong("VillageKey") : null;
    }
}
