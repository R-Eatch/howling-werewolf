package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.howlingwerewolf.WerewolfForm;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/** Standalone renderer for the original Howling Werewolf model. */
public final class WerewolfPlayerRenderer
        extends LivingEntityRenderer<AbstractClientPlayer, WerewolfPlayerModel<AbstractClientPlayer>> {
    public WerewolfPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfPlayerModel<>(WerewolfPlayerModel.createBodyLayer().bakeRoot()), 0.55F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
        WerewolfRenderTransforms.applyPlayerTravelPose(player, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return WerewolfSkinTextures.get(WerewolfSkinRenderContext.getSkinId(player), WerewolfForm.WEREWOLF);
    }

    @Override
    protected boolean shouldShowName(AbstractClientPlayer player) {
        return !WerewolfSkinRenderContext.isPreview(player) && super.shouldShowName(player);
    }
}
