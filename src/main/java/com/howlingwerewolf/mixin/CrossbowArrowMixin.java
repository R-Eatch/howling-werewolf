package com.howlingwerewolf.mixin;

import com.howlingwerewolf.content.HunterCrossbowItem;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
abstract class CrossbowArrowMixin {
    @Inject(method = "getArrow", at = @At("RETURN"))
    private static void howling$markSilverOathArrow(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo,
                                                   CallbackInfoReturnable<AbstractArrow> callback) {
        if (!level.isClientSide && weapon.is(ModItems.HUNTER_CROSSBOW.get())) {
            callback.getReturnValue().getPersistentData().putBoolean(HunterCrossbowItem.ARROW_MARKER, true);
        }
    }
}
