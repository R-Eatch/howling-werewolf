/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

/** Shared, deterministic rules. Time is world game time, never the adjustable daylight clock. */
public final class CompanionRules {
    public static final int INVENTORY_SIZE = 9;
    public static final int MAX_COMPANIONS = 3;
    public static final int MAX_TIER = 3;
    public static final int MAX_ADDRESS_LENGTH = 16;
    public static final int MERCHANT_LIFETIME = 20 * 60 * 10;
    public static final int MIN_HUNT_TICKS = 36_000;
    public static final int MAX_HUNT_TICKS = 72_000;
    public static final int HUNT_COOLDOWN_TICKS = 48_000;
    private static final int[] REWARD_WEIGHTS = {4, 5, 6, 8};

    public static int tier(int value) { return clamp(value, 0, MAX_TIER); }
    public static int capacity(int skillRank, boolean alphaDefeated) {
        return Math.min(tier(skillRank), alphaDefeated ? MAX_COMPANIONS : 1);
    }
    public static int upgradeCost(int currentTier) {
        return currentTier >= 0 && currentTier < MAX_TIER ? 2 << currentTier : 0;
    }
    public static double maxHealth(int tier) { return 30 + 10 * tier(tier); }
    public static double attackDamage(int tier) { return 6 + 2 * tier(tier); }
    public static float damageMultiplier(int tier) { return 1.0F - 0.10F * tier(tier); }
    public static float regeneration(int tier) { return 1.0F + 0.5F * tier(tier); }
    public static long huntElapsed(long now, long start, long end) {
        return clamp(now - start, 0L, clamp(end - start, 0L, (long) MAX_HUNT_TICKS));
    }
    public static int huntRolls(long elapsed, int tier) {
        // 0/1/2/3 tiers: 6–12 / 7–15 / 9–18 / 12–24 rolls for a complete hunt.
        // There is deliberately no minimum reward for starting and immediately recalling a hunt.
        return (int) (clamp(elapsed, 0L, (long) MAX_HUNT_TICKS) * REWARD_WEIGHTS[tier(tier)] / 24_000L);
    }
    public static long huntCooldownEnd(long now, long plannedEnd) {
        return Math.min(now, plannedEnd) + HUNT_COOLDOWN_TICKS;
    }
    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static long clamp(long value, long min, long max) { return Math.max(min, Math.min(max, value)); }
    public static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private CompanionRules() {}
}
