package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeSpawnEggItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MOONBANE_PEARL = ITEMS.register("moonbane_pearl", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WOLFSBANE_POTION = ITEMS.register("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final RegistryObject<Item> WEREWOLF_POTION = ITEMS.register("werewolf_potion", WerewolfPotionItem::new);
    public static final RegistryObject<Item> ALPHA_WEREWOLF_BADGE = ITEMS.register("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final RegistryObject<SwordItem> SILVER_SWORD = ITEMS.register("silver_sword",
            () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> WEREWOLF_SPAWN_EGG = ITEMS.register("werewolf_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WEREWOLF, 0x211A18, 0x9E1F1F,
                    new Item.Properties()));
    public static final RegistryObject<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HUNTER, 0x2D2928, 0xB7B9BD,
                    new Item.Properties()));

    private ModItems() {}
}
