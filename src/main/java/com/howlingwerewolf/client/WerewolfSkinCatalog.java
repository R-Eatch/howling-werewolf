package com.howlingwerewolf.client;

import com.google.gson.JsonObject;
import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Resource-pack skins contain textures and display text, never executable model data. */
public final class WerewolfSkinCatalog extends SimplePreparableReloadListener<WerewolfSkinCatalog.Prepared> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "werewolf_skins/";
    private static final int MAX_SKINS = 256;
    private static final int MAX_JSON_BYTES = 16 * 1024;
    private static final int MAX_PNG_BYTES = 1024 * 1024;
    private static volatile Snapshot snapshot = snapshot(builtins(), 0, 0);

    public record Entry(String id, String name, String description, String author,
                        Map<WerewolfForm, ResourceLocation> textures, boolean builtin) {
        public Entry { textures = Map.copyOf(textures); }
    }

    protected record Prepared(List<Entry> entries, int errors) {}
    private record Snapshot(List<Entry> entries, Map<String, Entry> byId, int revision, int errors) {}

    public static List<Entry> entries() { return snapshot.entries(); }
    public static int revision() { return snapshot.revision(); }
    public static int errorCount() { return snapshot.errors(); }
    public static Entry find(String id) { return snapshot.byId().get(WerewolfSkinIds.normalize(id)); }

    public static ResourceLocation texture(String id, WerewolfForm form) {
        Snapshot current = snapshot;
        Entry skin = current.byId().getOrDefault(WerewolfSkinIds.normalize(id),
                current.byId().get(WerewolfSkinIds.DEFAULT_ID));
        return skin.textures().get(form == WerewolfForm.HUMAN ? WerewolfForm.WEREWOLF : form);
    }

    private static List<Entry> builtins() {
        List<Entry> result = new ArrayList<>();
        for (WerewolfSkin skin : WerewolfSkin.values()) {
            String base = "textures/entity/" + (skin == WerewolfSkin.ADRIAN ? "" : "skins/" + skin.getId() + "/");
            Map<WerewolfForm, ResourceLocation> textures = new EnumMap<>(WerewolfForm.class);
            textures.put(WerewolfForm.WEREWOLF, new ResourceLocation(HowlingWerewolf.MOD_ID, base + "werewolf.png"));
            textures.put(WerewolfForm.QUADRUPED, new ResourceLocation(HowlingWerewolf.MOD_ID, base + "quadruped_werewolf.png"));
            textures.put(WerewolfForm.BEAST, new ResourceLocation(HowlingWerewolf.MOD_ID, base + "beast.png"));
            String name = skin.getId().substring(0, 1).toUpperCase(java.util.Locale.ROOT) + skin.getId().substring(1);
            result.add(new Entry(HowlingWerewolf.MOD_ID + ":" + skin.getId(), name, "", "R_Eatch", textures, true));
        }
        return result;
    }

    private static Snapshot snapshot(List<Entry> entries, int revision, int errors) {
        Map<String, Entry> byId = new LinkedHashMap<>();
        entries.forEach(entry -> byId.put(entry.id(), entry));
        return new Snapshot(List.copyOf(entries), Map.copyOf(byId), revision, errors);
    }

    @Override
    protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        List<Entry> result = builtins();
        int errors = 0;
        var resources = manager.listResources("werewolf_skins", id -> id.getPath().endsWith(".json"));
        var files = resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        for (int index = 0; index < files.size(); index++) {
            if (index >= MAX_SKINS) {
                errors += files.size() - index;
                LOGGER.warn("Ignoring custom werewolf skins beyond the {}-entry limit", MAX_SKINS);
                break;
            }
            var file = files.get(index);
            try {
                ResourceLocation location = file.getKey();
                String path = location.getPath();
                String id = location.getNamespace() + ":" + path.substring(DIRECTORY.length(), path.length() - 5);
                if (location.getNamespace().equals(HowlingWerewolf.MOD_ID)
                        || !id.equals(WerewolfSkinIds.canonicalize(id))) {
                    throw new IOException("Use a valid custom namespace; howlingwerewolf is reserved");
                }
                String document = new String(readBounded(file.getValue(), MAX_JSON_BYTES), StandardCharsets.UTF_8);
                validateJsonDepth(document);
                JsonObject json = GsonHelper.parse(document);
                String name = text(json, "name", "", 48);
                if (name.isBlank()) throw new IOException("name must not be empty");
                String description = text(json, "description", "", 160);
                String author = text(json, "author", "", 48);
                JsonObject definitions = GsonHelper.getAsJsonObject(json, "textures");
                Map<WerewolfForm, ResourceLocation> textures = new EnumMap<>(WerewolfForm.class);
                for (WerewolfForm form : List.of(WerewolfForm.WEREWOLF, WerewolfForm.QUADRUPED, WerewolfForm.BEAST)) {
                    String reference = GsonHelper.getAsString(definitions, form.id());
                    ResourceLocation texture = ResourceLocation.tryParse(reference);
                    if (reference.length() > 256 || texture == null || !texture.toString().equals(reference)
                            || !texture.getPath().startsWith("textures/") || !texture.getPath().endsWith(".png")
                            || !WerewolfSkinIds.validPath(texture.getPath())) {
                        throw new IOException("Invalid texture resource: " + form.id());
                    }
                    Resource resource = manager.getResource(texture).orElseThrow(() -> new IOException("Missing texture: " + texture));
                    validatePng(readBounded(resource, MAX_PNG_BYTES), form);
                    textures.put(form, texture);
                }
                result.add(new Entry(id, name, description, author, textures, false));
            } catch (Exception error) {
                errors++;
                LOGGER.warn("Ignoring invalid werewolf skin {}: {}", file.getKey(), error.getMessage());
            }
        }
        return new Prepared(List.copyOf(result), errors);
    }

    @Override
    protected void apply(Prepared prepared, ResourceManager manager, ProfilerFiller profiler) {
        snapshot = snapshot(prepared.entries(), snapshot.revision() + 1, prepared.errors());
    }

    private static String text(JsonObject json, String key, String fallback, int max) throws IOException {
        String value = GsonHelper.getAsString(json, key, fallback).trim();
        if (value.length() > max || value.chars().anyMatch(Character::isISOControl)) {
            throw new IOException(key + " must be plain text of at most " + max + " characters");
        }
        return value;
    }

    private static void validateJsonDepth(String document) throws IOException {
        int depth = 0;
        boolean quoted = false;
        boolean escaped = false;
        for (int index = 0; index < document.length(); index++) {
            char character = document.charAt(index);
            if (quoted) {
                if (escaped) escaped = false;
                else if (character == '\\') escaped = true;
                else if (character == '"') quoted = false;
            } else if (character == '"') quoted = true;
            else if (character == '{' || character == '[') {
                if (++depth > 16) throw new IOException("Skin JSON nesting exceeds 16 levels");
            } else if (character == '}' || character == ']') {
                if (--depth < 0) throw new IOException("Unbalanced skin JSON");
            } else if (character == '\'' || character == '/' || character == '#') {
                // Gson also accepts some non-JSON quoting/comments. Reject them so this
                // depth bound describes the same strings and containers as the parser.
                throw new IOException("Use standard JSON with double-quoted strings and no comments");
            }
        }
    }

    private static byte[] readBounded(Resource resource, int max) throws IOException {
        try (InputStream input = resource.open()) {
            byte[] bytes = input.readNBytes(max + 1);
            if (bytes.length > max) throw new IOException("Resource exceeds " + max + " bytes");
            return bytes;
        }
    }

    private static void validatePng(byte[] bytes, WerewolfForm form) throws IOException {
        // Validate dimensions before native decoding so a tiny compressed file cannot request
        // an enormous allocation. NativeImage then checks the actual image data as well.
        if (bytes.length < 33 || ByteBuffer.wrap(bytes).getLong() != 0x89504E470D0A1A0AL
                || ByteBuffer.wrap(bytes, 8, 4).getInt() != 13
                || ByteBuffer.wrap(bytes, 12, 4).getInt() != 0x49484452) {
            throw new IOException("Not a PNG with a valid IHDR");
        }
        int width = ByteBuffer.wrap(bytes, 16, 4).getInt();
        int height = ByteBuffer.wrap(bytes, 20, 4).getInt();
        int baseWidth = form == WerewolfForm.QUADRUPED ? 64 : 128;
        int baseHeight = form == WerewolfForm.QUADRUPED ? 32 : 128;
        int scale = width / baseWidth;
        if (scale < 1 || scale > 4 || width != baseWidth * scale || height != baseHeight * scale) {
            throw new IOException("Wrong " + form.id() + " size; expected " + baseWidth + "x" + baseHeight + " at 1x-4x scale");
        }
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes))) {
            if (image.getWidth() != width || image.getHeight() != height) throw new IOException("PNG dimensions disagree");
        }
    }
}
