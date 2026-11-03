package com.howlingwerewolf.mixin;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
abstract class BrewingPotionSlotMixin {
    @Inject(method="mayPlaceItem",at=@At("HEAD"),cancellable=true)
    private static void howling$bottle(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModItems.WOLFSBANE_POTION.get()) || stack.is(ModItems.WEREWOLF_POTION.get())) cir.setReturnValue(true);
    }
}
