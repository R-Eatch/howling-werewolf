package com.howlingwerewolf.content;
import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.*;

/** Small registration helpers; every value is registered in the vanilla registry immediately. */
public class ModRegistry<T> {
    private final Registry<T> registry;
    public ModRegistry(Registry<T> registry) { this.registry = registry; }
    public static Identifier id(String name) { return Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, name); }
    public <V extends T> Supplier<V> register(String name, Supplier<V> factory) {
        V value = Registry.register(registry, id(name), factory.get());
        return () -> value;
    }
    public static final class Items extends ModRegistry<Item> {
        public Items() { super(BuiltInRegistries.ITEM); }
        public Supplier<Item> registerSimpleItem(String name) { return registerItem(name, Item::new); }
        public Supplier<Item> registerSimpleItem(String name, UnaryOperator<Item.Properties> props) {
            return registerItem(name, p -> new Item(props.apply(p)));
        }
        public Supplier<Item> registerItem(String name, Function<Item.Properties, Item> factory) {
            return register(name, () -> factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
        }
        public void registerSimpleBlockItem(Supplier<Block> block) {
            String name = BuiltInRegistries.BLOCK.getKey(block.get()).getPath();
            registerItem(name, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()));
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
            return register(name, () -> factory.apply(props.get().setId(ResourceKey.create(Registries.BLOCK, id(name)))));
        }
    }
}
