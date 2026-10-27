package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/** Vanilla equipment layers, aligned to the original werewolf body without a second player body. */
public final class WerewolfEquipmentRenderer extends WerewolfLivingRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
    public WerewolfEquipmentRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.0F, AvatarRenderState::new);
        addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), WerewolfArmorModel::new),
                context.getEquipmentRenderer()));
        addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
    }
    @Override protected RenderType getRenderType(AvatarRenderState state, boolean body, boolean translucent, boolean glowing) {
        return null;
    }
    @Override protected void setupRotations(AvatarRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        WerewolfRenderTransforms.applyPlayerTravelPose(state, pose);
    }
    @Override public Identifier getTextureLocation(AvatarRenderState state) { return state.skin.body().texturePath(); }
    @Override protected boolean shouldShowName(AbstractClientPlayer player, double distance) { return false; }
    @Override protected void submitNameDisplay(AvatarRenderState state, PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {}
}
