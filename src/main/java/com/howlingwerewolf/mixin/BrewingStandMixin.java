package com.howlingwerewolf.mixin;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BrewingStandBlockEntity.class)
abstract class BrewingStandMixin {
    @Inject(method="canPlaceItem",at=@At("HEAD"),cancellable=true)
    private void howling$bottle(int slot,ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if (slot>=0 && slot<3 && (stack.is(ModItems.WOLFSBANE_POTION.get()) || stack.is(ModItems.WEREWOLF_POTION.get())))
            cir.setReturnValue(((BrewingStandBlockEntity)(Object)this).getItem(slot).isEmpty());
    }
}
