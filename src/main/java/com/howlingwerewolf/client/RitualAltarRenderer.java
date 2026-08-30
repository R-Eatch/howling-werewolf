package com.howlingwerewolf.client;

import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.trial.RitualAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Renders offerings and the twelve-second badge/pearl convergence sequence. */
public final class RitualAltarRenderer implements BlockEntityRenderer<RitualAltarBlockEntity> {
    private final ItemRenderer itemRenderer;

    public RitualAltarRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public AABB getRenderBoundingBox(RitualAltarBlockEntity altar) {
        // The activation renderer draws the four pearls up to three blocks from the center.
        return altar.isCentral() && altar.isTrialActive()
                ? new AABB(altar.getBlockPos()).inflate(4.0D, 4.0D, 4.0D)
                : BlockEntityRenderer.super.getRenderBoundingBox(altar);
    }

    @Override
    public void render(RitualAltarBlockEntity altar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (altar.isCentral() && altar.getTrialPhase() == RitualAltarBlockEntity.PHASE_ACTIVATION) {
            renderActivation(altar, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (!altar.getOffering().isEmpty()) {
            float time = altar.getLevel() == null ? 0.0F
                    : altar.getLevel().getGameTime() + partialTick;
            renderStack(altar.getOffering(), poseStack, buffer, packedLight,
                    0.5D, 1.0D + Math.sin(time * 0.08D) * 0.08D, 0.5D,
                    time * 2.5F, 0.8F);
        }
    }

    private void renderActivation(RitualAltarBlockEntity altar, float partialTick, PoseStack poseStack,
                                  MultiBufferSource buffer, int packedLight) {
        float ticks = altar.getRitualTicks() + partialTick;
        float badgeRise = Math.min(1.0F, ticks / 100.0F);
        double badgeY = 1.0D + badgeRise * 1.35D;
        renderStack(new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get()), poseStack, buffer,
                packedLight, 0.5D, badgeY, 0.5D, ticks * 2.4F, 0.9F);

        if (ticks < 100.0F) return;
        float progress = Math.min(1.0F, (ticks - 100.0F) / 140.0F);
        double radius = 3.0D * (1.0D - progress) + 0.35D;
        float angularSpeed = 0.05F + progress * 0.25F;
        for (int i = 0; i < 4; i++) {
            double angle = ticks * angularSpeed + i * Math.PI * 0.5D;
            renderStack(new ItemStack(ModItems.MOONBANE_PEARL.get()), poseStack, buffer,
                    packedLight, 0.5D + Math.cos(angle) * radius,
                    1.4D + progress * 1.25D,
                    0.5D + Math.sin(angle) * radius,
                    (float)Math.toDegrees(-angle), 0.65F);
        }
    }

    private void renderStack(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, double x, double y, double z, float rotation,
                             float scale) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}
