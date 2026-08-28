package com.howlingwerewolf.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public final class WolfsbaneBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        return input.is(Items.POTION) && PotionUtils.getPotion(input) == Potions.AWKWARD;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(ModBlocks.WOLFSBANE.get().asItem());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return isInput(input) && isIngredient(ingredient)
                ? new ItemStack(ModItems.WOLFSBANE_POTION.get())
                : ItemStack.EMPTY;
    }
}
