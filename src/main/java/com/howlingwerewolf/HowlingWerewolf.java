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
import com.howlingwerewolf.network.ModNetwork;
import com.howlingwerewolf.event.ModEntityEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(HowlingWerewolf.MOD_ID)
public final class HowlingWerewolf {
    public static final String MOD_ID = "howlingwerewolf";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
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

    public HowlingWerewolf() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModPlacementModifiers.PLACEMENT_MODIFIERS.register(modBus);
        ModBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HWConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            ModEntityEvents.registerSpawnPlacements();
            BrewingRecipeRegistry.addRecipe(new WolfsbaneBrewingRecipe());
            BrewingRecipeRegistry.addRecipe(new WerewolfPotionBrewingRecipe());
        });
    }
}
