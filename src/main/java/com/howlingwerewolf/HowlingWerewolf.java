/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf;
import com.howlingwerewolf.content.*;
import com.howlingwerewolf.event.*;
import com.howlingwerewolf.network.ModNetwork;
import com.howlingwerewolf.capability.ModAttachments;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
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
        TrapezoidIntProvider.initialize();
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
        ModEntityEvents.register();
        ModNetwork.register();
        FabricEvents.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
            com.howlingwerewolf.command.WerewolfCommands.register(dispatcher));
    }
}
