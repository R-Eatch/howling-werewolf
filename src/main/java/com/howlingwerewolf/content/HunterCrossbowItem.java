package com.howlingwerewolf.content;

import com.howlingwerewolf.entity.SettlementCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

public final class HunterCrossbowItem extends CrossbowItem {
    private static final float WEREWOLF_DAMAGE_MULTIPLIER = 1.25F;
    private static final int WEAKNESS_TICKS = 40;
    private static final int WEAKNESS_COOLDOWN = 100;
    private static final Map<LivingEntity, Long> NEXT_WEAKNESS = new WeakHashMap<>();

    public HunterCrossbowItem(Properties properties) { super(properties.durability(465)); }

    public static final String ARROW_MARKER = "howlingwerewolf.silver_oath_arrow";

    public static boolean firedArrow(DamageSource source) {
        if (!source.is(DamageTypes.ARROW) || !(source.getDirectEntity() instanceof AbstractArrow arrow)) return false;
        return arrow.getPersistentData().getBoolean(ARROW_MARKER);
    }

    public static float damage(LivingEntity target, DamageSource source, float amount) {
        return firedArrow(source) && SettlementCombat.isWerewolf(target) ? amount * WEREWOLF_DAMAGE_MULTIPLIER : amount;
    }

    public static void onDamage(LivingEntity target, DamageSource source) {
        if (!target.isAlive() || !firedArrow(source) || !SettlementCombat.isWerewolf(target)) return;
        long now = target.level().getGameTime();
        if (now < NEXT_WEAKNESS.getOrDefault(target, 0L)) return;
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS), source.getEntity());
        NEXT_WEAKNESS.put(target, now + WEAKNESS_COOLDOWN);
    }

}
