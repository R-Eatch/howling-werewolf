package com.howlingwerewolf.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

public final class WolfsbaneBrewingRecipe extends BrewingRecipe {
    public WolfsbaneBrewingRecipe() {
        super(DataComponentIngredient.of(false,
                        PotionContents.createItemStack(Items.POTION, Potions.AWKWARD)),
                Ingredient.of(ModBlocks.WOLFSBANE.get()),
                new ItemStack(ModItems.WOLFSBANE_POTION.get()));
    }

    @Override
    public boolean isInput(ItemStack input) {
        PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
        return input.is(Items.POTION) && contents != null && contents.is(Potions.AWKWARD);
    }
}
