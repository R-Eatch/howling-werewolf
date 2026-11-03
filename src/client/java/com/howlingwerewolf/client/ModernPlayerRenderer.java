package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Player-only renderers; Alpha and feral entities continue using their Classic renderers. */
final class ModernPlayerRenderer extends WerewolfLivingRenderer<AbstractClientPlayer, AvatarRenderState, ModernPlayerModel<AvatarRenderState>> {
    private final WerewolfForm form;

    ModernPlayerRenderer(EntityRendererProvider.Context context, WerewolfForm form) {
        super(context, new ModernPlayerModel<>(form), form == WerewolfForm.BEAST ? 0.85F : 0.5F, AvatarRenderState::new);
        this.form = form;
        if (form == WerewolfForm.QUADRUPED) {
            addLayer(new RenderLayer<>(this) {
                @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light,
                                             AvatarRenderState state, float yRot, float xRot) {
                    var item = WerewolfRenderData.get(state).mouthItem();
                    if (item.isEmpty()) return;
                    pose.pushPose();
                    getParentModel().translateToMouth(pose);
                    pose.mulPose(Axis.XP.rotationDegrees(90));
                    pose.scale(0.72F, 0.72F, 0.72F);
                    item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
                    pose.popPose();
                }
            });
        } else {
            addLayer(new ItemInHandLayer<>(this));
            if (form == WerewolfForm.WEREWOLF) {
                addLayer(new ModernEquipmentLayer(this, context));
                addLayer(new ModernWingsLayer(this, context));
            }
        }
    }

    @Override protected void setupRotations(AvatarRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        if (form == WerewolfForm.QUADRUPED) WerewolfRenderTransforms.applyQuadrupedTravelPose(state, pose);
        else WerewolfRenderTransforms.applyPlayerTravelPose(state, pose);
    }

    @Override public Identifier getTextureLocation(AvatarRenderState state) {
        return WerewolfSkinCatalog.texture(WerewolfRenderData.get(state).skinId(), WerewolfModelSet.MODERN, form);
    }

    @Override protected boolean shouldShowName(AbstractClientPlayer player, double distance) {
        return !WerewolfSkinRenderContext.isPreview(player) && super.shouldShowName(player, distance);
    }
}
