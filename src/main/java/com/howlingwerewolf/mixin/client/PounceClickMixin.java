package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ClientPounceTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class PounceClickMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void howling$confirmPounce(CallbackInfoReturnable<Boolean> event) {
        if (ClientPounceTarget.confirm() || ClientPounceTarget.consumesAttack()) event.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void howling$consumePounceClick(boolean held, CallbackInfo event) {
        if (held && ClientPounceTarget.consumesAttack()) event.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void howling$cancelPounce(CallbackInfo event) {
        if (ClientPounceTarget.cancel()) event.cancel();
    }
}
