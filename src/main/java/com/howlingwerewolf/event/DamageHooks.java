package com.howlingwerewolf.event;

import java.util.ArrayDeque;
import java.util.function.Function;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Carries pre-mitigation damage through nested native damage calls. */
public final class DamageHooks {
    private static final ThreadLocal<ArrayDeque<Float>> ORIGINAL = ThreadLocal.withInitial(ArrayDeque::new);

    public static boolean incoming(LivingEntity entity,DamageSource source,float amount,Function<Float,Boolean> original) {
        if (entity.level().isClientSide() || entity.isInvulnerableTo(source)) return original.apply(amount);
        var stack=ORIGINAL.get();
        stack.push(amount);
        try {
            var context=new GameplayContext.Damage(entity,source,amount);
            WerewolfGameplayEvents.beastVoidMeleeDamage(context);
            if (context.isCanceled()) return false;
            WerewolfGameplayEvents.formMeleeDamage(context);
            WerewolfGameplayEvents.livingHurt(context);
            return !context.isCanceled() && original.apply(context.getAmount());
        } finally {
            stack.pop();
            if (stack.isEmpty()) ORIGINAL.remove();
        }
    }

    public static void healthDamage(LivingEntity entity,DamageSource source,float amount,Runnable original) {
        float before=entity.getHealth();
        original.run();
        float loss=Math.max(0,before-entity.getHealth());
        if (loss>0) {
            var stack=ORIGINAL.get();
            var context=new GameplayContext.Damage(entity,source,loss,stack.isEmpty() ? amount : stack.peek());
            WerewolfGameplayEvents.lifesteal(context);
            WerewolfGameplayEvents.longClawSplash(context);
        }
    }

    public static void beforeHealthChange(LivingEntity entity,DamageSource source,float newHealth) {
        WerewolfGameplayEvents.fireClawAttack(new GameplayContext.Damage(entity,source,Math.max(0,entity.getHealth()-newHealth)));
    }
    private DamageHooks() { }
}
