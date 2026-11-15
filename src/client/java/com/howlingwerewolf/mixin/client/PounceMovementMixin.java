package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ClientPouncePose;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
abstract class PounceMovementMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void howling$keepPounceTrajectory(CallbackInfo event) {
        if (ClientPouncePose.flyingLocally()) {
            keyPresses = Input.EMPTY;
            moveVector = Vec2.ZERO;
        }
    }
}
