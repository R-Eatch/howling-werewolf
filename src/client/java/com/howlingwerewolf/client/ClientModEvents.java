package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModBlockEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.PackType;

public final class ClientModEvents {
    public static final ModelLayerLocation HUNTER_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "hunter"), "main");
    public static final ModelLayerLocation QUADRUPED_WEREWOLF_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "quadruped_werewolf"), "main");
    private static final String KEY_CATEGORY = "key.categories.howlingwerewolf";
    public static final KeyMapping OPEN_PROGRESSION = new KeyMapping(
            "key.howlingwerewolf.open_progression", InputConstants.Type.KEYSYM, InputConstants.KEY_K,
            KEY_CATEGORY);
    public static final KeyMapping TRANSFORM = new KeyMapping(
            "key.howlingwerewolf.transform", InputConstants.Type.KEYSYM, InputConstants.KEY_J,
            KEY_CATEGORY);
    public static final KeyMapping OPEN_COMPANIONS = new KeyMapping(
            "key.howlingwerewolf.open_companions", InputConstants.Type.KEYSYM, InputConstants.KEY_C,
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
        BlockRenderLayerMap.INSTANCE.putBlock(com.howlingwerewolf.content.ModBlocks.WOLFSBANE.get(), RenderType.cutout());
        for (KeyMapping key : new KeyMapping[]{OPEN_PROGRESSION,OPEN_COMPANIONS,TRANSFORM,BEAST_MODE,QUADRUPED_MODE,NIGHT_VISION,SUMMON_WOLF_SPIRIT,BLOODY_BITE,MOONBLOOD_SURGE})
            net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(key);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(HUNTER_LAYER,HunterModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(QUADRUPED_WEREWOLF_LAYER,QuadrupedWerewolfModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.HUNTER.get(),HunterRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.WEREWOLF.get(),WerewolfEntityRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.ALPHA_WEREWOLF.get(),AlphaWerewolfRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.ALPHA_MINION.get(), context ->
                new WerewolfNpcRenderer<>(context, com.howlingwerewolf.WerewolfForm.QUADRUPED, 0.5F));
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(ModEntities.WEREWOLF_COMPANION.get(),CompanionRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.RITUAL_ALTAR.get(), RitualAltarRenderer::new);
        var resources = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        resources.registerReloadListener(new ModernModelResources());
        resources.registerReloadListener(new WerewolfSkinCatalog());
    }
    private ClientModEvents() { }
}
