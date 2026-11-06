/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Per-player interface preferences in config/howlingwerewolf-client.json; nothing here reaches the server. */
public final class HWClientConfig {
    private static boolean loaded;
    private static boolean nightfall;

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve(HowlingWerewolf.MOD_ID + "-client.json"); }
    private static void load() {
        if (loaded) return;
        loaded = true;
        try {
            Path path = path();
            if (!Files.exists(path)) { save(); return; }
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            if (root.has("interface.progressionNightfall")) nightfall = root.get("interface.progressionNightfall").getAsBoolean();
        } catch (Exception error) {
            HowlingWerewolf.LOGGER.error("Unable to load {}. Using defaults.", path(), error);
        }
    }
    private static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("interface.progressionNightfall", nightfall);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.createDirectories(path().getParent());
            Files.writeString(path(), gson.toJson(root) + "\n");
        } catch (Exception error) {
            HowlingWerewolf.LOGGER.error("Unable to save {}.", path(), error);
        }
    }

    /** Show the progression screen on its dark Nightfall canvas instead of the white Moonlight one. */
    public static boolean nightfall() { load(); return nightfall; }
    public static void setNightfall(boolean value) { load(); nightfall = value; save(); }

    private HWClientConfig() {}
}
