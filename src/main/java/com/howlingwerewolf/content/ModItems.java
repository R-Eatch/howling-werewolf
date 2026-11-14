package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeSpawnEggItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<Item> WEREWOLF_PELT = ITEMS.register("werewolf_pelt", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILVERHIDE = ITEMS.register("silverhide", () -> new Item(new Item.Properties()));
    private static final ArmorMaterial SILVERHIDE_MATERIAL = PeltArmor.material();
    public static final RegistryObject<ArmorItem> SILVERHIDE_HELMET = ITEMS.register("silverhide_helmet",
            () -> PeltArmor.piece(SILVERHIDE_MATERIAL, ArmorItem.Type.HELMET));
    public static final RegistryObject<ArmorItem> SILVERHIDE_CHESTPLATE = ITEMS.register("silverhide_chestplate",
            () -> PeltArmor.piece(SILVERHIDE_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<ArmorItem> SILVERHIDE_LEGGINGS = ITEMS.register("silverhide_leggings",
            () -> PeltArmor.piece(SILVERHIDE_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<ArmorItem> SILVERHIDE_BOOTS = ITEMS.register("silverhide_boots",
            () -> PeltArmor.piece(SILVERHIDE_MATERIAL, ArmorItem.Type.BOOTS));

    public static final RegistryObject<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MOONBANE_PEARL = ITEMS.register("moonbane_pearl", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WOLFSBANE_POTION = ITEMS.register("wolfsbane_potion", WolfsbanePotionItem::new);
    public static final RegistryObject<Item> WEREWOLF_POTION = ITEMS.register("werewolf_potion", WerewolfPotionItem::new);
    public static final RegistryObject<Item> ALPHA_WEREWOLF_BADGE = ITEMS.register("alpha_werewolf_badge",
            AlphaWerewolfBadgeItem::new);
    public static final RegistryObject<SwordItem> SILVER_SWORD = ITEMS.register("silver_sword",
            () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> WEREWOLF_SPAWN_EGG = ITEMS.register("werewolf_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WEREWOLF, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties()));
    public static final RegistryObject<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HUNTER, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties()));
    public static final RegistryObject<Item> ALPHA_WEREWOLF_SPAWN_EGG = ITEMS.register("alpha_werewolf_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALPHA_WEREWOLF, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties()));

    private ModItems() {}
}
