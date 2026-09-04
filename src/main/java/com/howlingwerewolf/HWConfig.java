package com.howlingwerewolf;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HWConfig {
    private static final int HUNTER_BASE_WEIGHT = 50;
    private static final int FERAL_WEREWOLF_BASE_WEIGHT = 200;
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue WOLF_INFECTION_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FERAL_WEREWOLF_INFECTION_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WEREWOLF_EXPERIENCE_GAIN_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue MAX_WEREWOLF_LEVEL;
    public static final ForgeConfigSpec.BooleanValue GENERATE_SILVER;
    public static final ForgeConfigSpec.BooleanValue GENERATE_WOLFSBANE;
    public static final ForgeConfigSpec.DoubleValue HUNTER_SPAWN_WEIGHT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FERAL_WEREWOLF_SPAWN_WEIGHT_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue WOLFSBANE_GENERATION_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue BEAST_VOID_DAMAGE;
    public static final ForgeConfigSpec.IntValue ALPHA_TRIAL_DAMAGE_FREQUENCY_LIMIT_TICKS;
    public static final ForgeConfigSpec.BooleanValue PRESERVE_ALPHA_BADGE_ON_REVIVAL;
    public static final ForgeConfigSpec.BooleanValue SHOW_WEREWOLF_EQUIPMENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("gameplay");
        WOLF_INFECTION_CHANCE = builder.comment(
                        "Chance that damage from an untamed vanilla wolf infects a human player. Default is 10%.")
                .defineInRange("wolfInfectionChance", 0.10D, 0.0D, 1.0D);
        FERAL_WEREWOLF_INFECTION_CHANCE = builder.comment(
                        "Chance that damage from a wild Feral Werewolf infects a human player. Alpha-summoned werewolves do not use this chance. Default is 20%.")
                .defineInRange("feralWerewolfInfectionChance", 0.20D, 0.0D, 1.0D);
        WEREWOLF_EXPERIENCE_GAIN_MULTIPLIER = builder.comment(
                        "Final multiplier applied to normal werewolf experience gains. Does not affect administrator XP commands. Default is 1.0.")
                .defineInRange("werewolfExperienceGainMultiplier", 1.0D, 0.0D, 100.0D);
        MAX_WEREWOLF_LEVEL = builder.comment(
                        "Maximum werewolf progression level. Existing data above a lowered limit is clamped when loaded. Default is 20; maximum is 25.")
                .defineInRange("maxWerewolfLevel", 20, 1, 25);
        BEAST_VOID_DAMAGE = builder.comment(
                        "Use out-of-world damage for Beast Mode melee attacks. When false, Beast Mode uses the same damage type as normal werewolf attacks. Default is false.")
                .define("beastModeVoidDamage", false);
        ALPHA_TRIAL_DAMAGE_FREQUENCY_LIMIT_TICKS = builder.comment(
                        "Minimum interval in ticks between successful damage events received by the Alpha Trial boss. Set to 0 to disable the extra limit. Attackerless damage that bypasses invulnerability and player-caused Beast Mode void damage are unaffected. Default is 10 ticks.")
                .defineInRange("alphaTrialDamageFrequencyLimitTicks", 10, 0, Integer.MAX_VALUE);
        PRESERVE_ALPHA_BADGE_ON_REVIVAL = builder.comment(
                        "Keep the Alpha Werewolf Badge after it prevents fatal damage. When false, one badge is consumed and revival matches a Totem of Undying. Default is false.")
                .define("preserveAlphaBadgeOnRevival", false);
        SHOW_WEREWOLF_EQUIPMENT = builder.comment(
                        "Render worn armor and elytra on the normal transformed werewolf model. Held items are always rendered. Default is true.")
                .define("showWerewolfEquipment", true);
        builder.pop();
        builder.push("worldGeneration");
        GENERATE_SILVER = builder.comment(
                        "Generate silver ore in newly generated chunks. Existing chunks are not changed. Default is true.")
                .define("generateSilverOre", true);
        GENERATE_WOLFSBANE = builder.comment(
                        "Generate wolfsbane flowers in newly generated chunks. Existing chunks are not changed. Default is true.")
                .define("generateWolfsbane", true);
        // ConfigSpec removes obsolete keys and their comments when loading the config.
        HUNTER_SPAWN_WEIGHT_MULTIPLIER = builder.comment(
                        "Multiplier for the current default Hunter spawn weight (" + HUNTER_BASE_WEIGHT + "). 1.0 follows the mod default; 0 disables natural spawns. This is a relative weight, not a spawn probability. Restart the world after changing it.")
                .worldRestart().defineInRange("hunterSpawnWeightMultiplier", 1.0D, 0.0D, 10.0D);
        FERAL_WEREWOLF_SPAWN_WEIGHT_MULTIPLIER = builder.comment(
                        "Multiplier for the current default Feral Werewolf spawn weight (" + FERAL_WEREWOLF_BASE_WEIGHT + "). 1.0 follows the mod default; 0 disables natural spawns. Full-moon, night and darkness rules still apply. This is a relative weight, not a spawn probability. Restart the world after changing it.")
                .worldRestart().defineInRange("feralWerewolfSpawnWeightMultiplier", 1.0D, 0.0D, 10.0D);
        WOLFSBANE_GENERATION_WEIGHT = builder.comment(
                        "Wolfsbane generation weight relative to the 1.0.5 rate. 0 disables generation, 100 preserves the old rate (1 attempt per 8 taiga chunks or 22 forest chunks), and 200 doubles those attempt rates. Default is 100.")
                .defineInRange("wolfsbaneGenerationWeight", 100, 0, 200);
        builder.pop();
        SPEC = builder.build();
    }

    public static int hunterSpawnWeight() {
        return scaledSpawnWeight(HUNTER_BASE_WEIGHT, HUNTER_SPAWN_WEIGHT_MULTIPLIER.get());
    }

    public static int feralWerewolfSpawnWeight() {
        return scaledSpawnWeight(FERAL_WEREWOLF_BASE_WEIGHT, FERAL_WEREWOLF_SPAWN_WEIGHT_MULTIPLIER.get());
    }

    private static int scaledSpawnWeight(int baseWeight, double multiplier) {
        return multiplier == 0.0D ? 0 : Math.max(1, (int) Math.round(baseWeight * multiplier));
    }

    private HWConfig() {}
}
