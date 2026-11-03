package com.howlingwerewolf.content;
import com.howlingwerewolf.HWConfig;
import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
public final class FabricWorldGeneration {
    public static void register() {
        var forests=BiomeSelectors.tag(BiomeTags.IS_FOREST).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)).or(BiomeSelectors.includeByKey(Biomes.CHERRY_GROVE));
        if (HWConfig.hunterSpawnWeight()>0) BiomeModifications.addSpawn(forests, MobCategory.MONSTER,ModEntities.HUNTER.get(),HWConfig.hunterSpawnWeight(),2,3);
        if (HWConfig.feralWerewolfSpawnWeight()>0) BiomeModifications.addSpawn(forests, MobCategory.MONSTER,ModEntities.WEREWOLF.get(),HWConfig.feralWerewolfSpawnWeight(),1,1);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),GenerationStep.Decoration.UNDERGROUND_ORES,
            ResourceKey.create(Registries.PLACED_FEATURE,ModRegistry.id("silver_ore")));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.OLD_GROWTH_PINE_TAIGA,Biomes.OLD_GROWTH_SPRUCE_TAIGA,Biomes.TAIGA),GenerationStep.Decoration.VEGETAL_DECORATION,
            ResourceKey.create(Registries.PLACED_FEATURE,ModRegistry.id("wolfsbane")));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DARK_FOREST,Biomes.FOREST,Biomes.BIRCH_FOREST),GenerationStep.Decoration.VEGETAL_DECORATION,
            ResourceKey.create(Registries.PLACED_FEATURE,ModRegistry.id("wolfsbane_rare")));
    }
    private FabricWorldGeneration() { }
}
