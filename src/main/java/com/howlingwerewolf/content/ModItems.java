package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HowlingWerewolf.MOD_ID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, HowlingWerewolf.MOD_ID);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> SILVERHIDE_ARMOR = ARMOR_MATERIALS.register("silverhide", PeltArmor::material);
    public static final DeferredItem<Item> WEREWOLF_PELT = ITEMS.register("werewolf_pelt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SILVERHIDE = ITEMS.register("silverhide", () -> new Item(new Item.Properties()));
    public static final DeferredItem<ArmorItem> SILVERHIDE_HELMET = ITEMS.register("silverhide_helmet", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.HELMET));
    public static final DeferredItem<ArmorItem> SILVERHIDE_CHESTPLATE = ITEMS.register("silverhide_chestplate", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.CHESTPLATE));
    public static final DeferredItem<ArmorItem> SILVERHIDE_LEGGINGS = ITEMS.register("silverhide_leggings", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.LEGGINGS));
    public static final DeferredItem<ArmorItem> SILVERHIDE_BOOTS = ITEMS.register("silverhide_boots", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.BOOTS));

    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MOONBANE_PEARL = ITEMS.register("moonbane_pearl", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WOLFSBANE_POTION = ITEMS.register("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final DeferredItem<Item> WEREWOLF_POTION = ITEMS.register("werewolf_potion", WerewolfPotionItem::new);
    public static final DeferredItem<Item> ALPHA_WEREWOLF_BADGE = ITEMS.register("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final DeferredItem<SwordItem> SILVER_SWORD = ITEMS.register("silver_sword",
            () -> new SwordItem(Tiers.IRON, new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))));
    public static final DeferredItem<HunterCrossbowItem> HUNTER_CROSSBOW = ITEMS.register("hunter_crossbow",
            () -> new HunterCrossbowItem(new Item.Properties()));
    // Neutral tints preserve the dedicated full-color spawn egg textures.
    public static final DeferredItem<Item> WEREWOLF_SPAWN_EGG = ITEMS.register("werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.WEREWOLF, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties()));
    public static final DeferredItem<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HUNTER, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties()));
    public static final DeferredItem<Item> ALPHA_WEREWOLF_SPAWN_EGG = ITEMS.register("alpha_werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ALPHA_WEREWOLF, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties()));
    public static final DeferredItem<Item> HUNTER_CAPTAIN_SPAWN_EGG = ITEMS.register("hunter_captain_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HUNTER_CAPTAIN, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<Item> TRIBE_CHIEF_SPAWN_EGG = ITEMS.register("tribe_chief_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TRIBE_CHIEF, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<Item> TRIBE_WEREWOLF_SPAWN_EGG = ITEMS.register("tribe_werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TRIBE_WEREWOLF, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    private ModItems() {}
}
