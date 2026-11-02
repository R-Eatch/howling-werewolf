package com.howlingwerewolf;

/** Names follow the nine texture sets embedded in the supplied Blockbench projects. */
public enum ModernSkin {
    ASHEN("Ashen"), BLACK("Black"), CHESTNUT("Chestnut"), PALE("Pale"), RUSTY("Rusty"),
    SNOWY("Snowy"), SPOTTED("Spotted"), STRIPPED("Stripped"), WOODS("Woods");

    private final String displayName;

    ModernSkin(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
    public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
    public String skinId() { return HowlingWerewolf.MOD_ID + ":modern/" + id(); }

    public static ModernSkin bySkinId(String id) {
        for (ModernSkin skin : values()) if (skin.skinId().equals(id)) return skin;
        return null;
    }

    public static ModernSkin byId(String id) {
        for (ModernSkin skin : values()) if (skin.id().equals(id)) return skin;
        return ASHEN;
    }
}
