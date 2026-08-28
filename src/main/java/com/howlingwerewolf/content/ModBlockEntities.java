package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.trial.RitualAltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<BlockEntityType<RitualAltarBlockEntity>> RITUAL_ALTAR =
            BLOCK_ENTITIES.register("ritual_altar", () -> BlockEntityType.Builder.of(
                    RitualAltarBlockEntity::new,
                    ModBlocks.RITUAL_ALTAR.get(), ModBlocks.CENTRAL_RITUAL_ALTAR.get()).build(null));

    private ModBlockEntities() {}
}
