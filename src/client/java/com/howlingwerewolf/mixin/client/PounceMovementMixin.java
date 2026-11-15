package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ClientPouncePose;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
abstract class PounceMovementMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void howling$keepPounceTrajectory(boolean sneaking, float multiplier, CallbackInfo event) {
        if (!ClientPouncePose.flyingLocally()) return;
        forwardImpulse = 0;
        leftImpulse = 0;
        jumping = false;
        shiftKeyDown = false;
    }
}
