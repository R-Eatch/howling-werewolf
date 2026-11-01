package com.howlingwerewolf;

/** A player's model preference is saved and synchronized independently of the observing client. */
public enum WerewolfModelSet {
    CLASSIC("classic", "Classic"),
    MODERN("modern", "Modern");

    private final String id;
    private final String displayName;

    WerewolfModelSet(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String defaultSkinId() {
        return this == MODERN ? ModernSkin.ASHEN.skinId() : WerewolfSkinIds.DEFAULT_ID;
    }

    public static WerewolfModelSet parse(String id) {
        for (WerewolfModelSet set : values()) if (set.id.equals(id)) return set;
        return null;
    }

    public static WerewolfModelSet byId(String id) {
        WerewolfModelSet set = parse(id);
        return set == null ? CLASSIC : set;
    }
}
