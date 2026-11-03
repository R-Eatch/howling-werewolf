package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;

public final class ModItems {
    public static final ModRegistry.Items ITEMS = new ModRegistry.Items();

    public static final Supplier<Item> SILVER_INGOT = ITEMS.registerSimpleItem("silver_ingot");
    public static final Supplier<Item> MOONBANE_PEARL = ITEMS.registerSimpleItem("moonbane_pearl");
    public static final Supplier<Item> WOLFSBANE_POTION = ITEMS.registerItem("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final Supplier<Item> WEREWOLF_POTION = ITEMS.registerItem("werewolf_potion", WerewolfPotionItem::new);
    public static final Supplier<Item> ALPHA_WEREWOLF_BADGE = ITEMS.registerItem("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final Supplier<Item> SILVER_SWORD = ITEMS.registerSimpleItem("silver_sword",
            props -> props.sword(ToolMaterial.IRON, 3.0F, -2.4F));
    public static final Supplier<Item> WEREWOLF_SPAWN_EGG = ITEMS.registerItem("werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.WEREWOLF.get())));
    public static final Supplier<Item> HUNTER_SPAWN_EGG = ITEMS.registerItem("hunter_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.HUNTER.get())));
    public static final Supplier<Item> ALPHA_WEREWOLF_SPAWN_EGG = ITEMS.registerItem("alpha_werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ALPHA_WEREWOLF.get())));

    public static final java.util.List<Supplier<Item>> ITEMS_LIST = java.util.List.of(
            SILVER_INGOT, MOONBANE_PEARL, SILVER_SWORD, WOLFSBANE_POTION, WEREWOLF_POTION,
            ALPHA_WEREWOLF_BADGE, WEREWOLF_SPAWN_EGG, HUNTER_SPAWN_EGG, ALPHA_WEREWOLF_SPAWN_EGG);
    private ModItems() {}
}
