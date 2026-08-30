package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HowlingWerewolf.MOD_ID);

    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MOONBANE_PEARL = ITEMS.register("moonbane_pearl", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WOLFSBANE_POTION = ITEMS.register("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final DeferredItem<Item> WEREWOLF_POTION = ITEMS.register("werewolf_potion", WerewolfPotionItem::new);
    public static final DeferredItem<Item> ALPHA_WEREWOLF_BADGE = ITEMS.register("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final DeferredItem<SwordItem> SILVER_SWORD = ITEMS.register("silver_sword",
            () -> new SwordItem(Tiers.IRON, new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))));
    public static final DeferredItem<Item> WEREWOLF_SPAWN_EGG = ITEMS.register("werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.WEREWOLF, 0x211A18, 0x9E1F1F,
                    new Item.Properties()));
    public static final DeferredItem<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HUNTER, 0x2D2928, 0xB7B9BD,
                    new Item.Properties()));

    private ModItems() {}
}
