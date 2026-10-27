package com.howlingwerewolf.content;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class AlphaWerewolfBadgeRecipe extends CustomRecipe {
    public AlphaWerewolfBadgeRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        if (container.width() != 3 || container.height() != 3
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
    public ItemStack assemble(CraftingInput container) {
        return new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get());
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients());
    }

    // The recipe book displays one layout; matching still permits every arrangement of
    // four silver ingots and four wolfsbane flowers around the central pearl.
    private List<Ingredient> ingredients() {
        return List.of(
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
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(3, 3,
                ingredients().stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemSlotDisplay(ModItems.ALPHA_WEREWOLF_BADGE.get()),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public RecipeSerializer<AlphaWerewolfBadgeRecipe> getSerializer() {
        return ModRecipes.ALPHA_WEREWOLF_BADGE.get();
    }
}
