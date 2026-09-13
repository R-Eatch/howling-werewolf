package com.howlingwerewolf;

import net.minecraft.resources.ResourceLocation;

/** Shared validation for persisted skin selections and server-bound cosmetic requests. */
public final class WerewolfSkinIds {
    public static final int MAX_ID_LENGTH = 128;
    public static final String DEFAULT_ID = HowlingWerewolf.MOD_ID + ":adrian";

    /** Returns a canonical ID, or null for unsupported syntax/reserved built-in IDs. */
    public static String canonicalize(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_ID_LENGTH) return null;
        if (value.indexOf(':') < 0) {
            // The 1.1.0 save format used bare built-in names.
            for (WerewolfSkin skin : WerewolfSkin.values()) {
                if (skin.getId().equals(value)) return HowlingWerewolf.MOD_ID + ":" + value;
            }
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null || id.getNamespace().isEmpty() || !validPath(id.getPath())
                || !id.toString().equals(value)) return null;
        if (HowlingWerewolf.MOD_ID.equals(id.getNamespace())) {
            for (WerewolfSkin skin : WerewolfSkin.values()) {
                if (skin.getId().equals(id.getPath())) return id.toString();
            }
            return null;
        }
        return id.toString();
    }

    public static String normalize(String value) {
        String canonical = canonicalize(value);
        return canonical == null ? DEFAULT_ID : canonical;
    }

    /** Resource-pack paths are relative, with no empty or traversal segments. */
    public static boolean validPath(String path) {
        if (path.isEmpty() || path.startsWith("/") || path.endsWith("/")) return false;
        for (String segment : path.split("/")) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) return false;
        }
        return true;
    }

    private WerewolfSkinIds() {}
}
