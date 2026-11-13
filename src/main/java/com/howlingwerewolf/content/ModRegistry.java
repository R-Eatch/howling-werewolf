package com.howlingwerewolf.content;
import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.*;

/** Small registration helpers; every value is registered in the vanilla registry immediately. */
public class ModRegistry<T> {
    private final Registry<T> registry;
    public ModRegistry(Registry<T> registry) { this.registry = registry; }
    public static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, name); }
    public <V extends T> Supplier<V> register(String name, Supplier<V> factory) {
        V value = Registry.register(registry, id(name), factory.get());
        return () -> value;
    }
    public static final class Items extends ModRegistry<Item> {
        public Items() { super(BuiltInRegistries.ITEM); }
        public Supplier<Item> registerSimpleItem(String name) { return registerItem(name, Item::new); }
        public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory) {
            return register(name, () -> factory.apply(new Item.Properties()));
        }
        public void registerBlockItem(Supplier<Block> block, Function<Item.Properties, BlockItem> factory) {
            String name = BuiltInRegistries.BLOCK.getKey(block.get()).getPath();
            BlockItem item = registerItem(name, factory).get();
            item.registerBlocks(Item.BY_BLOCK, item);
        }
        public void registerSimpleBlockItem(Supplier<Block> block) {
            registerBlockItem(block, p -> new BlockItem(block.get(), p));
        }
    }
    public static final class Blocks extends ModRegistry<Block> {
        public Blocks() { super(BuiltInRegistries.BLOCK); }
        public Supplier<Block> registerSimpleBlock(String name, UnaryOperator<BlockBehaviour.Properties> props) {
            return registerBlock(name, Block::new, props);
        }
        public Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory,
                UnaryOperator<BlockBehaviour.Properties> props) {
            return registerBlock(name, factory, () -> props.apply(BlockBehaviour.Properties.of()));
        }
        public Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory,
                Supplier<BlockBehaviour.Properties> props) {
            return register(name, () -> factory.apply(props.get()));
        }
    }
}
