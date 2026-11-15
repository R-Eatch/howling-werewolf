package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DoubleLootCount implements LootItemFunction {
    private static final DoubleLootCount INSTANCE = new DoubleLootCount();
    public static final DeferredRegister<LootItemFunctionType<?>> TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, HowlingWerewolf.MOD_ID);
    private static final java.util.function.Supplier<LootItemFunctionType<DoubleLootCount>> TYPE =
            TYPES.register("double_count", () -> new LootItemFunctionType<>(MapCodec.unit(INSTANCE)));

    @Override public LootItemFunctionType<DoubleLootCount> getType() { return TYPE.get(); }

    @Override public ItemStack apply(ItemStack stack, LootContext context) {
        return stack.copyWithCount(stack.getCount() * 2);
    }

    private DoubleLootCount() {}
}
