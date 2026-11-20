package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ModernPlayerOverlayLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
abstract class PlayerOverlayLayerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void howling$addOverlay(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        ((AvatarRenderer<?>) (Object) this).addLayer(new ModernPlayerOverlayLayer((AvatarRenderer<?>) (Object) this));
    }
}
