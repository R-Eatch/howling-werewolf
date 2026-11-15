package com.howlingwerewolf.content;

import com.howlingwerewolf.world.VillageWatchtower;
import com.howlingwerewolf.world.WerewolfTribe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.function.Supplier;

public final class ModStructures {
    public static final ModRegistry<StructureType<?>> TYPES = new ModRegistry<>(BuiltInRegistries.STRUCTURE_TYPE);
    public static final Supplier<StructureType<VillageWatchtower>> HUNTER_WATCHTOWER =
            TYPES.register("hunter_watchtower", () -> () -> VillageWatchtower.CODEC);

    public static final Supplier<StructureType<WerewolfTribe>> WEREWOLF_TRIBE =
            TYPES.register("werewolf_tribe", () -> () -> WerewolfTribe.CODEC);

    private ModStructures() {}
}
