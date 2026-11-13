/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf;
import com.howlingwerewolf.content.*;
import com.howlingwerewolf.event.*;
import com.howlingwerewolf.network.ModNetwork;
import com.howlingwerewolf.capability.ModAttachments;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class HowlingWerewolf implements ModInitializer {
    public static final String MOD_ID = "howlingwerewolf";
    public static final Logger LOGGER = LogUtils.getLogger();
    @Override public void onInitialize() {
        HWConfig.load();
        ModItems.ITEMS.getClass();
        ModBlocks.BLOCKS.getClass();
        ModEntities.ENTITIES.getClass();
        ModBlockEntities.BLOCK_ENTITIES.getClass();
        ModRecipes.SERIALIZERS.getClass();
        ModPlacementModifiers.PLACEMENT_MODIFIERS.getClass();
        ModSounds.initialize();
        ModAttachments.initialize();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModRegistry.id("main"), FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.howlingwerewolf"))
            .icon(() -> new ItemStack(ModItems.MOONBANE_PEARL.get()))
            .displayItems((parameters, output) -> {
                ModItems.ITEMS_LIST.forEach(item -> output.accept(item.get()));
                output.accept(ModBlocks.SILVER_ORE.get()); output.accept(ModBlocks.DEEPSLATE_SILVER_ORE.get());
                output.accept(ModBlocks.WOLFSBANE.get()); output.accept(ModBlocks.RITUAL_ALTAR.get());
                output.accept(ModBlocks.CENTRAL_RITUAL_ALTAR.get());
            }).build());
        addToVanillaCreativeTabs();
        ModEntityEvents.register();
        ModNetwork.register();
        FabricEvents.register();
        com.howlingwerewolf.companion.CompanionEvents.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
            com.howlingwerewolf.command.WerewolfCommands.register(dispatcher));
    }

    /** Fabric pages modded tabs after the vanilla ones, so the first page also lists every item by category. */
    private static void addToVanillaCreativeTabs() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(ModItems.SILVER_INGOT.get()); entries.accept(ModItems.MOONBANE_PEARL.get());
            entries.accept(ModItems.ALPHA_WEREWOLF_BADGE.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> entries.accept(ModItems.SILVER_SWORD.get()));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.accept(ModItems.WOLFSBANE_POTION.get()); entries.accept(ModItems.WEREWOLF_POTION.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(ModItems.WEREWOLF_SPAWN_EGG.get()); entries.accept(ModItems.HUNTER_SPAWN_EGG.get());
            entries.accept(ModItems.ALPHA_WEREWOLF_SPAWN_EGG.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(ModBlocks.SILVER_ORE.get()); entries.accept(ModBlocks.DEEPSLATE_SILVER_ORE.get());
            entries.accept(ModBlocks.WOLFSBANE.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(ModBlocks.RITUAL_ALTAR.get()); entries.accept(ModBlocks.CENTRAL_RITUAL_ALTAR.get());
        });
    }
}
