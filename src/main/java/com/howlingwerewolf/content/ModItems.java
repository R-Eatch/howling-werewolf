package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HowlingWerewolf.MOD_ID);

    public static final DeferredItem<Item> WEREWOLF_PELT = ITEMS.registerSimpleItem("werewolf_pelt");
    public static final DeferredItem<Item> SILVERHIDE = ITEMS.registerSimpleItem("silverhide");
    public static final DeferredItem<Item> SILVERHIDE_HELMET = ITEMS.registerItem("silverhide_helmet",
            props -> PeltArmor.piece(props, ArmorType.HELMET));
    public static final DeferredItem<Item> SILVERHIDE_CHESTPLATE = ITEMS.registerItem("silverhide_chestplate",
            props -> PeltArmor.piece(props, ArmorType.CHESTPLATE));
    public static final DeferredItem<Item> SILVERHIDE_LEGGINGS = ITEMS.registerItem("silverhide_leggings",
            props -> PeltArmor.piece(props, ArmorType.LEGGINGS));
    public static final DeferredItem<Item> SILVERHIDE_BOOTS = ITEMS.registerItem("silverhide_boots",
            props -> PeltArmor.piece(props, ArmorType.BOOTS));

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
