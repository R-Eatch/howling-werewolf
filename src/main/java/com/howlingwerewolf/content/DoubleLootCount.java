package com.howlingwerewolf.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class DoubleLootCount implements LootItemFunction {
    private static final DoubleLootCount INSTANCE = new DoubleLootCount();
    private static final LootItemFunctionType<DoubleLootCount> TYPE = new LootItemFunctionType<>(MapCodec.unit(INSTANCE));

    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ModRegistry.id("double_count"), TYPE);
    }

    @Override public LootItemFunctionType<DoubleLootCount> getType() { return TYPE; }

    @Override public ItemStack apply(ItemStack stack, LootContext context) {
        return stack.copyWithCount(stack.getCount() * 2);
    }

    private DoubleLootCount() {}
}
