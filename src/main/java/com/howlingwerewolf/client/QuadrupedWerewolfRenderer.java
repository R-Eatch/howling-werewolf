package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

public final class QuadrupedWerewolfRenderer extends WerewolfLivingRenderer<AbstractClientPlayer, AvatarRenderState, QuadrupedWerewolfModel> {
    public QuadrupedWerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new QuadrupedWerewolfModel(context.bakeLayer(ClientModEvents.QUADRUPED_WEREWOLF_LAYER)), 0.5F, AvatarRenderState::new);
        addLayer(new QuadrupedHeldItemLayer(this));
    }

    @Override protected void setupRotations(AvatarRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        WerewolfRenderTransforms.applyQuadrupedTravelPose(state, pose);
    }

    @Override public Identifier getTextureLocation(AvatarRenderState state) {
        return WerewolfSkinTextures.get(WerewolfRenderData.get(state).skinId(), WerewolfForm.QUADRUPED);
    }

    @Override protected boolean shouldShowName(AbstractClientPlayer player, double distance) {
        return !WerewolfSkinRenderContext.isPreview(player) && super.shouldShowName(player, distance);
    }
}
