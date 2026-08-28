package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Places the main-hand stack between the quadruped werewolf's jaws, fox-style. */
public final class QuadrupedHeldItemLayer
        extends RenderLayer<AbstractClientPlayer, QuadrupedWerewolfModel> {
    private final ItemInHandRenderer itemInHandRenderer;

    public QuadrupedHeldItemLayer(
            RenderLayerParent<AbstractClientPlayer, QuadrupedWerewolfModel> parent,
            ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        getParentModel().translateToMouth(poseStack);
        poseStack.translate(0.0D, 0.02D, -0.38D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.72F, 0.72F, 0.72F);
        itemInHandRenderer.renderItem(player, stack, ItemDisplayContext.GROUND, false,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
