package com.howlingwerewolf.content;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public final class ModPlacementModifiers {
    public static final ModRegistry<MapCodec<? extends PlacementModifier>> PLACEMENT_MODIFIERS =
            new ModRegistry<>(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
    public static final Supplier<MapCodec<ConfigEnabledPlacement>> CONFIG_ENABLED =
            PLACEMENT_MODIFIERS.register("config_enabled", () -> ConfigEnabledPlacement.CODEC);

    private ModPlacementModifiers() {}
}
