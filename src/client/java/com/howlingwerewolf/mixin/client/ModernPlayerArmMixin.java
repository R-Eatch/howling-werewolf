/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ModernPlayerOverlayLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
abstract class ModernPlayerArmMixin {
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void howlingwerewolf$drawClaws(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                         ModelPart arm, ModelPart sleeve, CallbackInfo callback) {
        var model = ((PlayerRenderer) (Object) this).getModel();
        HumanoidArm side = arm == model.leftArm ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        ModernPlayerOverlayLayer.renderArm(pose, buffers, light, player, arm, side);
    }
}
