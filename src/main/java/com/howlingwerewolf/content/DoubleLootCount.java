package com.howlingwerewolf.content;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;

public final class DoubleLootCount implements LootItemFunction {
    private static final DoubleLootCount INSTANCE = new DoubleLootCount();
    public static final DeferredRegister<LootItemFunctionType> TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, HowlingWerewolf.MOD_ID);
    private static final java.util.function.Supplier<LootItemFunctionType> TYPE =
            TYPES.register("double_count", () -> new LootItemFunctionType(new Serializer<DoubleLootCount>() {
                @Override public void serialize(JsonObject json, DoubleLootCount fn, JsonSerializationContext context) {}
                @Override public DoubleLootCount deserialize(JsonObject json, JsonDeserializationContext context) {
                    return INSTANCE;
                }
            }));

    @Override public LootItemFunctionType getType() { return TYPE.get(); }

    @Override public ItemStack apply(ItemStack stack, LootContext context) {
        return stack.copyWithCount(stack.getCount() * 2);
    }

    private DoubleLootCount() {}
}
