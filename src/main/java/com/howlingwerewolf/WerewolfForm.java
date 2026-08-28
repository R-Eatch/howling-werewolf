package com.howlingwerewolf;

import java.util.Locale;

/**
 * The player's current physical form. Infection and lycanthropy ownership remain separate
 * progression states; this enum only describes the body currently rendered and simulated.
 */
public enum WerewolfForm {
    HUMAN,
    WEREWOLF,
    QUADRUPED,
    BEAST;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static WerewolfForm byName(String value) {
        for (WerewolfForm form : values()) {
            if (form.name().equalsIgnoreCase(value)) return form;
        }
        return HUMAN;
    }
}
