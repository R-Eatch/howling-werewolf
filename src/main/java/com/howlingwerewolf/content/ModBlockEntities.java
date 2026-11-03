package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import com.howlingwerewolf.trial.RitualAltarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final ModRegistry<BlockEntityType<?>> BLOCK_ENTITIES =
            new ModRegistry<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);

    public static final Supplier<BlockEntityType<RitualAltarBlockEntity>> RITUAL_ALTAR =
            BLOCK_ENTITIES.register("ritual_altar", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(
                    RitualAltarBlockEntity::new, ModBlocks.RITUAL_ALTAR.get(), ModBlocks.CENTRAL_RITUAL_ALTAR.get()).build());

    private ModBlockEntities() {}
}
