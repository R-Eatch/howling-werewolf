package com.howlingwerewolf.content;

import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class ModItems {
    public static final ModRegistry.Items ITEMS = new ModRegistry.Items();

    private static final Holder<ArmorMaterial> SILVERHIDE_ARMOR = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL, ModRegistry.id("silverhide"), PeltArmor.material());
    public static final Supplier<Item> WEREWOLF_PELT = ITEMS.registerSimpleItem("werewolf_pelt");
    public static final Supplier<Item> SILVERHIDE = ITEMS.registerSimpleItem("silverhide");
    public static final Supplier<Item> SILVERHIDE_HELMET = ITEMS.register("silverhide_helmet", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.HELMET));
    public static final Supplier<Item> SILVERHIDE_CHESTPLATE = ITEMS.register("silverhide_chestplate", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.CHESTPLATE));
    public static final Supplier<Item> SILVERHIDE_LEGGINGS = ITEMS.register("silverhide_leggings", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.LEGGINGS));
    public static final Supplier<Item> SILVERHIDE_BOOTS = ITEMS.register("silverhide_boots", () -> PeltArmor.piece(SILVERHIDE_ARMOR, ArmorItem.Type.BOOTS));

    public static final Supplier<Item> SILVER_INGOT = ITEMS.registerSimpleItem("silver_ingot");
    public static final Supplier<Item> MOONBANE_PEARL = ITEMS.registerSimpleItem("moonbane_pearl");
    public static final Supplier<Item> WOLFSBANE_POTION = ITEMS.register("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final Supplier<Item> WEREWOLF_POTION = ITEMS.register("werewolf_potion", WerewolfPotionItem::new);
    public static final Supplier<Item> ALPHA_WEREWOLF_BADGE = ITEMS.register("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final Supplier<Item> SILVER_SWORD = ITEMS.registerItem("silver_sword", props -> new SwordItem(Tiers.IRON,
            props.attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))));
    public static final Supplier<Item> WEREWOLF_SPAWN_EGG = ITEMS.registerItem("werewolf_spawn_egg",
            props -> new SpawnEggItem(ModEntities.WEREWOLF.get(), 0xFFFFFF, 0xFFFFFF, props));
    public static final Supplier<Item> HUNTER_SPAWN_EGG = ITEMS.registerItem("hunter_spawn_egg",
            props -> new SpawnEggItem(ModEntities.HUNTER.get(), 0xFFFFFF, 0xFFFFFF, props));
    public static final Supplier<Item> ALPHA_WEREWOLF_SPAWN_EGG = ITEMS.registerItem("alpha_werewolf_spawn_egg",
            props -> new SpawnEggItem(ModEntities.ALPHA_WEREWOLF.get(), 0xFFFFFF, 0xFFFFFF, props));

    public static final java.util.List<Supplier<Item>> ITEMS_LIST = java.util.List.of(
            SILVER_INGOT, MOONBANE_PEARL, SILVER_SWORD, WOLFSBANE_POTION, WEREWOLF_POTION,
            WEREWOLF_PELT, SILVERHIDE, SILVERHIDE_HELMET, SILVERHIDE_CHESTPLATE, SILVERHIDE_LEGGINGS, SILVERHIDE_BOOTS,
            ALPHA_WEREWOLF_BADGE, WEREWOLF_SPAWN_EGG, HUNTER_SPAWN_EGG, ALPHA_WEREWOLF_SPAWN_EGG);
    private ModItems() {}
}
