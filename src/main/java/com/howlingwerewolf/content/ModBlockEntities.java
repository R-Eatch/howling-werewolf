package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.trial.RitualAltarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HowlingWerewolf.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RitualAltarBlockEntity>> RITUAL_ALTAR =
            BLOCK_ENTITIES.register("ritual_altar", () -> BlockEntityType.Builder.of(
                    RitualAltarBlockEntity::new,
                    ModBlocks.RITUAL_ALTAR.get(), ModBlocks.CENTRAL_RITUAL_ALTAR.get()).build(null));

    private ModBlockEntities() {}
}
