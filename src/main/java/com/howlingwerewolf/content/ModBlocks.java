package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.trial.RitualAltarBlock;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HowlingWerewolf.MOD_ID);

    public static final DeferredBlock<Block> SILVER_ORE = BLOCKS.registerSimpleBlock("silver_ore",
            props -> props.mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> DEEPSLATE_SILVER_ORE = BLOCKS.registerSimpleBlock("deepslate_silver_ore",
            props -> props.mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> WOLFSBANE = BLOCKS.registerBlock("wolfsbane",
            props -> new WolfsbaneFlowerBlock(MobEffects.NAUSEA, 10, props),
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POPPY)
                    .mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.GRASS));
    public static final DeferredBlock<Block> RITUAL_ALTAR = BLOCKS.registerBlock("ritual_altar",
            props -> new RitualAltarBlock(props, false),
            props -> props.mapColor(MapColor.STONE).strength(3.5F, 6.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> CENTRAL_RITUAL_ALTAR = BLOCKS.registerBlock("central_ritual_altar",
            props -> new RitualAltarBlock(props, true),
            props -> props.mapColor(MapColor.GOLD).strength(5.0F, 9.0F).sound(SoundType.METAL));

    static {
        ModItems.ITEMS.registerSimpleBlockItem(SILVER_ORE);
        ModItems.ITEMS.registerSimpleBlockItem(DEEPSLATE_SILVER_ORE);
        ModItems.ITEMS.registerSimpleBlockItem(WOLFSBANE);
        ModItems.ITEMS.registerSimpleBlockItem(RITUAL_ALTAR);
        ModItems.ITEMS.registerItem("central_ritual_altar", props -> new CentralRitualAltarBlockItem(
                CENTRAL_RITUAL_ALTAR.get(), props.fireResistant().useBlockDescriptionPrefix()));
    }

    private ModBlocks() {}
}
