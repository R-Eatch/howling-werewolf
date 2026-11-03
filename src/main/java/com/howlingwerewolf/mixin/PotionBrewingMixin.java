package com.howlingwerewolf.mixin;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PotionBrewing.class)
abstract class PotionBrewingMixin {
    @Unique private static ItemStack howling$result(ItemStack input,ItemStack ingredient) {
        var potion=input.get(DataComponents.POTION_CONTENTS);
        if (input.is(Items.POTION) && potion!=null && potion.is(Potions.AWKWARD) && ingredient.is(ModBlocks.WOLFSBANE.get().asItem())) return new ItemStack(ModItems.WOLFSBANE_POTION.get());
        if (input.is(ModItems.WOLFSBANE_POTION.get()) && ingredient.is(Items.FERMENTED_SPIDER_EYE)) return new ItemStack(ModItems.WEREWOLF_POTION.get());
        return ItemStack.EMPTY;
    }
    @Inject(method="isIngredient",at=@At("HEAD"),cancellable=true)
    private void howling$ingredient(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModBlocks.WOLFSBANE.get().asItem())) cir.setReturnValue(true);
    }
    @Inject(method="hasMix",at=@At("HEAD"),cancellable=true)
    private void howling$hasMix(ItemStack input,ItemStack ingredient,CallbackInfoReturnable<Boolean> cir) {
        if (!howling$result(input,ingredient).isEmpty()) cir.setReturnValue(true);
    }
    @Inject(method="mix",at=@At("HEAD"),cancellable=true)
    private void howling$mix(ItemStack ingredient,ItemStack input,CallbackInfoReturnable<ItemStack> cir) {
        var result=howling$result(input,ingredient);
        if (!result.isEmpty()) cir.setReturnValue(result);
    }
}
