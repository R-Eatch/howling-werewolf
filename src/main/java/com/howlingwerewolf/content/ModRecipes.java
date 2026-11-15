package com.howlingwerewolf.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public final class ModRecipes {
    public static final ModRegistry<RecipeSerializer<?>> SERIALIZERS =
            new ModRegistry<>(BuiltInRegistries.RECIPE_SERIALIZER);

    public static final Supplier<RecipeSerializer<AlphaWerewolfBadgeRecipe>> ALPHA_WEREWOLF_BADGE =
            SERIALIZERS.register("alpha_werewolf_badge",
                    () -> new RecipeSerializer<>(MapCodec.unit(AlphaWerewolfBadgeRecipe.INSTANCE),
                            StreamCodec.unit(AlphaWerewolfBadgeRecipe.INSTANCE)));

    private ModRecipes() {}
}
