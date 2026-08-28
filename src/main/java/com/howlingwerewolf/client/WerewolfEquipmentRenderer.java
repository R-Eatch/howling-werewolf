package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders vanilla armor and elytra layers over the transformed werewolf body while keeping the
 * vanilla player body itself invisible. The hidden parent model is manually aligned to the custom
 * werewolf torso/legs so armor and elytra follow the custom model more closely, especially in
 * crouch and fall-flying poses.
 */
public final class WerewolfEquipmentRenderer
        extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public WerewolfEquipmentRenderer(EntityRendererProvider.Context context) {
        super(context, new HiddenPlayerModel(context), 0.0F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
        WerewolfRenderTransforms.applyPlayerTravelPose(player, poseStack, partialTick);
    }

    public void prepare(AbstractClientPlayer player, PlayerRenderer vanillaRenderer, float partialTick) {
        PlayerModel<AbstractClientPlayer> vanilla = vanillaRenderer.getModel();
        vanilla.copyPropertiesTo(model);
        model.leftArmPose = vanilla.leftArmPose;
        model.rightArmPose = vanilla.rightArmPose;
        model.crouching = player.isCrouching();
        model.swimAmount = player.getSwimAmount(partialTick);
        model.attackTime = vanilla.attackTime;
        model.riding = vanilla.riding;
        model.young = vanilla.young;

        // Default vanilla player baseline.
        model.head.y = 0.0F;
        model.hat.y = 0.0F;
        model.body.y = 0.0F;
        model.rightArm.y = 2.0F;
        model.leftArm.y = 2.0F;
        model.rightLeg.y = 12.0F;
        model.leftLeg.y = 12.0F;
        model.rightLeg.z = 0.0F;
        model.leftLeg.z = 0.0F;

        // Werewolf custom model torso is one pixel lower and visually broader. Align the armor and
        // elytra carrier body to that lower torso anchor.
        model.body.y = 1.0F;
        model.rightArm.y = 3.0F;
        model.leftArm.y = 3.0F;
        model.head.y = 1.0F;
        model.hat.y = 1.0F;

        if (player.isCrouching()) {
            // Mirror vanilla Steve crouch, but keep the equipment anchored slightly lower to the
            // werewolf torso. This greatly reduces armor/elytra separation in sneak pose.
            model.body.xRot = 0.5F;
            model.body.y = 4.2F;
            model.head.y = 5.2F;
            model.hat.y = 5.2F;
            model.rightArm.y = 6.2F;
            model.leftArm.y = 6.2F;
            model.rightLeg.z = 4.0F;
            model.leftLeg.z = 4.0F;
            model.rightLeg.y = 12.2F;
            model.leftLeg.y = 12.2F;
        }

        if (player.getFallFlyingTicks() > 4) {
            // Elytra layer is driven from the hidden model's body transform. Pull that transform
            // into the same travel lane as the custom renderer.
            model.body.y = 1.0F;
            model.head.y = 1.0F;
            model.hat.y = 1.0F;
            model.rightArm.y = 3.0F;
            model.leftArm.y = 3.0F;
            model.body.xRot = 0.08F;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return player.getSkinTextureLocation();
    }

    @Override
    protected boolean shouldShowName(AbstractClientPlayer player) {
        return false;
    }

    private static final class HiddenPlayerModel extends PlayerModel<AbstractClientPlayer> {
        private HiddenPlayerModel(EntityRendererProvider.Context context) {
            super(context.bakeLayer(ModelLayers.PLAYER), false);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                   int packedOverlay, float red, float green, float blue, float alpha) {
            // The custom werewolf renderer draws the body. Only this renderer's layers are visible.
        }
    }
}
