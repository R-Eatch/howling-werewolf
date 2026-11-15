package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.world.VillageWatchtower;
import com.howlingwerewolf.world.WerewolfTribe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, HowlingWerewolf.MOD_ID);
    public static final DeferredHolder<StructureType<?>, StructureType<VillageWatchtower>> HUNTER_WATCHTOWER =
            TYPES.register("hunter_watchtower", () -> () -> VillageWatchtower.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<WerewolfTribe>> WEREWOLF_TRIBE =
            TYPES.register("werewolf_tribe", () -> () -> WerewolfTribe.CODEC);

    private ModStructures() {}
}
