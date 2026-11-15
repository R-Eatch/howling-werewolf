/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.WerewolfRenderHooks;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
abstract class PlayerRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void howling$extract(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer player) WerewolfRenderHooks.extract(player, state, partialTick);
    }
}
