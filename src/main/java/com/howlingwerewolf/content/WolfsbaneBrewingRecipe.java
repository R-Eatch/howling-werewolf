package com.howlingwerewolf.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

public final class WolfsbaneBrewingRecipe extends BrewingRecipe {
    public WolfsbaneBrewingRecipe() {
        super(StrictNBTIngredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                Ingredient.of(ModBlocks.WOLFSBANE.get()),
                new ItemStack(ModItems.WOLFSBANE_POTION.get()));
    }

    @Override
    public boolean isInput(ItemStack input) {
        return input.is(Items.POTION) && PotionUtils.getPotion(input) == Potions.AWKWARD;
    }
}
