package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

final class ModernPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, ModernPlayerModel<AbstractClientPlayer>> {
    private final WerewolfForm form;

    ModernPlayerRenderer(EntityRendererProvider.Context context, WerewolfForm form) {
        super(context, new ModernPlayerModel<>(form), form == WerewolfForm.BEAST ? 0.85F : 0.5F);
        this.form = form;
        if (form == WerewolfForm.QUADRUPED) {
            addLayer(new RenderLayer<>(this) {
                @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                             float limb, float speed, float partial, float age, float yaw, float pitch) {
                    if (player.getMainHandItem().isEmpty()) return;
                    pose.pushPose();
                    getParentModel().translateToMouth(pose);
                    pose.mulPose(Axis.XP.rotationDegrees(90));
                    pose.scale(0.72F, 0.72F, 0.72F);
                    context.getItemInHandRenderer().renderItem(player, player.getMainHandItem(), ItemDisplayContext.GROUND, false, pose, buffers, light);
                    pose.popPose();
                }
            });
        } else {
            addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
            if (form == WerewolfForm.WEREWOLF) {
                addLayer(new ModernEquipmentLayer(this, context));
                addLayer(new ModernWingsLayer(this, context));
            }
        }
    }

    @Override protected void setupRotations(AbstractClientPlayer player, PoseStack pose, float age, float yaw, float partial) {
        super.setupRotations(player, pose, age, yaw, partial);
        if (form == WerewolfForm.QUADRUPED) WerewolfRenderTransforms.applyQuadrupedTravelPose(player, pose, partial);
        else WerewolfRenderTransforms.applyPlayerTravelPose(player, pose, partial);
    }

    @Override public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return WerewolfSkinCatalog.texture(WerewolfSkinRenderContext.getSkinId(player), WerewolfModelSet.MODERN, form);
    }

    @Override protected boolean shouldShowName(AbstractClientPlayer player) {
        return !WerewolfSkinRenderContext.isPreview(player) && super.shouldShowName(player);
    }
}
