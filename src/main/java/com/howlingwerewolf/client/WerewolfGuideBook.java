package com.howlingwerewolf.client;

import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Comparator;
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
    enum Method { CRAFT, SMELT, BLAST, BREW }
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
            new Entry("deepslate_silver_ore", ModBlocks.DEEPSLATE_SILVER_ORE::get),
            new Entry("silver_ingot", ModItems.SILVER_INGOT::get),
            new Entry("silver_sword", ModItems.SILVER_SWORD::get),
            new Entry("wolfsbane_potion", ModItems.WOLFSBANE_POTION::get),
            new Entry("werewolf_potion", ModItems.WEREWOLF_POTION::get),
            new Entry("moonbane_pearl", ModItems.MOONBANE_PEARL::get),
            new Entry("alpha_werewolf_badge", ModItems.ALPHA_WEREWOLF_BADGE::get),
            new Entry("ritual_altar", ModBlocks.RITUAL_ALTAR::get),
            new Entry("central_ritual_altar", ModBlocks.CENTRAL_RITUAL_ALTAR::get));

    /** Read crafting and cooking recipes from the world. The mod's two brews are registered separately. */
    static List<Shown> recipes(ItemLike target) {
        var level = Minecraft.getInstance().level;
        if (level == null) return List.of();
        if (target.asItem() == ModItems.WOLFSBANE_POTION.get()) {
            return List.of(brew(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD),
                    new ItemStack(ModBlocks.WOLFSBANE.get()), target));
        }
        if (target.asItem() == ModItems.WEREWOLF_POTION.get()) {
            return List.of(brew(new ItemStack(ModItems.WOLFSBANE_POTION.get()),
                    new ItemStack(Items.FERMENTED_SPIDER_EYE), target));
        }
        var manager = level.getRecipeManager();
        RegistryAccess registries = level.registryAccess();
        List<Shown> shown = new ArrayList<>();
        for (Recipe<?> recipe : manager.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .sorted(Comparator.comparing(Recipe::getId)).toList()) {
            ItemStack result = recipe.getResultItem(registries);
            if (!result.is(target.asItem())) continue;
            List<Ingredient> ingredients = recipe.getIngredients();
            int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
            if (width < 1 || width > 3 || ingredients.isEmpty() || ingredients.size() > width * 3) continue;
            List<List<ItemStack>> slots = new ArrayList<>();
            for (int i = 0; i < 9; i++) slots.add(List.of());
            for (int i = 0; i < ingredients.size(); i++) {
                slots.set(i / width * 3 + i % width, List.of(ingredients.get(i).getItems()));
            }
            shown.add(new Shown(Method.CRAFT, slots, result));
        }
        addCooking(shown, manager.getAllRecipesFor(RecipeType.SMELTING), Method.SMELT, target, registries);
        addCooking(shown, manager.getAllRecipesFor(RecipeType.BLASTING), Method.BLAST, target, registries);
        return List.copyOf(shown);
    }

    private static void addCooking(List<Shown> shown, List<? extends AbstractCookingRecipe> recipes,
                                   Method method, ItemLike target, RegistryAccess registries) {
        for (AbstractCookingRecipe recipe : recipes.stream().sorted(Comparator.comparing(Recipe::getId)).toList()) {
            ItemStack result = recipe.getResultItem(registries);
            if (!result.is(target.asItem()) || recipe.getIngredients().isEmpty()) continue;
            shown.add(new Shown(method, List.of(List.of(recipe.getIngredients().get(0).getItems())), result));
        }
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
