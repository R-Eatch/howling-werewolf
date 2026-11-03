package com.howlingwerewolf.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/** Alignment must be applied by the armor model itself during deferred rendering. */
final class WerewolfArmorModel extends HumanoidModel<AvatarRenderState> {
    WerewolfArmorModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
        body.y = head.y = 1.0F;
        rightArm.y = leftArm.y = 3.0F;
        if (state.isCrouching) {
            body.xRot = 0.5F;
            body.y = 4.2F;
            head.y = 5.2F;
            rightArm.y = leftArm.y = 6.2F;
            rightLeg.z = leftLeg.z = 4.0F;
            rightLeg.y = leftLeg.y = 12.2F;
        }
        if (state.isFallFlying) {
            body.y = head.y = 1.0F;
            rightArm.y = leftArm.y = 3.0F;
            body.xRot = 0.08F;
        }
        // In 26.3 the hat is a child of the head and already inherits its translation.
    }
}
