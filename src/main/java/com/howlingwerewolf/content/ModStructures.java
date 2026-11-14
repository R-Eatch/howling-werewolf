package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.world.VillageWatchtower;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, HowlingWerewolf.MOD_ID);
    public static final RegistryObject<StructureType<VillageWatchtower>> HUNTER_WATCHTOWER =
            TYPES.register("hunter_watchtower", () -> () -> VillageWatchtower.CODEC);

    private ModStructures() {}
}
