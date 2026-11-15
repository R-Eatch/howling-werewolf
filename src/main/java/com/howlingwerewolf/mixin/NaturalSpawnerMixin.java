package com.howlingwerewolf.mixin;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.event.ModEntityEvents;
import com.howlingwerewolf.world.SettlementPositions;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.WeightedRandomList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerMixin {
    @ModifyReturnValue(method="mobsAt",at=@At("RETURN"))
    private static WeightedRandomList<MobSpawnSettings.SpawnerData> howling$fullMoon(WeightedRandomList<MobSpawnSettings.SpawnerData> result,
            ServerLevel level,StructureManager structures,ChunkGenerator generator,MobCategory category,BlockPos pos,Holder<Biome> biome) {
        if (category != MobCategory.MONSTER) return result;
        boolean feralAllowed = ModEntityEvents.isFeralWerewolfSpawnWindow(level);
        boolean huntersAllowed = result.unwrap().stream().noneMatch(entry -> entry.type == ModEntities.HUNTER.get())
                || !SettlementPositions.blocksHunterSpawn(level, pos);
        if (feralAllowed && huntersAllowed) return result;
        return WeightedRandomList.create(result.unwrap().stream()
                .filter(entry -> (feralAllowed || entry.type != ModEntities.WEREWOLF.get())
                        && (huntersAllowed || entry.type != ModEntities.HUNTER.get())).toList());
    }
}
