package com.howlingwerewolf;

/** A cosmetic coat shared by all of the player's werewolf forms. */
public enum WerewolfSkin {
    ADRIAN("adrian"),
    ASHEN("ashen"),
    ONYX("onyx");

    private final String id;

    WerewolfSkin(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /** Older saves and unknown IDs retain the original Adrian appearance. */
    public static WerewolfSkin byId(String id) {
        for (WerewolfSkin skin : values()) {
            if (skin.id.equals(id)) return skin;
        }
        return ADRIAN;
    }
}
