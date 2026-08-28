package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.howlingwerewolf.trial.RitualAltarBlock;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<Block> SILVER_ORE = register("silver_ore", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> DEEPSLATE_SILVER_ORE = register("deepslate_silver_ore", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> WOLFSBANE = register("wolfsbane", () -> new WolfsbaneFlowerBlock(
            MobEffects.CONFUSION, 10, BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.POPPY)
            .mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> RITUAL_ALTAR = register("ritual_altar",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(3.5F, 6.0F).sound(SoundType.STONE), false));
    public static final RegistryObject<Block> CENTRAL_RITUAL_ALTAR = BLOCKS.register("central_ritual_altar",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD).strength(5.0F, 9.0F).sound(SoundType.METAL), true));

    static {
        ModItems.ITEMS.register("central_ritual_altar", () -> new CentralRitualAltarBlockItem(
                CENTRAL_RITUAL_ALTAR.get(), new Item.Properties().fireResistant()));
    }

    private static RegistryObject<Block> register(String name, Supplier<Block> supplier) {
        RegistryObject<Block> block = BLOCKS.register(name, supplier);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private ModBlocks() {}
}
