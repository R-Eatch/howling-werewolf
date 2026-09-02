package com.howlingwerewolf.content;

import com.howlingwerewolf.HWConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/** A data-driven placement gate backed by the common world-generation config. */
public final class ConfigEnabledPlacement extends PlacementFilter {
    public static final MapCodec<ConfigEnabledPlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("feature").forGetter(placement -> placement.feature),
                    Codec.INT.optionalFieldOf("base_chance", 1)
                            .forGetter(placement -> placement.baseChance)
            ).apply(instance, ConfigEnabledPlacement::new));

    private final String feature;
    private final int baseChance;

    private ConfigEnabledPlacement(String feature, int baseChance) {
        this.feature = feature;
        this.baseChance = Math.max(1, baseChance);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return switch (feature) {
            case "silver" -> HWConfig.GENERATE_SILVER.get();
            case "wolfsbane" -> HWConfig.GENERATE_WOLFSBANE.get()
                    && random.nextDouble() < Math.min(1.0D,
                    HWConfig.WOLFSBANE_GENERATION_WEIGHT.get() / (100.0D * baseChance));
            default -> false;
        };
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.CONFIG_ENABLED.get();
    }
}
