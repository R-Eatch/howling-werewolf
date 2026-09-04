package com.howlingwerewolf.content;

import com.howlingwerewolf.HWConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

/** Adds forest, taiga and cherry grove spawns with weights supplied by the common config. */
public record ConfiguredSpawnBiomeModifier(String spawn) implements BiomeModifier {
    public static final Codec<ConfiguredSpawnBiomeModifier> CODEC = Codec.STRING.fieldOf("spawn")
            .xmap(ConfiguredSpawnBiomeModifier::new, ConfiguredSpawnBiomeModifier::spawn).codec();

    @Override
    public void modify(Holder<Biome> biome, Phase phase,
                       ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || (!biome.is(BiomeTags.IS_FOREST)
                && !biome.is(BiomeTags.IS_TAIGA)
                && !biome.is(Biomes.CHERRY_GROVE))) return;

        if ("hunter".equals(spawn)) {
            int weight = HWConfig.hunterSpawnWeight();
            if (weight > 0) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(ModEntities.HUNTER.get(), weight, 2, 3));
            }
        } else if ("werewolf".equals(spawn)) {
            int weight = HWConfig.feralWerewolfSpawnWeight();
            if (weight > 0) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(ModEntities.WEREWOLF.get(), weight, 1, 1));
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return ModBiomeModifiers.CONFIGURED_SPAWNS.get();
    }
}
