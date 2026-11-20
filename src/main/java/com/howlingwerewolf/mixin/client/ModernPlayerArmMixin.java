/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ModernPlayerOverlayLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
abstract class ModernPlayerArmMixin {
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void howlingwerewolf$drawClaws(PoseStack pose, SubmitNodeCollector collector, int light, Identifier texture,
                                         ModelPart arm, boolean sleeve, CallbackInfo callback) {
        var model = ((AvatarRenderer<?>) (Object) this).getModel();
        HumanoidArm side = arm == model.leftArm ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        ModernPlayerOverlayLayer.renderArm(pose, collector, light, Minecraft.getInstance().player, arm, side);
    }
}
