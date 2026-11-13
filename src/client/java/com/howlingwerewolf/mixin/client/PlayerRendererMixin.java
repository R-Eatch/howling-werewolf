/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void howling$renderPlayer(AbstractClientPlayer player, float yaw, float partial, PoseStack pose,
                                       MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (ClientEvents.renderWerewolfPlayer((PlayerRenderer) (Object) this, player, yaw, partial, pose, buffers, light)) {
            ci.cancel();
        }
    }
}
