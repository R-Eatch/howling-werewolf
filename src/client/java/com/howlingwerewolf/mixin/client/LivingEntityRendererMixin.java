package com.howlingwerewolf.mixin.client;
import com.howlingwerewolf.client.RenderStateHooks;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
    private void howling$extract(LivingEntity entity,LivingEntityRenderState state,float delta,CallbackInfo ci) {
        RenderStateHooks.extract(entity,state,delta);
    }
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",at=@At("HEAD"),cancellable=true)
    private void howling$submit(LivingEntityRenderState state,com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector,net.minecraft.client.renderer.state.CameraRenderState camera,CallbackInfo ci) {
        if (!((Object)this instanceof com.howlingwerewolf.client.WerewolfLivingRenderer)
                && state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState avatar
                && com.howlingwerewolf.client.ClientEvents.renderWerewolfPlayer(avatar,pose,collector,camera)) ci.cancel();
    }
}
