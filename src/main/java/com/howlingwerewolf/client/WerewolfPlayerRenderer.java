package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

public final class WerewolfPlayerRenderer extends WerewolfLivingRenderer<AbstractClientPlayer, AvatarRenderState, WerewolfPlayerModel<AvatarRenderState>> {
    public WerewolfPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfPlayerModel<>(WerewolfPlayerModel.createBodyLayer().bakeRoot()), 0.55F, AvatarRenderState::new);
        addLayer(new ItemInHandLayer<>(this));
    }

    @Override protected void setupRotations(AvatarRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        WerewolfRenderTransforms.applyPlayerTravelPose(state, pose);
    }

    @Override public Identifier getTextureLocation(AvatarRenderState state) {
        return WerewolfSkinTextures.get(WerewolfRenderData.get(state).skinId(), WerewolfForm.WEREWOLF);
    }

    @Override protected boolean shouldShowName(AbstractClientPlayer player, double distance) {
        return !WerewolfSkinRenderContext.isPreview(player) && super.shouldShowName(player, distance);
    }
}
