package com.howlingwerewolf;

import java.util.Locale;

public enum WerewolfTreeSkill {
    DAMAGE(6), MOON_REND(3), DEFENSE(5), SPEED(5), RESISTANCE(5), REGENERATION(5),
    JUMP(3), KNOCKBACK_RESISTANCE(2), FALL_RESISTANCE(2), LIFESTEAL(3), SATIETY(3),
    CLAW_EFFICIENCY(3), HUNTING_MASTERY(2);

    private final int maxRank;

    WerewolfTreeSkill(int maxRank) { this.maxRank = maxRank; }
    public int maxRank() { return maxRank; }
    public String id() { return name().toLowerCase(Locale.ROOT); }
    public String translationKey() { return "tree_skill.howlingwerewolf." + id(); }

    public static WerewolfTreeSkill byName(String value) {
        for (WerewolfTreeSkill skill : values()) {
            if (skill.name().equalsIgnoreCase(value)) return skill;
        }
        return null;
    }
}
