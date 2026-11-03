package com.howlingwerewolf.mixin.client;
import com.howlingwerewolf.client.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin {
    @Inject(method="renderRightHand",at=@At("HEAD"),cancellable=true)
    private void howling$rightHand(PoseStack pose,SubmitNodeCollector collector,int light,net.minecraft.resources.Identifier texture,boolean sleeve,CallbackInfo ci) {
        howling$arm(pose,collector,light,net.minecraft.world.entity.HumanoidArm.RIGHT,ci);
    }
    @Inject(method="renderLeftHand",at=@At("HEAD"),cancellable=true)
    private void howling$leftHand(PoseStack pose,SubmitNodeCollector collector,int light,net.minecraft.resources.Identifier texture,boolean sleeve,CallbackInfo ci) {
        howling$arm(pose,collector,light,net.minecraft.world.entity.HumanoidArm.LEFT,ci);
    }
    @org.spongepowered.asm.mixin.Unique
    private void howling$arm(PoseStack pose,SubmitNodeCollector collector,int light,net.minecraft.world.entity.HumanoidArm arm,CallbackInfo ci) {
        var player=net.minecraft.client.Minecraft.getInstance().player;
        if (player==null) return;
        var context=new ArmRenderContext(player,arm,pose,collector,light);
        ClientEvents.renderWerewolfArm(context);
        if (context.isCanceled()) ci.cancel();
    }
}
