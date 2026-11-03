package com.howlingwerewolf.mixin;
import com.howlingwerewolf.event.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(EnchantedCountIncreaseFunction.class)
abstract class LootingMixin {
    @WrapOperation(method="run",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int howling$looting(Holder<Enchantment> enchantment,LivingEntity entity,Operation<Integer> original,@Local(argsOnly=true) LootContext loot) {
        int level=original.call(enchantment,entity);
        var source=loot.getOptionalParameter(LootContextParams.DAMAGE_SOURCE);
        if (source==null) return level;
        var context=new GameplayContext.Looting(enchantment,source,level);
        WerewolfGameplayEvents.provideClawLooting(context);
        return context.getEnchantmentLevel();
    }
}
