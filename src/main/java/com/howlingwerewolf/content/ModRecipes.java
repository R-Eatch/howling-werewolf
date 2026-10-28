package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HowlingWerewolf.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlphaWerewolfBadgeRecipe>> ALPHA_WEREWOLF_BADGE =
            SERIALIZERS.register("alpha_werewolf_badge",
                    // This stateless recipe must use the same instance for JSON loading and
                    // network sync: StreamCodec.unit rejects other recipe instances.
                    () -> new RecipeSerializer<>(MapCodec.unit(AlphaWerewolfBadgeRecipe.INSTANCE),
                            StreamCodec.unit(AlphaWerewolfBadgeRecipe.INSTANCE)));

    private ModRecipes() {}
}
