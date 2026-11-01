package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Items;

final class ModernWingsLayer extends RenderLayer<AbstractClientPlayer, ModernPlayerModel<AbstractClientPlayer>> {
    private final ElytraModel<AbstractClientPlayer> wings;
    private final ModelPart root;
    private final float[] back;

    ModernWingsLayer(ModernPlayerRenderer parent, EntityRendererProvider.Context context) {
        super(parent);
        root = context.bakeLayer(ModelLayers.ELYTRA);
        wings = new ElytraModel<>(root);
        back = ModernEquipmentLayer.attachmentBounds(parent.getModel().rig.bone("ribs"));
    }

    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                 float limb, float speed, float partial, float age, float yaw, float pitch) {
        var stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ModernEquipmentLayer.enabled(player) || !stack.is(Items.ELYTRA)) return;
        var skin = player.getSkin();
        ResourceLocation texture = skin.elytraTexture() != null ? skin.elytraTexture()
                : skin.capeTexture() != null && player.isModelPartShown(PlayerModelPart.CAPE) ? skin.capeTexture()
                : ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");
        wings.setupAnim(player, limb, speed, age, yaw, pitch);
        root.getChild("left_wing").y = root.getChild("right_wing").y = 0;
        pose.pushPose();
        getParentModel().rig.transform("ribs", pose);
        pose.translate(0, back[1] / 16, back[5] / 16 + 0.03F);
        wings.renderToBuffer(pose, ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(texture), stack.hasFoil()),
                light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
