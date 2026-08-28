package com.howlingwerewolf;

import java.util.Locale;

public enum WerewolfAbility {
    NIGHT_VISION(1), SUMMON_WOLF_SPIRIT(3), QUADRUPED_FORM(4), MOONBLOOD_SURGE(3),
    ARMORED_INSTINCT(2), HARD_LIFE(2), EMPTY_CLAW_SLOT(1), LONG_CLAWS(2),
    TOOL_CLAWS(2), FIRE_CLAWS(2), BLOODY_BITE(3);

    private final int cost;

    WerewolfAbility(int cost) { this.cost = cost; }
    public int cost() { return cost; }
    public String id() { return name().toLowerCase(Locale.ROOT); }
    public String translationKey() { return "ability.howlingwerewolf." + id(); }

    public static WerewolfAbility byName(String value) {
        for (WerewolfAbility ability : values()) {
            if (ability.name().equalsIgnoreCase(value)) return ability;
        }
        return null;
    }
}
