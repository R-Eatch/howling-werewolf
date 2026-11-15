/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.WerewolfRenderHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
abstract class AvatarSubmitMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void howling$submitAvatar(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
                                      CameraRenderState camera, CallbackInfo ci) {
        if ((Object) this instanceof AvatarRenderer<?> && state instanceof AvatarRenderState avatar
                && WerewolfRenderHooks.submit(avatar, pose, collector, camera)) ci.cancel();
    }
}
