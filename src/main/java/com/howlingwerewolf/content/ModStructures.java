package com.howlingwerewolf.content;

import com.howlingwerewolf.world.VillageWatchtower;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.function.Supplier;

public final class ModStructures {
    public static final ModRegistry<StructureType<?>> TYPES = new ModRegistry<>(BuiltInRegistries.STRUCTURE_TYPE);
    public static final Supplier<StructureType<VillageWatchtower>> HUNTER_WATCHTOWER =
            TYPES.register("hunter_watchtower", () -> () -> VillageWatchtower.CODEC);

    private ModStructures() {}
}
