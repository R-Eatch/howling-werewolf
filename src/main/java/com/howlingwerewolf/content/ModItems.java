package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HowlingWerewolf.MOD_ID);

    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.registerSimpleItem("silver_ingot");
    public static final DeferredItem<Item> MOONBANE_PEARL = ITEMS.registerSimpleItem("moonbane_pearl");
    public static final DeferredItem<Item> WOLFSBANE_POTION = ITEMS.registerItem("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final DeferredItem<Item> WEREWOLF_POTION = ITEMS.registerItem("werewolf_potion", WerewolfPotionItem::new);
    public static final DeferredItem<Item> ALPHA_WEREWOLF_BADGE = ITEMS.registerItem("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final DeferredItem<Item> SILVER_SWORD = ITEMS.registerSimpleItem("silver_sword",
            props -> props.sword(ToolMaterial.IRON, 3.0F, -2.4F));
    public static final DeferredItem<Item> WEREWOLF_SPAWN_EGG = ITEMS.registerItem("werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.WEREWOLF.get())));
    public static final DeferredItem<Item> HUNTER_SPAWN_EGG = ITEMS.registerItem("hunter_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.HUNTER.get())));
    public static final DeferredItem<Item> ALPHA_WEREWOLF_SPAWN_EGG = ITEMS.registerItem("alpha_werewolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ALPHA_WEREWOLF.get())));

    private ModItems() {}
}
