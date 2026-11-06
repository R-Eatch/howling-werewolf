/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.mixin;

import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Monsters leave a passively following companion alone; replacing with null also clears an old target. */
@Mixin(Mob.class)
abstract class MobMixin {
    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity howling$companionTarget(LivingEntity target) {
        return WerewolfCompanionEntity.ignoresFollowingCompanion((Mob) (Object) this, target) ? null : target;
    }
}
