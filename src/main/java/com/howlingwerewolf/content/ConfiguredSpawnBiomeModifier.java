package com.howlingwerewolf.content;

import com.howlingwerewolf.HWConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/** Adds forest, taiga and cherry grove spawns with weights supplied by the common config. */
public record ConfiguredSpawnBiomeModifier(String spawn) implements BiomeModifier {
    public static final MapCodec<ConfiguredSpawnBiomeModifier> CODEC = Codec.STRING.fieldOf("spawn")
            .xmap(ConfiguredSpawnBiomeModifier::new, ConfiguredSpawnBiomeModifier::spawn);

    @Override
    public void modify(Holder<Biome> biome, Phase phase,
                       ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || (!biome.is(BiomeTags.IS_FOREST)
                && !biome.is(BiomeTags.IS_TAIGA)
                && !biome.is(Biomes.CHERRY_GROVE))) return;

        if ("hunter".equals(spawn)) {
            int weight = HWConfig.HUNTER_SPAWN_WEIGHT.get();
            if (weight > 0) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(ModEntities.HUNTER.get(), weight, 2, 3));
            }
        } else if ("werewolf".equals(spawn)) {
            int weight = HWConfig.FERAL_WEREWOLF_SPAWN_WEIGHT.get();
            if (weight > 0) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(ModEntities.WEREWOLF.get(), weight, 1, 1));
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModBiomeModifiers.CONFIGURED_SPAWNS.get();
    }
}
