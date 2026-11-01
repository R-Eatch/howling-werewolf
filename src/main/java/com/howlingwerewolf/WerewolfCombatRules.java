package com.howlingwerewolf;

/** Damage arithmetic shared by primary attacks and Long Claws. Inputs precede victim mitigation. */
public final class WerewolfCombatRules {
    public static float attackMultiplier(WerewolfForm form) {
        return switch (form) {
            case BEAST -> 2.0F;
            case QUADRUPED -> 0.75F;
            default -> 1.0F;
        };
    }

    public static float longClawSplash(float originalDamage, WerewolfForm form,
                                      boolean convertedVoidHit, float moonRendDamage) {
        // Match NeoForge 1.21.1: ordinary splash keeps Moonrend and the extra form multiplier.
        // Converted Beast void hits already include both; remove Moonrend without scaling again.
        // 恢复旧版普通副伤害中的裂月加成及额外形态倍率；野兽虚空副伤仍扣除裂月、不再乘倍率。
        float clawDamage = convertedVoidHit
                ? originalDamage - moonRendDamage
                : (originalDamage * attackMultiplier(form) + moonRendDamage) * attackMultiplier(form);
        return Math.max(0.0F, clawDamage) * 0.5F;
    }

    private WerewolfCombatRules() {}
}
