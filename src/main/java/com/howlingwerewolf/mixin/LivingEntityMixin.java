package com.howlingwerewolf.mixin;
import com.howlingwerewolf.event.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @WrapMethod(method="hurtServer")
    private boolean howling$damage(ServerLevel world, DamageSource source,float amount,Operation<Boolean> original) {
        return DamageHooks.incoming((LivingEntity)(Object)this,world,source,amount,value -> original.call(world,source,value));
    }
    @WrapMethod(method="actuallyHurt")
    private void howling$healthDamage(ServerLevel world,DamageSource source,float amount,Operation<Void> original) {
        DamageHooks.healthDamage((LivingEntity)(Object)this,source,amount,() -> original.call(world,source,amount));
    }
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="actuallyHurt",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void howling$beforeHealth(LivingEntity entity,float value,Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly=true) DamageSource source) {
        DamageHooks.beforeHealthChange(entity,source,value);
        original.call(entity,value);
    }
    @Inject(method="startUsingItem",at=@At("HEAD"),cancellable=true)
    private void howling$food(InteractionHand hand,CallbackInfo ci) {
        var entity=(LivingEntity)(Object)this;
        var context=new GameplayContext.Food(entity,entity.getItemInHand(hand));
        WerewolfGameplayEvents.restrictFood(context);
        if (context.isCanceled()) ci.cancel();
    }
    @Inject(method="jumpFromGround",at=@At("TAIL"))
    private void howling$jump(CallbackInfo ci) { WerewolfGameplayEvents.jumpBoost(new GameplayContext.Jump((LivingEntity)(Object)this)); }
    @Inject(method="causeFallDamage",at=@At("HEAD"),cancellable=true)
    private void howling$fall(double distance,float multiplier,DamageSource source,CallbackInfoReturnable<Boolean> cir) {
        var context=new GameplayContext.Fall((LivingEntity)(Object)this,distance,multiplier);
        WerewolfGameplayEvents.preventMaxRankFallImpact(context);
        if (context.isCanceled()) cir.setReturnValue(false);
    }
    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method="getDimensions",at=@At("RETURN"))
    private net.minecraft.world.entity.EntityDimensions howling$dimensions(net.minecraft.world.entity.EntityDimensions original,net.minecraft.world.entity.Pose pose) {
        if (!((Object)this instanceof net.minecraft.world.entity.player.Player player)) return original;
        var context=new GameplayContext.Size(player,pose,original);
        WerewolfGameplayEvents.formSize(context);
        var result=context.getNewSize();
        var data=com.howlingwerewolf.capability.WerewolfApi.get(player).orElseThrow();
        if (data.isTransformed() && data.getModelSet()==com.howlingwerewolf.WerewolfModelSet.MODERN) {
            result=result.withEyeHeight(com.howlingwerewolf.WerewolfModelPose.eyeHeight(data.getForm(),pose, com.howlingwerewolf.WerewolfModelPose.isBeastSprint(player,data)));
        }
        return result;
    }
}
