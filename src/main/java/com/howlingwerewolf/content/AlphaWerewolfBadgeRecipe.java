package com.howlingwerewolf.content;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class AlphaWerewolfBadgeRecipe extends CustomRecipe {
    public AlphaWerewolfBadgeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (container.getWidth() != 3 || container.getHeight() != 3
                || !container.getItem(4).is(ModItems.MOONBANE_PEARL.get())) {
            return false;
        }
        int silver = 0;
        int wolfsbane = 0;
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 4) continue;
            ItemStack stack = container.getItem(slot);
            if (stack.is(ModItems.SILVER_INGOT.get())) {
                silver++;
            } else if (stack.is(ModBlocks.WOLFSBANE.get().asItem())) {
                wolfsbane++;
            } else {
                return false;
            }
        }
        return silver == 4 && wolfsbane == 4;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get());
    }

    /**
     * Exposes one deterministic arrangement to the vanilla recipe book and recipe viewers such as
     * JEI. Manual crafting still accepts any ordering of the four silver ingots and four wolfsbane
     * flowers around the center pearl through {@link #matches(CraftingContainer, Level)}.
     */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ModItems.SILVER_INGOT.get()),
                Ingredient.of(ModBlocks.WOLFSBANE.get()),
                Ingredient.of(ModItems.SILVER_INGOT.get()),
                Ingredient.of(ModBlocks.WOLFSBANE.get()),
                Ingredient.of(ModItems.MOONBANE_PEARL.get()),
                Ingredient.of(ModBlocks.WOLFSBANE.get()),
                Ingredient.of(ModItems.SILVER_INGOT.get()),
                Ingredient.of(ModBlocks.WOLFSBANE.get()),
                Ingredient.of(ModItems.SILVER_INGOT.get()));
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALPHA_WEREWOLF_BADGE.get();
    }
}
