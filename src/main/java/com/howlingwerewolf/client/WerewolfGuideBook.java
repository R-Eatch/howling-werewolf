package com.howlingwerewolf.client;

import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** The tutorial's chapters and item entries. Recipes are read from the world, so datapack changes show. */
final class WerewolfGuideBook {
    /**
     * Each line is a translation key under {@code screen.howlingwerewolf.}: a leading "•" makes it a list
     * entry and a leading "!" a warning; the first plain line leads the chapter.
     */
    record Chapter(String id, String title, Supplier<ItemLike> icon, List<String> lines) {}
    record Entry(String id, Supplier<ItemLike> item) {}
    enum Method { CRAFT, SMELT, BREW }
    /** Ingredient slots in reading order (nine for crafting), each cycling through its alternatives. */
    record Shown(Method method, List<List<ItemStack>> slots, ItemStack result) {}

    static final List<Chapter> CHAPTERS = List.of(
            new Chapter("awakening", "guide.awakening.title", () -> Items.BONE, List.of("guide.awakening.intro",
                    "•guide.awakening.bite", "•guide.awakening.potion", "•guide.awakening.feral", "•guide.awakening.cure")),
            new Chapter("transform", "guide.transform.title", () -> Items.CLOCK, List.of("guide.transform.intro",
                    "•info.full_moon", "•info.night_vision", "•info.diet", "•guide.transform.keys")),
            new Chapter("traits", "guide.traits.title", () -> Items.IRON_CHESTPLATE, List.of("guide.traits.intro",
                    "•info.armor", "•info.fire", "•info.silver", "•guide.traits.hunters")),
            new Chapter("growth", "guide.growth.title", () -> Items.EXPERIENCE_BOTTLE, List.of("info.progression",
                    "•kill_xp_rule", "•info.level_scaling", "•guide.growth.points", "•guide.growth.badge")),
            new Chapter("quadruped", "guide.quadruped.title", () -> Items.LEATHER_BOOTS, List.of("info.quadruped",
                    "•guide.quadruped.key")),
            new Chapter("trial", "tab.trial", ModItems.ALPHA_WEREWOLF_BADGE::get, List.of("trial.intro",
                    "•trial.layout", "•trial.items", "•trial.requirements", "!trial.warning")),
            new Chapter("beast", "guide.beast.title", () -> Items.NETHER_STAR, List.of("info.beast",
                    "•guide.beast.key", "!info.beast_warning")),
            new Chapter("companions", "guide.companions.title", () -> Items.LEAD, List.of("guide.companions.intro",
                    "•guide.companions.summon", "•guide.companions.recruit", "•guide.companions.manage")),
            new Chapter("items", "guide.items.title", ModItems.MOONBANE_PEARL::get, List.of("guide.items.intro")));

    static final List<Entry> ITEMS = List.of(
            new Entry("wolfsbane", ModBlocks.WOLFSBANE::get),
            new Entry("silver_ore", ModBlocks.SILVER_ORE::get),
            new Entry("silver_ingot", ModItems.SILVER_INGOT::get),
            new Entry("silver_sword", ModItems.SILVER_SWORD::get),
            new Entry("wolfsbane_potion", ModItems.WOLFSBANE_POTION::get),
            new Entry("werewolf_potion", ModItems.WEREWOLF_POTION::get),
            new Entry("moonbane_pearl", ModItems.MOONBANE_PEARL::get),
            new Entry("alpha_werewolf_badge", ModItems.ALPHA_WEREWOLF_BADGE::get),
            new Entry("ritual_altar", ModBlocks.RITUAL_ALTAR::get),
            new Entry("central_ritual_altar", ModBlocks.CENTRAL_RITUAL_ALTAR::get));

    static int index(String id) {
        for (int i = 0; i < CHAPTERS.size(); i++) if (CHAPTERS.get(i).id().equals(id)) return i;
        return 0;
    }

    /** The chapter for where the player stands in the bloodline, marked on the chapter list. */
    static int stage(@Nullable WerewolfData data) {
        if (data == null || !data.isWerewolf()) return index("awakening");
        if (data.hasDefeatedAlpha()) return index("beast");
        return index(data.getLevel() >= 10 ? "trial" : "growth");
    }

    /** How an item is made: a crafting or smelting recipe from the world, or the two brews of this mod. */
    static @Nullable Shown recipe(ItemLike target) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        if (target.asItem() == ModItems.WOLFSBANE_POTION.get()) {
            return brew(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD), new ItemStack(ModBlocks.WOLFSBANE.get()), target);
        }
        if (target.asItem() == ModItems.WEREWOLF_POTION.get()) {
            return brew(new ItemStack(ModItems.WOLFSBANE_POTION.get()), new ItemStack(Items.FERMENTED_SPIDER_EYE), target);
        }
        var manager = level.getRecipeManager();
        for (Recipe<?> recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!recipe.getResultItem(level.registryAccess()).is(target.asItem())) continue;
            List<List<ItemStack>> slots = new ArrayList<>();
            for (int i = 0; i < 9; i++) slots.add(List.of());
            List<Ingredient> ingredients = recipe.getIngredients();
            int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
            for (int i = 0; i < ingredients.size() && i < 9; i++) {
                slots.set(i / width * 3 + i % width, List.of(ingredients.get(i).getItems()));
            }
            return new Shown(Method.CRAFT, slots, recipe.getResultItem(level.registryAccess()));
        }
        for (Recipe<?> recipe : manager.getAllRecipesFor(RecipeType.SMELTING)) {
            if (!recipe.getResultItem(level.registryAccess()).is(target.asItem()) || recipe.getIngredients().isEmpty()) continue;
            return new Shown(Method.SMELT, List.of(List.of(recipe.getIngredients().get(0).getItems())),
                    recipe.getResultItem(level.registryAccess()));
        }
        return null;
    }

    private static Shown brew(ItemStack base, ItemStack reagent, ItemLike result) {
        return new Shown(Method.BREW, List.of(List.of(reagent), List.of(base)), new ItemStack(result));
    }

    /** One alternative per second, so tags and multi-item ingredients show every choice. */
    static ItemStack cycle(List<ItemStack> choices) {
        return choices.isEmpty() ? ItemStack.EMPTY : choices.get((int) (net.minecraft.Util.getMillis() / 1000 % choices.size()));
    }

    private WerewolfGuideBook() {}
}
