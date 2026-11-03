package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public final class ModRecipes {
    public static final ModRegistry<RecipeSerializer<?>> SERIALIZERS =
            new ModRegistry<>(BuiltInRegistries.RECIPE_SERIALIZER);

    public static final Supplier<RecipeSerializer<AlphaWerewolfBadgeRecipe>> ALPHA_WEREWOLF_BADGE =
            SERIALIZERS.register("alpha_werewolf_badge",
                    // This stateless recipe must use the same instance for JSON loading and
                    // network sync: StreamCodec.unit rejects other recipe instances.
                    () -> new RecipeSerializer<AlphaWerewolfBadgeRecipe>() {
                        @Override public MapCodec<AlphaWerewolfBadgeRecipe> codec() { return MapCodec.unit(AlphaWerewolfBadgeRecipe.INSTANCE); }
                        @Override public StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,AlphaWerewolfBadgeRecipe> streamCodec() { return StreamCodec.unit(AlphaWerewolfBadgeRecipe.INSTANCE); }
                    });

    private ModRecipes() {}
}
