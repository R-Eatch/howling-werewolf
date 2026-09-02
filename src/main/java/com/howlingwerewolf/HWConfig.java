package com.howlingwerewolf;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HWConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue WOLF_INFECTION_CHANCE;
    public static final ModConfigSpec.DoubleValue FERAL_WEREWOLF_INFECTION_CHANCE;
    public static final ModConfigSpec.DoubleValue WEREWOLF_EXPERIENCE_GAIN_MULTIPLIER;
    public static final ModConfigSpec.IntValue MAX_WEREWOLF_LEVEL;
    public static final ModConfigSpec.BooleanValue GENERATE_SILVER;
    public static final ModConfigSpec.BooleanValue GENERATE_WOLFSBANE;
    public static final ModConfigSpec.IntValue HUNTER_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue FERAL_WEREWOLF_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue WOLFSBANE_GENERATION_WEIGHT;
    public static final ModConfigSpec.BooleanValue BEAST_VOID_DAMAGE;
    public static final ModConfigSpec.IntValue ALPHA_TRIAL_DAMAGE_FREQUENCY_LIMIT_TICKS;
    public static final ModConfigSpec.BooleanValue PRESERVE_ALPHA_BADGE_ON_REVIVAL;
    public static final ModConfigSpec.BooleanValue SHOW_WEREWOLF_EQUIPMENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
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
        HUNTER_SPAWN_WEIGHT = builder.comment(
                        "Relative Hunter spawn weight in forest creature spawn lists. 0 disables natural Hunter spawns; common passive mobs usually use weights from 8 to 12. Default is 20.")
                .defineInRange("hunterSpawnWeight", 20, 0, 200);
        FERAL_WEREWOLF_SPAWN_WEIGHT = builder.comment(
                        "Relative Feral Werewolf spawn weight in forest monster spawn lists. 0 disables natural Feral Werewolf spawns; common monsters usually use weights around 95 to 100. Full-moon, night and darkness rules still apply. Default is 70.")
                .defineInRange("feralWerewolfSpawnWeight", 70, 0, 200);
        WOLFSBANE_GENERATION_WEIGHT = builder.comment(
                        "Wolfsbane generation weight relative to the 1.0.5 rate. 0 disables generation, 100 preserves the old rate (1 attempt per 8 taiga chunks or 22 forest chunks), and 200 doubles those attempt rates. Default is 100.")
                .defineInRange("wolfsbaneGenerationWeight", 100, 0, 200);
        builder.pop();
        SPEC = builder.build();
    }

    private HWConfig() {}
}
