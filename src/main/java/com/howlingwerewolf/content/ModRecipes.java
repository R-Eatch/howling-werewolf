package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HowlingWerewolf.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlphaWerewolfBadgeRecipe>> ALPHA_WEREWOLF_BADGE =
            SERIALIZERS.register("alpha_werewolf_badge",
                    () -> new SimpleCraftingRecipeSerializer<>(AlphaWerewolfBadgeRecipe::new));

    private ModRecipes() {}
}
