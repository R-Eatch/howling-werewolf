package com.howlingwerewolf.client;

import com.google.gson.JsonObject;
import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Exports the bundled painting kit; never overwrites an existing player-created file. */
public final class WerewolfSkinTemplate {
    private static final List<String> FILES = List.of(
            "assets/mywolf/werewolf_skins/custom.json",
            "assets/mywolf/textures/entity/werewolf.png",
            "assets/mywolf/textures/entity/quadruped_werewolf.png",
            "assets/mywolf/textures/entity/beast.png",
            "blockbench/werewolf.bbmodel", "blockbench/quadruped.bbmodel", "blockbench/beast.bbmodel",
            "CUSTOM_SKINS.md", "CUSTOM_SKINS-zh.md", "LICENSE-ASSETS.md", "pack.mcmeta");

    public static Path export(Path resourcePackDirectory) throws IOException {
        return export(resourcePackDirectory, WerewolfModelSet.CLASSIC, "howlingwerewolf:adrian");
    }

    public static Path export(Path resourcePackDirectory, WerewolfModelSet modelSet, String skinId) throws IOException {
        String namespace = "wolfskin_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Map<String, byte[]> contents = new LinkedHashMap<>();
        for (String name : FILES) {
            byte[] data = templateFile(name, modelSet, skinId);
            if (!name.endsWith(".png")) {
                data = new String(data, StandardCharsets.UTF_8).replace("mywolf", namespace).getBytes(StandardCharsets.UTF_8);
            }
            contents.put(name.replace("assets/mywolf/", "assets/" + namespace + "/"), data);
        }
        Path parent = resourcePackDirectory.toAbsolutePath().normalize();
        Files.createDirectories(parent);
        Path target = null;
        for (int index = 1; index <= 1000; index++) {
            Path candidate = parent.resolve("howlingwerewolf-custom-skin" + (index == 1 ? "" : "-" + index));
            try {
                target = Files.createDirectory(candidate);
                break;
            } catch (FileAlreadyExistsException existing) {
                // A previous kit may contain irreplaceable player artwork.
            }
        }
        if (target == null) throw new IOException("Too many existing template directories");
        for (var file : contents.entrySet()) {
            Path output = target.resolve(file.getKey());
            Files.createDirectories(output.getParent());
            Files.write(output, file.getValue(), StandardOpenOption.CREATE_NEW);
        }
        // pack.mcmeta is written last, so an interrupted export is not recognized as a pack.
        return target;
    }

    private static byte[] templateFile(String name, WerewolfModelSet modelSet, String skinId) throws IOException {
        if (modelSet == WerewolfModelSet.MODERN && (name.endsWith(".bbmodel") || name.endsWith(".png"))) {
            WerewolfForm form = name.contains("beast") ? WerewolfForm.BEAST
                    : name.contains("quadruped") ? WerewolfForm.QUADRUPED : WerewolfForm.WEREWOLF;
            byte[] model = resource("/assets/howlingwerewolf/" + ModernModelResources.modelId(form).getPath());
            ModernSkin skin = ModernSkin.bySkinId(skinId);
            if (skin == null) skin = ModernSkin.ASHEN;
            String suffix = form == WerewolfForm.BEAST ? "_Beast" : form == WerewolfForm.QUADRUPED ? "_Wolf" : "_Werewolf";
            JsonObject json = GsonHelper.parse(new String(model, StandardCharsets.UTF_8));
            for (var texture : json.getAsJsonArray("textures")) {
                JsonObject object = texture.getAsJsonObject();
                if ((skin.displayName() + suffix).equals(GsonHelper.getAsString(object, "name"))) {
                    if (name.endsWith(".bbmodel")) {
                        // The source projects contain nine palettes and inconsistent active indices.
                        // A painting kit must open with the same single coat as its exported PNG.
                        var selected = new com.google.gson.JsonArray();
                        selected.add(object.deepCopy());
                        json.add("textures", selected);
                        for (var element : json.getAsJsonArray("elements")) {
                            for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                                face.getValue().getAsJsonObject().addProperty("texture", 0);
                            }
                        }
                        return json.toString().getBytes(StandardCharsets.UTF_8);
                    }
                    return java.util.Base64.getDecoder().decode(GsonHelper.getAsString(object, "source").substring("data:image/png;base64,".length()));
                }
            }
            throw new IOException("Missing Modern template texture");
        }
        byte[] data = resource("/howlingwerewolf_skin_template/" + name);
        if (name.endsWith("custom.json")) {
            JsonObject json = GsonHelper.parse(new String(data, StandardCharsets.UTF_8));
            json.addProperty("model", modelSet.id());
            data = json.toString().getBytes(StandardCharsets.UTF_8);
        }
        return data;
    }

    private static byte[] resource(String name) throws IOException {
        try (InputStream input = WerewolfSkinTemplate.class.getResourceAsStream(name)) {
            if (input == null) throw new IOException("Missing bundled template file: " + name);
            return input.readAllBytes();
        }
    }

    private WerewolfSkinTemplate() {}
}
