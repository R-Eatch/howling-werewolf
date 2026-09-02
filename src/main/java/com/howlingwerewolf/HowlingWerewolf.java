/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf;

import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModBlockEntities;
import com.howlingwerewolf.content.ModBiomeModifiers;
import com.howlingwerewolf.content.ModPlacementModifiers;
import com.howlingwerewolf.content.ModRecipes;
import com.howlingwerewolf.content.WerewolfPotionBrewingRecipe;
import com.howlingwerewolf.content.WolfsbaneBrewingRecipe;
import com.howlingwerewolf.capability.ModAttachments;
import com.howlingwerewolf.event.ModEntityEvents;
import com.howlingwerewolf.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(HowlingWerewolf.MOD_ID)
public final class HowlingWerewolf {
    public static final String MOD_ID = "howlingwerewolf";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.howlingwerewolf"))
                    .icon(() -> new ItemStack(ModItems.MOONBANE_PEARL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SILVER_INGOT.get());
                        output.accept(ModItems.MOONBANE_PEARL.get());
                        output.accept(ModItems.SILVER_SWORD.get());
                        output.accept(ModItems.WOLFSBANE_POTION.get());
                        output.accept(ModItems.WEREWOLF_POTION.get());
                        output.accept(ModItems.ALPHA_WEREWOLF_BADGE.get());
                        output.accept(ModItems.WEREWOLF_SPAWN_EGG.get());
                        output.accept(ModItems.HUNTER_SPAWN_EGG.get());
                        output.accept(ModBlocks.SILVER_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_SILVER_ORE.get());
                        output.accept(ModBlocks.WOLFSBANE.get());
                        output.accept(ModBlocks.RITUAL_ALTAR.get());
                        output.accept(ModBlocks.CENTRAL_RITUAL_ALTAR.get());
                    }).build());

    public HowlingWerewolf(IEventBus modBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModPlacementModifiers.PLACEMENT_MODIFIERS.register(modBus);
        ModBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(ModEntityEvents::registerSpawnPlacements);
        modBus.addListener(ModNetwork::register);
        NeoForge.EVENT_BUS.addListener(HowlingWerewolf::registerBrewingRecipes);
        modContainer.registerConfig(ModConfig.Type.COMMON, HWConfig.SPEC);
    }

    private static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(new WolfsbaneBrewingRecipe());
        event.getBuilder().addRecipe(new WerewolfPotionBrewingRecipe());
    }
}
