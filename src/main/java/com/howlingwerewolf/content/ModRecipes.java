package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<RecipeSerializer<AlphaWerewolfBadgeRecipe>> ALPHA_WEREWOLF_BADGE =
            SERIALIZERS.register("alpha_werewolf_badge",
                    () -> new SimpleCraftingRecipeSerializer<>(AlphaWerewolfBadgeRecipe::new));

    private ModRecipes() {}
}
