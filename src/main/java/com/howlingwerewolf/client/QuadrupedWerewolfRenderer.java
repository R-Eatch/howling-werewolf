package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public final class QuadrupedWerewolfRenderer
        extends LivingEntityRenderer<AbstractClientPlayer, QuadrupedWerewolfModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            HowlingWerewolf.MOD_ID, "textures/entity/quadruped_werewolf.png");

    public QuadrupedWerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new QuadrupedWerewolfModel(
                context.bakeLayer(ClientModEvents.QUADRUPED_WEREWOLF_LAYER)), 0.5F);
        addLayer(new QuadrupedHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
        WerewolfRenderTransforms.applyQuadrupedTravelPose(player, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return TEXTURE;
    }
}
