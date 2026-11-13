package com.howlingwerewolf.content;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class ModItems {
    public static final ModRegistry.Items ITEMS = new ModRegistry.Items();

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
            ALPHA_WEREWOLF_BADGE, WEREWOLF_SPAWN_EGG, HUNTER_SPAWN_EGG, ALPHA_WEREWOLF_SPAWN_EGG);
    private ModItems() {}
}
