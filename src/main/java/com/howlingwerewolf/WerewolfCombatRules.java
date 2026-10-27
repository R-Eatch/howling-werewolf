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
        // A recursive Beast void hit already contains the form multiplier and Moonrend.
        // Ordinary hits start with unmodified source damage; Moonrend never enters their splash.
        float clawDamage = convertedVoidHit
                ? originalDamage - moonRendDamage
                : originalDamage * attackMultiplier(form);
        return Math.max(0.0F, clawDamage) * 0.5F;
    }

    private WerewolfCombatRules() {}
}
