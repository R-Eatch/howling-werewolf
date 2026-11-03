package com.howlingwerewolf.mixin;
import com.howlingwerewolf.event.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {
    @org.spongepowered.asm.mixin.Shadow private java.util.UUID target;
    @Inject(method="playerTouch",at=@At("HEAD"),cancellable=true)
    private void howling$pickup(Player player,CallbackInfo ci) {
        var item=(ItemEntity)(Object)this;
        if (item.hasPickUpDelay() || (target!=null && !target.equals(player.getUUID()))) return;
        var context=new GameplayContext.Pickup(player,item);
        WerewolfGameplayEvents.preventReservedSlotPickupWhenInventoryIsFull(context);
        if (!context.allowed()) ci.cancel();
    }
}
