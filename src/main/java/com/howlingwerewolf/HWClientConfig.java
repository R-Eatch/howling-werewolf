/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Per-player interface preferences; nothing here reaches the server. */
public final class HWClientConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue NIGHTFALL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("interface");
        NIGHTFALL = builder.comment("Show the progression screen on its dark Nightfall canvas instead of the white Moonlight one.")
                .define("progressionNightfall", false);
        builder.pop();
        SPEC = builder.build();
    }

    public static boolean nightfall() { return SPEC.isLoaded() && NIGHTFALL.get(); }
    public static void setNightfall(boolean value) {
        if (!SPEC.isLoaded()) return;
        NIGHTFALL.set(value);
        NIGHTFALL.save();
    }

    private HWClientConfig() {}
}
