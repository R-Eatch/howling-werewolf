package com.howlingwerewolf.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;

public final class WerewolfPotionBrewingRecipe extends BrewingRecipe {
    public WerewolfPotionBrewingRecipe() {
        super(Ingredient.of(ModItems.WOLFSBANE_POTION.get()),
                Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                new ItemStack(ModItems.WEREWOLF_POTION.get()));
    }
}
