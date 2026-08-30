package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public final class BeastPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, BeastPlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation BEAST_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/beast.png");

    public BeastPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastPlayerModel<>(BeastPlayerModel.createBodyLayer().bakeRoot()), 0.85F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick, float scale) {
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick, scale);
        WerewolfRenderTransforms.applyPlayerTravelPose(player, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return BEAST_TEXTURE;
    }
}
