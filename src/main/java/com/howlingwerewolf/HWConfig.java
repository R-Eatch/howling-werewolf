package com.howlingwerewolf;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HWConfig {
    private static final int HUNTER_BASE_WEIGHT = 50;
    private static final int FERAL_WEREWOLF_BASE_WEIGHT = 200;
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue WOLF_INFECTION_CHANCE;
    public static final ModConfigSpec.DoubleValue FERAL_WEREWOLF_INFECTION_CHANCE;
    public static final ModConfigSpec.DoubleValue WEREWOLF_EXPERIENCE_GAIN_MULTIPLIER;
    public static final ModConfigSpec.IntValue MAX_WEREWOLF_LEVEL;
    public static final ModConfigSpec.BooleanValue GENERATE_SILVER;
    public static final ModConfigSpec.BooleanValue GENERATE_WOLFSBANE;
    public static final ModConfigSpec.DoubleValue HUNTER_SPAWN_WEIGHT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FERAL_WEREWOLF_SPAWN_WEIGHT_MULTIPLIER;
    public static final ModConfigSpec.IntValue WOLFSBANE_GENERATION_WEIGHT;
    public static final ModConfigSpec.BooleanValue BEAST_VOID_DAMAGE;
    public static final ModConfigSpec.IntValue ALPHA_TRIAL_DAMAGE_FREQUENCY_LIMIT_TICKS;
    public static final ModConfigSpec.BooleanValue PRESERVE_ALPHA_BADGE_ON_REVIVAL;
    public static final ModConfigSpec.BooleanValue SHOW_WEREWOLF_EQUIPMENT;
    public static final ModConfigSpec.BooleanValue ENABLE_WEREWOLF_AUTO_STEP;
    public static final ModConfigSpec.BooleanValue ENABLE_COMPANIONS;
    public static final ModConfigSpec.BooleanValue ENABLE_COMPANION_SORTING;
    public static final ModConfigSpec.IntValue COMPANION_SORTING_REACH;
    public static final ModConfigSpec.ConfigValue<String> NPC_MODEL_SET;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("gameplay");
        ENABLE_COMPANIONS = builder.comment(
                        "Enable werewolf merchants, recruitment and companion commands. Default is true.",
                        "The server controls this setting. Disabling parks existing companions without deleting belongings and pauses hunts/cooldowns until re-enabled.")
                .define("enableCompanions", true);
        ENABLE_COMPANION_SORTING = builder.comment(
                        "Allow companions to consolidate items in owner-registered vanilla chests and barrels. Default is true.",
                        "Server-controlled. Disabling stops sorting without deleting registered containers or moving belongings.")
                .define("enableCompanionSorting", true);
        COMPANION_SORTING_REACH = builder.comment(
                        "How far, in blocks, a sorting companion reaches into a container it cannot walk up to. Default is 32.",
                        "It still walks over whenever there is a way; 3 means it skips anything it cannot reach on foot.")
                .defineInRange("companionSortingReach", 32, 3, 64);
        NPC_MODEL_SET = builder.comment(
                        "Model set for wild werewolves, spawn-egg werewolves, the Alpha and its summoned pack: modern or classic. Default is modern.",
                        "Modern uses Ashen for the Alpha/pack and a saved random coat for wild werewolves. Classic uses Adrian for all of them.",
                        "The server (including the integrated single-player server) controls this setting. Existing entities update after the config reloads; player skin choices are independent.")
                .define("npcModelSet", "modern", value -> value instanceof String name
                        && (name.equals("classic") || name.equals("modern")));
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
        ENABLE_WEREWOLF_AUTO_STEP = builder.comment(
                        "Automatically step up 1.0 block in Quadruped Wolf form, 1.5 in Werewolf form and 2.0 in Beast form. Default is true; false restores normal stepping.")
                .define("enableWerewolfAutoStep", true);
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

    public static WerewolfModelSet npcModelSet() { return WerewolfModelSet.byId(NPC_MODEL_SET.get()); }

    public static int feralWerewolfSpawnWeight() {
        return scaledSpawnWeight(FERAL_WEREWOLF_BASE_WEIGHT, FERAL_WEREWOLF_SPAWN_WEIGHT_MULTIPLIER.get());
    }

    private static int scaledSpawnWeight(int baseWeight, double multiplier) {
        return multiplier == 0.0D ? 0 : Math.max(1, (int) Math.round(baseWeight * multiplier));
    }

    private HWConfig() {}
}
