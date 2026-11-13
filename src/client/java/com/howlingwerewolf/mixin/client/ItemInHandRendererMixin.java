/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin.client;

import com.howlingwerewolf.client.ArmRenderContext;
import com.howlingwerewolf.client.ClientEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    // Vanilla has already posed the arm here. Drawing at the call site also supports mods
    // such as MCA that replace PlayerRenderer's hands at their entry points.
    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"))
    private void howling$rightHand(PlayerRenderer renderer, PoseStack pose, MultiBufferSource buffers, int light,
                                    AbstractClientPlayer player, Operation<Void> original) {
        if (!howling$renderArm(player, HumanoidArm.RIGHT, pose, buffers, light)) {
            original.call(renderer, pose, buffers, light, player);
        }
    }

    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"))
    private void howling$leftHand(PlayerRenderer renderer, PoseStack pose, MultiBufferSource buffers, int light,
                                   AbstractClientPlayer player, Operation<Void> original) {
        if (!howling$renderArm(player, HumanoidArm.LEFT, pose, buffers, light)) {
            original.call(renderer, pose, buffers, light, player);
        }
    }

    @Unique
    private static boolean howling$renderArm(AbstractClientPlayer player, HumanoidArm arm, PoseStack pose,
                                             MultiBufferSource buffers, int light) {
        var context = new ArmRenderContext(player, arm, pose, buffers, light);
        ClientEvents.renderWerewolfArm(context);
        return context.isCanceled();
    }
}
