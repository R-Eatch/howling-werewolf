package com.howlingwerewolf.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

public final class WerewolfPotionBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        return input.is(ModItems.WOLFSBANE_POTION.get());
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(Items.FERMENTED_SPIDER_EYE);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return isInput(input) && isIngredient(ingredient)
                ? new ItemStack(ModItems.WEREWOLF_POTION.get())
                : ItemStack.EMPTY;
    }
}
