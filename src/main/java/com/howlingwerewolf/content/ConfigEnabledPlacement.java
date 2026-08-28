package com.howlingwerewolf.content;

import com.howlingwerewolf.HWConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/** A data-driven placement gate backed by the common world-generation config. */
public final class ConfigEnabledPlacement extends PlacementFilter {
    public static final Codec<ConfigEnabledPlacement> CODEC = Codec.STRING.fieldOf("feature")
            .xmap(ConfigEnabledPlacement::new, placement -> placement.feature).codec();

    private final String feature;

    private ConfigEnabledPlacement(String feature) {
        this.feature = feature;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return switch (feature) {
            case "silver" -> HWConfig.GENERATE_SILVER.get();
            case "wolfsbane" -> HWConfig.GENERATE_WOLFSBANE.get();
            default -> false;
        };
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.CONFIG_ENABLED.get();
    }
}
