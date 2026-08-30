package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Reuses the Beast geometry and attack animation for the trial's final opponent. */
public final class AlphaWerewolfRenderer
        extends LivingEntityRenderer<AlphaWerewolfEntity, BeastPlayerModel<AlphaWerewolfEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/alpha_werewolf.png");

    public AlphaWerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastPlayerModel<>(BeastPlayerModel.createBodyLayer().bakeRoot()), 0.9F);
    }

    @Override
    protected void scale(AlphaWerewolfEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.06F, 1.06F, 1.06F);
    }

    @Override
    public ResourceLocation getTextureLocation(AlphaWerewolfEntity entity) {
        return TEXTURE;
    }
}
