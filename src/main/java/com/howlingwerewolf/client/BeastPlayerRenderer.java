package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

public final class BeastPlayerRenderer extends WerewolfLivingRenderer<AbstractClientPlayer, AvatarRenderState, BeastPlayerModel<AvatarRenderState>> {
    public BeastPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastPlayerModel<>(BeastPlayerModel.createBodyLayer().bakeRoot()), 0.85F, AvatarRenderState::new);
        addLayer(new ItemInHandLayer<>(this));
    }

    @Override protected void setupRotations(AvatarRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        WerewolfRenderTransforms.applyPlayerTravelPose(state, pose);
    }

    @Override public Identifier getTextureLocation(AvatarRenderState state) {
        return WerewolfSkinTextures.get(WerewolfRenderData.get(state).skinId(), WerewolfForm.BEAST);
    }

    @Override protected boolean shouldShowName(AbstractClientPlayer player, double distance) {
        return !WerewolfSkinRenderContext.isPreview(player) && super.shouldShowName(player, distance);
    }
}
