package com.howlingwerewolf.capability;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfTreeSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WerewolfData {
    public static final int CURRENT_DATA_VERSION = 2;
    public static final int DEFAULT_MAX_LEVEL = 20;
    public static final int MAX_LEVEL_LIMIT = 25;
    public static final int LEVEL_ONE_SKILL_POINTS = 2;
    public static final int LEVEL_ONE_TREE_POINTS = 5;

    private boolean werewolf;
    private boolean infected;
    private boolean moonForced;
    private WerewolfForm form = WerewolfForm.HUMAN;
    private boolean alphaDefeated;
    private boolean nightVisionEnabled = true;
    private int level = 1;
    private int experience;
    private double experienceGainRemainder;
    private int bonusSkillPoints;
    private int bonusTreePoints;
    private int clawHotbarSlot = 1;
    private long awakeningDayTime = -1L;
    private long wolfSpiritCooldownEnd;
    private long wolfSpiritExpireTime;
    private long bloodyBiteCooldownEnd;
    private long moonbloodSurgeCooldownEnd;
    private long moonbloodCrashTime;
    private int totemExperienceTicks;
    /** Server maximum carried only by S2C snapshots; zero means use this side's loaded config. */
    private int syncedMaxLevel;
    private final EnumMap<WerewolfTreeSkill, Integer> treeSkills = new EnumMap<>(WerewolfTreeSkill.class);
    private final EnumSet<WerewolfAbility> abilities = EnumSet.noneOf(WerewolfAbility.class);
    private final List<UUID> spiritWolfIds = new ArrayList<>();

    public boolean isWerewolf() { return werewolf; }
    public boolean isTransformed() { return form != WerewolfForm.HUMAN; }
    public boolean isInfected() { return infected; }
    public boolean isMoonForced() { return moonForced; }
    public WerewolfForm getForm() { return form; }
    public boolean isBeastMode() { return form == WerewolfForm.BEAST; }
    public boolean isQuadrupedMode() { return form == WerewolfForm.QUADRUPED; }
    public boolean hasDefeatedAlpha() { return alphaDefeated; }
    public boolean isNightVisionEnabled() { return nightVisionEnabled; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public double getExperienceGainRemainder() { return experienceGainRemainder; }
    public int getClawHotbarSlot() { return clawHotbarSlot; }
    public long getAwakeningDayTime() { return awakeningDayTime; }
    public long getWolfSpiritCooldownEnd() { return wolfSpiritCooldownEnd; }
    public long getWolfSpiritExpireTime() { return wolfSpiritExpireTime; }
    public long getBloodyBiteCooldownEnd() { return bloodyBiteCooldownEnd; }
    public long getMoonbloodSurgeCooldownEnd() { return moonbloodSurgeCooldownEnd; }
    public long getMoonbloodCrashTime() { return moonbloodCrashTime; }
    public int getTotemExperienceTicks() { return totemExperienceTicks; }
    public List<UUID> getSpiritWolfIds() { return spiritWolfIds; }
    public static int getMaxLevel() { return HWConfig.MAX_WEREWOLF_LEVEL.get(); }
    public int getEffectiveMaxLevel() { return syncedMaxLevel > 0 ? syncedMaxLevel : getMaxLevel(); }

    public void setWerewolf(boolean value) { werewolf = value; if (!value) form = WerewolfForm.HUMAN; }
    public void setTransformed(boolean value) {
        if (!value || !werewolf) form = WerewolfForm.HUMAN;
        else if (form == WerewolfForm.HUMAN) form = WerewolfForm.WEREWOLF;
    }
    public void setInfected(boolean value) { infected = value; }
    public void setMoonForced(boolean value) { moonForced = value; }
    public void setForm(WerewolfForm value) {
        WerewolfForm requested = value == null ? WerewolfForm.HUMAN : value;
        if (!werewolf || requested == WerewolfForm.HUMAN) {
            form = WerewolfForm.HUMAN;
        } else if (requested == WerewolfForm.QUADRUPED && !hasAbility(WerewolfAbility.QUADRUPED_FORM)) {
            form = WerewolfForm.WEREWOLF;
        } else if (requested == WerewolfForm.BEAST && !alphaDefeated) {
            form = WerewolfForm.WEREWOLF;
        } else {
            form = requested;
        }
    }
    public void setBeastMode(boolean value) {
        if (value) setForm(WerewolfForm.BEAST);
        else if (form == WerewolfForm.BEAST) form = WerewolfForm.WEREWOLF;
    }
    public void setQuadrupedMode(boolean value) {
        if (value) setForm(WerewolfForm.QUADRUPED);
        else if (form == WerewolfForm.QUADRUPED) form = WerewolfForm.WEREWOLF;
    }
    public void setAlphaDefeated(boolean value) {
        alphaDefeated = value;
        if (!value && form == WerewolfForm.BEAST) form = WerewolfForm.WEREWOLF;
    }
    public void setNightVisionEnabled(boolean value) { nightVisionEnabled = value; }
    public void setExperience(int value) { experience = Math.max(0, value); }
    public void setAwakeningDayTime(long value) { awakeningDayTime = value; }
    public void setWolfSpiritCooldownEnd(long value) { wolfSpiritCooldownEnd = Math.max(0L, value); }
    public void setWolfSpiritExpireTime(long value) { wolfSpiritExpireTime = Math.max(0L, value); }
    public void setBloodyBiteCooldownEnd(long value) { bloodyBiteCooldownEnd = Math.max(0L, value); }
    public void setMoonbloodSurgeCooldownEnd(long value) { moonbloodSurgeCooldownEnd = Math.max(0L, value); }
    public void setMoonbloodCrashTime(long value) { moonbloodCrashTime = Math.max(0L, value); }
    public void setTotemExperienceTicks(int value) { totemExperienceTicks = Math.max(0, Math.min(1200, value)); }
    public void setClawHotbarSlot(int value) { clawHotbarSlot = Math.max(0, Math.min(8, value)); }

    public void setLevel(int value) {
        level = Math.max(1, Math.min(getMaxLevel(), value));
        experience = 0;
        experienceGainRemainder = 0.0D;
    }

    public int getTreeSkillRank(WerewolfTreeSkill skill) { return treeSkills.getOrDefault(skill, 0); }
    public void setTreeSkillRank(WerewolfTreeSkill skill, int rank) {
        int clamped = Math.max(0, Math.min(skill.maxRank(), rank));
        if (clamped == 0) treeSkills.remove(skill);
        else treeSkills.put(skill, clamped);
    }
    public boolean hasAbility(WerewolfAbility ability) { return abilities.contains(ability); }
    public void setAbility(WerewolfAbility ability, boolean unlocked) {
        if (unlocked) abilities.add(ability); else abilities.remove(ability);
        if (!abilities.contains(WerewolfAbility.QUADRUPED_FORM) && form == WerewolfForm.QUADRUPED) {
            form = WerewolfForm.WEREWOLF;
        }
    }

    public int getSpentTreePoints() { return treeSkills.values().stream().mapToInt(Integer::intValue).sum(); }
    public int getSpentSkillPoints() { return abilities.stream().mapToInt(WerewolfAbility::cost).sum(); }
    public static int treePointsForLevel(int value) {
        return treePointsForLevel(value, getMaxLevel());
    }
    public static int treePointsForLevel(int value, int maxLevel) {
        int clamped = Math.max(1, Math.min(maxLevel, value));
        return LEVEL_ONE_TREE_POINTS + (clamped - 1) * 2;
    }
    public static int skillPointsForLevel(int value) {
        return skillPointsForLevel(value, getMaxLevel());
    }
    public static int skillPointsForLevel(int value, int maxLevel) {
        int clamped = Math.max(1, Math.min(maxLevel, value));
        return LEVEL_ONE_SKILL_POINTS + clamped - 1;
    }
    public int getEarnedTreePoints() { return treePointsForLevel(level, getEffectiveMaxLevel()); }
    public int getEarnedSkillPoints() { return skillPointsForLevel(level, getEffectiveMaxLevel()); }
    public int getAvailableTreePoints() {
        return Math.max(0, getEarnedTreePoints() + bonusTreePoints - getSpentTreePoints());
    }
    public int getAvailableSkillPoints() {
        return Math.max(0, getEarnedSkillPoints() + bonusSkillPoints - getSpentSkillPoints());
    }
    public void setAvailableTreePoints(int desired) {
        bonusTreePoints = Math.max(0, desired) - getEarnedTreePoints() + getSpentTreePoints();
    }
    public void setAvailableSkillPoints(int desired) {
        bonusSkillPoints = Math.max(0, desired) - getEarnedSkillPoints() + getSpentSkillPoints();
    }
    public void addBonusSkillPoints(int amount) { bonusSkillPoints = Math.max(0, bonusSkillPoints + amount); }
    public void addBonusTreePoints(int amount) { bonusTreePoints = Math.max(0, bonusTreePoints + amount); }

    public boolean upgradeTreeSkill(WerewolfTreeSkill skill) {
        int current = getTreeSkillRank(skill);
        if (!werewolf || getAvailableTreePoints() <= 0 || current >= skill.maxRank()) return false;
        treeSkills.put(skill, current + 1);
        return true;
    }

    public boolean unlockAbility(WerewolfAbility ability) {
        if (!werewolf || abilities.contains(ability) || getAvailableSkillPoints() < ability.cost()) return false;
        abilities.add(ability);
        return true;
    }

    public static int experienceForNextLevel(int currentLevel) {
        return experienceForNextLevel(currentLevel, getMaxLevel());
    }
    public static int experienceForNextLevel(int currentLevel, int maxLevel) {
        return currentLevel >= maxLevel ? 0 : 25 * (2 * currentLevel + 1);
    }
    public int getExperienceForNextLevel() { return experienceForNextLevel(level, getEffectiveMaxLevel()); }
    public static int cumulativeExperienceForLevel(int targetLevel) {
        int clamped = Math.max(1, Math.min(getMaxLevel(), targetLevel));
        return 25 * (clamped * clamped - 1);
    }
    public int addExperience(int amount) {
        int maxLevel = getMaxLevel();
        if (!werewolf || level >= maxLevel || amount <= 0) return 0;
        int levelsGained = 0;
        experience += amount;
        while (level < maxLevel) {
            int needed = experienceForNextLevel(level);
            if (experience < needed) break;
            experience -= needed;
            level++;
            levelsGained++;
        }
        if (level >= maxLevel) experience = 0;
        return levelsGained;
    }

    /** Applies the server growth-rate setting while preserving fractional XP between awards. */
    public int addScaledExperience(int amount) {
        int maxLevel = getMaxLevel();
        if (!werewolf || level >= maxLevel || amount <= 0) return 0;
        double multiplier = HWConfig.WEREWOLF_EXPERIENCE_GAIN_MULTIPLIER.get();
        if (multiplier <= 0.0D) {
            experienceGainRemainder = 0.0D;
            return 0;
        }
        double scaled = amount * multiplier + experienceGainRemainder;
        int wholePoints = scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)Math.floor(scaled);
        experienceGainRemainder = wholePoints == Integer.MAX_VALUE ? 0.0D : scaled - wholePoints;
        int levelsGained = addExperience(wholePoints);
        if (level >= maxLevel) experienceGainRemainder = 0.0D;
        return levelsGained;
    }

    public void resetTreeSkills() { treeSkills.clear(); }
    public void resetAbilities() {
        abilities.clear();
        if (form == WerewolfForm.QUADRUPED) form = WerewolfForm.WEREWOLF;
    }
    public boolean canResetProgression() { return werewolf && level > 5; }
    public boolean resetProgressionWithLevelCost() {
        if (!canResetProgression()) return false;
        level -= 5;
        experience = 0;
        experienceGainRemainder = 0.0D;
        bonusSkillPoints = 0;
        bonusTreePoints = 0;
        treeSkills.clear();
        abilities.clear();
        if (form != WerewolfForm.HUMAN) form = WerewolfForm.WEREWOLF;
        wolfSpiritCooldownEnd = 0L;
        wolfSpiritExpireTime = 0L;
        bloodyBiteCooldownEnd = 0L;
        moonbloodSurgeCooldownEnd = 0L;
        moonbloodCrashTime = 0L;
        return true;
    }
    public void prepareAfterDeath() { form = WerewolfForm.HUMAN; moonForced = false; moonbloodCrashTime = 0L; }

    /** Returns true only for a newly-created / fully cured default state. */
    public boolean isDefaultState() {
        return !werewolf && form == WerewolfForm.HUMAN && !infected && !moonForced && !alphaDefeated
                && nightVisionEnabled && level == 1 && experience == 0
                && experienceGainRemainder == 0.0D
                && bonusSkillPoints == 0 && bonusTreePoints == 0 && clawHotbarSlot == 1
                && awakeningDayTime == -1L && wolfSpiritCooldownEnd == 0L
                && wolfSpiritExpireTime == 0L && bloodyBiteCooldownEnd == 0L
                && moonbloodSurgeCooldownEnd == 0L && moonbloodCrashTime == 0L
                && totemExperienceTicks == 0 && getSpentTreePoints() == 0 && abilities.isEmpty()
                && spiritWolfIds.isEmpty();
    }

    public void reset() {
        werewolf = false;
        infected = false;
        moonForced = false;
        form = WerewolfForm.HUMAN;
        alphaDefeated = false;
        nightVisionEnabled = true;
        level = 1;
        experience = 0;
        experienceGainRemainder = 0.0D;
        bonusSkillPoints = 0;
        bonusTreePoints = 0;
        clawHotbarSlot = 1;
        awakeningDayTime = -1L;
        wolfSpiritCooldownEnd = 0L;
        wolfSpiritExpireTime = 0L;
        bloodyBiteCooldownEnd = 0L;
        moonbloodSurgeCooldownEnd = 0L;
        moonbloodCrashTime = 0L;
        totemExperienceTicks = 0;
        treeSkills.clear();
        abilities.clear();
        spiritWolfIds.clear();
        syncedMaxLevel = 0;
    }

    public void copyFrom(WerewolfData other) {
        werewolf = other.werewolf;
        infected = other.infected;
        moonForced = other.moonForced;
        form = other.form;
        alphaDefeated = other.alphaDefeated;
        nightVisionEnabled = other.nightVisionEnabled;
        level = other.level;
        experience = other.experience;
        experienceGainRemainder = other.experienceGainRemainder;
        bonusSkillPoints = other.bonusSkillPoints;
        bonusTreePoints = other.bonusTreePoints;
        clawHotbarSlot = other.clawHotbarSlot;
        awakeningDayTime = other.awakeningDayTime;
        wolfSpiritCooldownEnd = other.wolfSpiritCooldownEnd;
        wolfSpiritExpireTime = other.wolfSpiritExpireTime;
        bloodyBiteCooldownEnd = other.bloodyBiteCooldownEnd;
        moonbloodSurgeCooldownEnd = other.moonbloodSurgeCooldownEnd;
        moonbloodCrashTime = other.moonbloodCrashTime;
        totemExperienceTicks = other.totemExperienceTicks;
        syncedMaxLevel = other.syncedMaxLevel;
        treeSkills.clear(); treeSkills.putAll(other.treeSkills);
        abilities.clear(); abilities.addAll(other.abilities);
        spiritWolfIds.clear(); spiritWolfIds.addAll(other.spiritWolfIds);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", CURRENT_DATA_VERSION);
        tag.putBoolean("Werewolf", werewolf);
        tag.putBoolean("Infected", infected);
        tag.putBoolean("MoonForced", moonForced);
        tag.putString("Form", form.id());
        tag.putBoolean("AlphaDefeated", alphaDefeated);
        tag.putBoolean("NightVisionEnabled", nightVisionEnabled);
        tag.putInt("Level", level);
        tag.putInt("Experience", experience);
        tag.putDouble("ExperienceGainRemainder", experienceGainRemainder);
        tag.putInt("BonusSkillPoints", bonusSkillPoints);
        tag.putInt("BonusTreePoints", bonusTreePoints);
        tag.putInt("ClawHotbarSlot", clawHotbarSlot);
        tag.putLong("AwakeningDayTime", awakeningDayTime);
        tag.putLong("WolfSpiritCooldownEnd", wolfSpiritCooldownEnd);
        tag.putLong("WolfSpiritExpireTime", wolfSpiritExpireTime);
        tag.putLong("BloodyBiteCooldownEnd", bloodyBiteCooldownEnd);
        tag.putLong("MoonbloodSurgeCooldownEnd", moonbloodSurgeCooldownEnd);
        tag.putLong("MoonbloodCrashTime", moonbloodCrashTime);
        tag.putInt("TotemExperienceTicks", totemExperienceTicks);
        CompoundTag treeTag = new CompoundTag();
        for (Map.Entry<WerewolfTreeSkill, Integer> entry : treeSkills.entrySet()) {
            treeTag.putInt(entry.getKey().id(), entry.getValue());
        }
        tag.put("TreeSkills", treeTag);
        ListTag abilityTag = new ListTag();
        for (WerewolfAbility ability : abilities) abilityTag.add(StringTag.valueOf(ability.id()));
        tag.put("Abilities", abilityTag);
        ListTag spiritTag = new ListTag();
        for (UUID id : spiritWolfIds) spiritTag.add(StringTag.valueOf(id.toString()));
        tag.put("SpiritWolves", spiritTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        tag = migrateData(tag);
        syncedMaxLevel = tag.contains("NetworkMaxLevel", Tag.TAG_INT)
                ? Math.max(1, Math.min(MAX_LEVEL_LIMIT, tag.getInt("NetworkMaxLevel"))) : 0;
        werewolf = tag.getBoolean("Werewolf");
        infected = tag.getBoolean("Infected");
        moonForced = tag.getBoolean("MoonForced");
        form = WerewolfForm.byName(tag.getString("Form"));
        alphaDefeated = tag.getBoolean("AlphaDefeated");
        nightVisionEnabled = !tag.contains("NightVisionEnabled") || tag.getBoolean("NightVisionEnabled");
        level = Math.max(1, Math.min(getEffectiveMaxLevel(), tag.getInt("Level")));
        experience = Math.max(0, tag.getInt("Experience"));
        double storedRemainder = tag.contains("ExperienceGainRemainder")
                ? tag.getDouble("ExperienceGainRemainder") : 0.0D;
        experienceGainRemainder = Double.isFinite(storedRemainder)
                && storedRemainder >= 0.0D && storedRemainder < 1.0D ? storedRemainder : 0.0D;
        bonusSkillPoints = tag.getInt("BonusSkillPoints");
        bonusTreePoints = tag.getInt("BonusTreePoints");
        clawHotbarSlot = tag.contains("ClawHotbarSlot")
                ? Math.max(0, Math.min(8, tag.getInt("ClawHotbarSlot"))) : 1;
        awakeningDayTime = tag.contains("AwakeningDayTime") ? tag.getLong("AwakeningDayTime") : -1L;
        wolfSpiritCooldownEnd = Math.max(0L, tag.getLong("WolfSpiritCooldownEnd"));
        wolfSpiritExpireTime = Math.max(0L, tag.getLong("WolfSpiritExpireTime"));
        bloodyBiteCooldownEnd = Math.max(0L, tag.getLong("BloodyBiteCooldownEnd"));
        moonbloodSurgeCooldownEnd = Math.max(0L, tag.getLong("MoonbloodSurgeCooldownEnd"));
        moonbloodCrashTime = Math.max(0L, tag.getLong("MoonbloodCrashTime"));
        totemExperienceTicks = Math.max(0, Math.min(1200, tag.getInt("TotemExperienceTicks")));
        treeSkills.clear();
        CompoundTag treeTag = tag.getCompound("TreeSkills");
        for (WerewolfTreeSkill skill : WerewolfTreeSkill.values()) setTreeSkillRank(skill, treeTag.getInt(skill.id()));
        abilities.clear();
        ListTag abilityTag = tag.getList("Abilities", Tag.TAG_STRING);
        for (int i = 0; i < abilityTag.size(); i++) {
            WerewolfAbility ability = WerewolfAbility.byName(abilityTag.getString(i));
            if (ability != null) abilities.add(ability);
        }
        spiritWolfIds.clear();
        ListTag spiritTag = tag.getList("SpiritWolves", Tag.TAG_STRING);
        for (int i = 0; i < spiritTag.size(); i++) {
            try { spiritWolfIds.add(UUID.fromString(spiritTag.getString(i))); }
            catch (IllegalArgumentException ignored) {}
        }
        if (!werewolf) {
            form = WerewolfForm.HUMAN;
            moonForced = false;
            experienceGainRemainder = 0.0D;
        }
        if (form == WerewolfForm.QUADRUPED && !abilities.contains(WerewolfAbility.QUADRUPED_FORM)) {
            form = WerewolfForm.WEREWOLF;
        }
        if (form == WerewolfForm.BEAST && !alphaDefeated) form = WerewolfForm.WEREWOLF;
    }

    private static CompoundTag migrateData(CompoundTag source) {
        int version = source.contains("DataVersion", Tag.TAG_INT)
                ? Math.max(0, source.getInt("DataVersion")) : 0;
        if (version >= CURRENT_DATA_VERSION) return source;

        CompoundTag migrated = source.copy();
        if (version < 1) {
            // Version 0 is the unversioned 0.3.x schema. Its field names already match 0.4.0;
            // optional fields continue to use the backward-compatible defaults above.
            version = 1;
        }
        if (version < 2) {
            WerewolfForm legacyForm = migrated.getBoolean("BeastMode")
                    ? WerewolfForm.BEAST
                    : migrated.getBoolean("Transformed") ? WerewolfForm.WEREWOLF : WerewolfForm.HUMAN;
            migrated.putString("Form", legacyForm.id());
            migrated.putBoolean("AlphaDefeated", migrated.getBoolean("BeastMode"));
            migrated.remove("Transformed");
            migrated.remove("BeastMode");
            version = 2;
        }
        migrated.putInt("DataVersion", version);
        return migrated;
    }
}
