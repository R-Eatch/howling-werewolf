package com.howlingwerewolf.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

public final class WolfsbaneBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
        return input.is(Items.POTION) && contents != null && contents.is(Potions.AWKWARD);
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
