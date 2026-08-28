package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<PlacementModifierType<ConfigEnabledPlacement>> CONFIG_ENABLED =
            PLACEMENT_MODIFIERS.register("config_enabled", () -> () -> ConfigEnabledPlacement.CODEC);

    private ModPlacementModifiers() {}
}
