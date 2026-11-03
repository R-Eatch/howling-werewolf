package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModBlockEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class ClientModEvents {
    public static final ModelLayerLocation HUNTER_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "hunter"), "main");
    public static final ModelLayerLocation QUADRUPED_WEREWOLF_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "quadruped_werewolf"), "main");
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "howlingwerewolf"));
    public static final KeyMapping OPEN_PROGRESSION = new KeyMapping(
            "key.howlingwerewolf.open_progression", InputConstants.Type.KEYSYM, InputConstants.KEY_K,
            KEY_CATEGORY);
    public static final KeyMapping TRANSFORM = new KeyMapping(
            "key.howlingwerewolf.transform", InputConstants.Type.KEYSYM, InputConstants.KEY_J,
            KEY_CATEGORY);
    public static final KeyMapping BEAST_MODE = new KeyMapping(
            "key.howlingwerewolf.beast_mode", InputConstants.Type.KEYSYM, InputConstants.KEY_H,
            KEY_CATEGORY);
    public static final KeyMapping QUADRUPED_MODE = new KeyMapping(
            "key.howlingwerewolf.quadruped_mode", InputConstants.Type.KEYSYM, InputConstants.KEY_G,
            KEY_CATEGORY);
    public static final KeyMapping NIGHT_VISION = new KeyMapping(
            "key.howlingwerewolf.night_vision", InputConstants.Type.KEYSYM, InputConstants.KEY_V,
            KEY_CATEGORY);
    public static final KeyMapping SUMMON_WOLF_SPIRIT = new KeyMapping(
            "key.howlingwerewolf.summon_wolf_spirit", InputConstants.Type.KEYSYM, InputConstants.KEY_N,
            KEY_CATEGORY);
    public static final KeyMapping BLOODY_BITE = new KeyMapping(
            "key.howlingwerewolf.bloody_bite", InputConstants.Type.KEYSYM, InputConstants.KEY_B,
            KEY_CATEGORY);
    public static final KeyMapping MOONBLOOD_SURGE = new KeyMapping(
            "key.howlingwerewolf.moonblood_surge", InputConstants.Type.KEYSYM, InputConstants.KEY_R,
            KEY_CATEGORY);

    public static void initialize() {
        net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlock(
                com.howlingwerewolf.content.ModBlocks.WOLFSBANE.get(),net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
        for (KeyMapping key : new KeyMapping[]{OPEN_PROGRESSION,TRANSFORM,BEAST_MODE,QUADRUPED_MODE,NIGHT_VISION,SUMMON_WOLF_SPIRIT,BLOODY_BITE,MOONBLOOD_SURGE})
            net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(key);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(HUNTER_LAYER,HunterModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(QUADRUPED_WEREWOLF_LAYER,QuadrupedWerewolfModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUNTER.get(),HunterRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.WEREWOLF.get(),WerewolfEntityRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.ALPHA_WEREWOLF.get(),AlphaWerewolfRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.ALPHA_MINION.get(),AlphaMinionRenderer::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ModBlockEntities.RITUAL_ALTAR.get(),RitualAltarRenderer::new);
        var loader=net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES);
        var models=Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID,"modern_models");
        loader.registerReloader(models,new ModernModelResources());
        loader.registerReloader(Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID,"werewolf_skins"),new WerewolfSkinCatalog());
        loader.addReloaderOrdering(net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Client.TEXTURES,models);
        loader.addReloaderOrdering(models,net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Client.ENTITY_RENDERERS);
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type,renderer,helper,context) -> {
            if (type==net.minecraft.world.entity.EntityType.PLAYER) ClientEvents.initializeRenderers(context);
        });
    }
    private ClientModEvents() { }
}
