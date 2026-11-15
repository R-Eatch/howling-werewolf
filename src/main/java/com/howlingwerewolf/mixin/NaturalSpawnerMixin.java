package com.howlingwerewolf.mixin;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.event.ModEntityEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerMixin {
    @ModifyReturnValue(method="mobsAt",at=@At("RETURN"))
    private static WeightedList<MobSpawnSettings.SpawnerData> howling$fullMoon(WeightedList<MobSpawnSettings.SpawnerData> result,
            ServerLevel level,StructureManager structures,ChunkGenerator generator,MobCategory category,BlockPos pos) {
        if (category != MobCategory.MONSTER) return result;
        boolean feralAllowed = ModEntityEvents.isFeralWerewolfSpawnWindow(level);
        boolean huntersAllowed = result.unwrap().stream().noneMatch(entry -> entry.value().type() == ModEntities.HUNTER.get())
                || !com.howlingwerewolf.world.SettlementPositions.blocksHunterSpawn(level, pos);
        if (feralAllowed && huntersAllowed) return result;
        var selected = WeightedList.<MobSpawnSettings.SpawnerData>builder();
        for (var entry : result.unwrap()) {
            var type = entry.value().type();
            if (!feralAllowed && type == ModEntities.WEREWOLF.get() || !huntersAllowed && type == ModEntities.HUNTER.get()) continue;
            selected.add(entry.value(), entry.weight());
        }
        return selected.build();
    }
}
