/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin;

import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    // Vanilla skips empty equipped stacks before asking for their enchantments. Both loot-count
    // and rare-drop queries reach this entity-level method with the actual loot attacker.
    @Inject(method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("RETURN"), cancellable = true)
    private static void howlingwerewolf$clawLooting(Holder<Enchantment> enchantment, LivingEntity entity,
                                                   CallbackInfoReturnable<Integer> result) {
        if (!enchantment.is(Enchantments.LOOTING) || !(entity instanceof ServerPlayer player)
                || !player.getMainHandItem().isEmpty()) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed()) return;
            int rank = data.getTreeSkillRank(WerewolfTreeSkill.CLAW_EFFICIENCY);
            if (rank > result.getReturnValueI()) result.setReturnValue(rank);
        });
    }
}
