/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ArmRenderContext;
import com.howlingwerewolf.client.WerewolfRenderHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
abstract class ItemInHandRendererMixin {
    @Inject(method = "renderPlayerHand", at = @At("HEAD"), cancellable = true)
    private void howling$renderArm(PoseStack pose, SubmitNodeCollector collector, int light,
                                  HumanoidArm arm, PlayerRenderState player, CallbackInfo ci) {
        if (player.avatarRenderState == null) return;
        var context = new ArmRenderContext(arm, pose, collector, light);
        WerewolfRenderHooks.renderArm(context, player.avatarRenderState);
        if (context.isCanceled()) ci.cancel();
    }
}
