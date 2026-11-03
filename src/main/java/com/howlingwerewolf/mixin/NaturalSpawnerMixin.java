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
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerMixin {
    @ModifyReturnValue(method="mobsAt",at=@At("RETURN"))
    private static WeightedList<MobSpawnSettings.SpawnerData> howling$fullMoon(WeightedList<MobSpawnSettings.SpawnerData> result,
            ServerLevel level,StructureManager structures,ChunkGenerator generator,MobCategory category,BlockPos pos,Holder<Biome> biome) {
        if (category!=MobCategory.MONSTER || ModEntityEvents.isFeralWerewolfSpawnWindow(level)) return result;
        return WeightedList.of(result.unwrap().stream().filter(entry -> entry.value().type()!=ModEntities.WEREWOLF.get()).toList());
    }
}
