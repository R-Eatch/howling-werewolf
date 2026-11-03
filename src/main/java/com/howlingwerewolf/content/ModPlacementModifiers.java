package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ModPlacementModifiers {
    public static final ModRegistry<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            new ModRegistry<>(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);

    public static final Supplier<PlacementModifierType<ConfigEnabledPlacement>> CONFIG_ENABLED =
            PLACEMENT_MODIFIERS.register("config_enabled", () -> () -> ConfigEnabledPlacement.CODEC);

    private ModPlacementModifiers() {}
}
