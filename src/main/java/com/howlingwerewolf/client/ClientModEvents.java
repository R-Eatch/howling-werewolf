package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModBlockEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final ModelLayerLocation HUNTER_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "hunter"), "main");
    public static final ModelLayerLocation QUADRUPED_WEREWOLF_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "quadruped_werewolf"), "main");
    public static final KeyMapping OPEN_PROGRESSION = new KeyMapping(
            "key.howlingwerewolf.open_progression", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K,
            "key.categories.howlingwerewolf");
    public static final KeyMapping TRANSFORM = new KeyMapping(
            "key.howlingwerewolf.transform", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J,
            "key.categories.howlingwerewolf");
    public static final KeyMapping BEAST_MODE = new KeyMapping(
            "key.howlingwerewolf.beast_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H,
            "key.categories.howlingwerewolf");
    public static final KeyMapping QUADRUPED_MODE = new KeyMapping(
            "key.howlingwerewolf.quadruped_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G,
            "key.categories.howlingwerewolf");
    public static final KeyMapping NIGHT_VISION = new KeyMapping(
            "key.howlingwerewolf.night_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V,
            "key.categories.howlingwerewolf");
    public static final KeyMapping SUMMON_WOLF_SPIRIT = new KeyMapping(
            "key.howlingwerewolf.summon_wolf_spirit", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N,
            "key.categories.howlingwerewolf");
    public static final KeyMapping BLOODY_BITE = new KeyMapping(
            "key.howlingwerewolf.bloody_bite", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B,
            "key.categories.howlingwerewolf");
    public static final KeyMapping MOONBLOOD_SURGE = new KeyMapping(
            "key.howlingwerewolf.moonblood_surge", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R,
            "key.categories.howlingwerewolf");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_PROGRESSION);
        event.register(TRANSFORM);
        event.register(BEAST_MODE);
        event.register(QUADRUPED_MODE);
        event.register(NIGHT_VISION);
        event.register(SUMMON_WOLF_SPIRIT);
        event.register(BLOODY_BITE);
        event.register(MOONBLOOD_SURGE);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HUNTER_LAYER, HunterModel::createBodyLayer);
        event.registerLayerDefinition(QUADRUPED_WEREWOLF_LAYER,
                QuadrupedWerewolfModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HUNTER.get(), HunterRenderer::new);
        event.registerEntityRenderer(ModEntities.ALPHA_WEREWOLF.get(), AlphaWerewolfRenderer::new);
        event.registerEntityRenderer(ModEntities.ALPHA_MINION.get(),
                net.minecraft.client.renderer.entity.WolfRenderer::new);
        event.registerEntityRenderer(ModEntities.WEREWOLF.get(), WerewolfEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RITUAL_ALTAR.get(),
                RitualAltarRenderer::new);
    }

    private ClientModEvents() {}
}
