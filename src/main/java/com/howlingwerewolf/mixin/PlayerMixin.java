package com.howlingwerewolf.mixin;
import com.howlingwerewolf.event.*;
import com.howlingwerewolf.capability.*;
import com.howlingwerewolf.WerewolfModelPose;
import com.howlingwerewolf.WerewolfModelSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.nbt.CompoundTag;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method="tick",at=@At("HEAD"))
    private void howling$beforeTick(CallbackInfo ci) {
        WerewolfGameplayEvents.playerTickPre(new GameplayContext.Tick((Player)(Object)this));
    }
    @Inject(method="tick",at=@At("TAIL"))
    private void howling$afterTick(CallbackInfo ci) {
        var context=new GameplayContext.Tick((Player)(Object)this);
        WerewolfGameplayEvents.playerTickPost(context);
        com.howlingwerewolf.world.VillageHunterSpawner.playerTick(context);
    }
    @ModifyReturnValue(method="getDestroySpeed",at=@At("RETURN"))
    private float howling$clawSpeed(float speed,BlockState state) {
        var context=new GameplayContext.BreakSpeed((Player)(Object)this,state,speed);
        WerewolfGameplayEvents.toolClawBreakSpeed(context);
        return context.getNewSpeed();
    }
    @ModifyReturnValue(method="hasCorrectToolForDrops",at=@At("RETURN"))
    private boolean howling$harvest(boolean result,BlockState state) {
        var context=new GameplayContext.Harvest((Player)(Object)this,state,result);
        WerewolfGameplayEvents.toolClawHarvestCheck(context);
        return context.canHarvest();
    }
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="actuallyHurt")
    private void howling$healthDamage(net.minecraft.server.level.ServerLevel world,net.minecraft.world.damagesource.DamageSource source,float amount,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        DamageHooks.healthDamage((Player)(Object)this,source,amount,() -> original.call(world,source,amount));
    }
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="actuallyHurt",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;setHealth(F)V"))
    private void howling$beforeHealth(Player player,float value,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly=true) net.minecraft.world.damagesource.DamageSource source) {
        DamageHooks.beforeHealthChange(player,source,value);
        original.call(player,value);
    }
    @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
    private void howling$legacy(ValueInput input,CallbackInfo ci) {
        // Older Forge/NeoForge saves used this mirror. Fabric attachments remain authoritative.
        input.read("ForgeData",CompoundTag.CODEC).ifPresent(root -> howling$restoreRoot(root));
        input.read("NeoForgeData",CompoundTag.CODEC).ifPresent(root -> howling$restoreRoot(root));
    }
    private void howling$restoreRoot(CompoundTag root) {
        var player=(Player)(Object)this;
        var data=WerewolfApi.get(player).orElseThrow();
        if (data.isDefaultState() && root.contains("PlayerPersisted")) {
            player.setAttached(ModAttachments.LEGACY,root.copy());
            WerewolfPersistence.recoverIfFresh(player,data);
        }
    }
}
