package com.howlingwerewolf.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public final class DoubleLootCount implements LootItemFunction {
    private static final DoubleLootCount INSTANCE = new DoubleLootCount();
    private static final MapCodec<DoubleLootCount> CODEC = MapCodec.unit(INSTANCE);

    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ModRegistry.id("double_count"), CODEC);
    }

    @Override public MapCodec<DoubleLootCount> codec() { return CODEC; }

    @Override public ItemStack apply(ItemStack stack, LootContext context) {
        return stack.copyWithCount(stack.getCount() * 2);
    }

    private DoubleLootCount() {}
}
