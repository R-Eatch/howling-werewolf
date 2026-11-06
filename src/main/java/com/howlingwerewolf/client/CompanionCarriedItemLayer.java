/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Shows the stack a companion is carrying between containers. */
final class CompanionCarriedItemLayer extends RenderLayer<WerewolfCompanionEntity, EntityModel<WerewolfCompanionEntity>> {
    CompanionCarriedItemLayer(RenderLayerParent<WerewolfCompanionEntity, EntityModel<WerewolfCompanionEntity>> parent) { super(parent); }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, WerewolfCompanionEntity entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float headYaw, float headPitch) {
        ItemStack stack = entity.getMainHandItem();
        if (stack.isEmpty() || !(getParentModel() instanceof ArmedModel hand)) return;
        pose.pushPose();
        hand.translateToHand(HumanoidArm.RIGHT, pose);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate(1 / 16F, 2 / 16F, -10 / 16F);
        Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
                pose, buffers, entity.level(), light, OverlayTexture.NO_OVERLAY, entity.getId());
        pose.popPose();
    }
}
