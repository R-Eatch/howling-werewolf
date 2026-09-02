package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
                    HowlingWerewolf.MOD_ID);

    public static final RegistryObject<Codec<ConfiguredSpawnBiomeModifier>> CONFIGURED_SPAWNS =
            BIOME_MODIFIER_SERIALIZERS.register("configured_spawns",
                    () -> ConfiguredSpawnBiomeModifier.CODEC);

    private ModBiomeModifiers() {}
}
