package com.howlingwerewolf.content;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.function.Supplier;

public final class ModItems {
    public static final ModRegistry.Items ITEMS = new ModRegistry.Items();

    public static final Supplier<Item> WEREWOLF_PELT = ITEMS.registerSimpleItem("werewolf_pelt");
    public static final Supplier<Item> SILVERHIDE = ITEMS.registerSimpleItem("silverhide");
    public static final Supplier<Item> SILVERHIDE_HELMET = ITEMS.registerItem("silverhide_helmet",
            props -> PeltArmor.piece(props, ArmorType.HELMET));
    public static final Supplier<Item> SILVERHIDE_CHESTPLATE = ITEMS.registerItem("silverhide_chestplate",
            props -> PeltArmor.piece(props, ArmorType.CHESTPLATE));
    public static final Supplier<Item> SILVERHIDE_LEGGINGS = ITEMS.registerItem("silverhide_leggings",
            props -> PeltArmor.piece(props, ArmorType.LEGGINGS));
    public static final Supplier<Item> SILVERHIDE_BOOTS = ITEMS.registerItem("silverhide_boots",
            props -> PeltArmor.piece(props, ArmorType.BOOTS));

    public static final Supplier<Item> SILVER_INGOT = ITEMS.registerSimpleItem("silver_ingot");
    public static final Supplier<Item> MOONBANE_PEARL = ITEMS.registerSimpleItem("moonbane_pearl");
    public static final Supplier<Item> WOLFSBANE_POTION = ITEMS.registerItem("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final Supplier<Item> WEREWOLF_POTION = ITEMS.registerItem("werewolf_potion", WerewolfPotionItem::new);
    public static final Supplier<Item> ALPHA_WEREWOLF_BADGE = ITEMS.registerItem("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final Supplier<Item> SILVER_SWORD = ITEMS.registerItem("silver_sword",
            props -> new Item(props.sword(ToolMaterial.IRON, 3.0F, -2.4F)));
    public static final Supplier<Item> WEREWOLF_SPAWN_EGG = ITEMS.registerItem("werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.WEREWOLF.get())));
    public static final Supplier<Item> HUNTER_SPAWN_EGG = ITEMS.registerItem("hunter_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.HUNTER.get())));
    public static final Supplier<Item> ALPHA_WEREWOLF_SPAWN_EGG = ITEMS.registerItem("alpha_werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ALPHA_WEREWOLF.get())));

    public static final Supplier<Item> HUNTER_CAPTAIN_SPAWN_EGG = ITEMS.registerItem("hunter_captain_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.HUNTER_CAPTAIN.get())));
    public static final Supplier<Item> TRIBE_CHIEF_SPAWN_EGG = ITEMS.registerItem("tribe_chief_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.TRIBE_CHIEF.get())));
    public static final Supplier<Item> TRIBE_WEREWOLF_SPAWN_EGG = ITEMS.registerItem("tribe_werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.TRIBE_WEREWOLF.get())));
    public static final Supplier<Item> HUNTER_CROSSBOW = ITEMS.registerItem("hunter_crossbow", HunterCrossbowItem::new);

    public static final java.util.List<Supplier<Item>> ITEMS_LIST = java.util.List.of(
            SILVER_INGOT, MOONBANE_PEARL, SILVER_SWORD, HUNTER_CROSSBOW, WOLFSBANE_POTION, WEREWOLF_POTION,
            WEREWOLF_PELT, SILVERHIDE, SILVERHIDE_HELMET, SILVERHIDE_CHESTPLATE, SILVERHIDE_LEGGINGS, SILVERHIDE_BOOTS,
            ALPHA_WEREWOLF_BADGE, WEREWOLF_SPAWN_EGG, HUNTER_SPAWN_EGG, ALPHA_WEREWOLF_SPAWN_EGG,
            HUNTER_CAPTAIN_SPAWN_EGG, TRIBE_CHIEF_SPAWN_EGG, TRIBE_WEREWOLF_SPAWN_EGG);
    private ModItems() {}
}
