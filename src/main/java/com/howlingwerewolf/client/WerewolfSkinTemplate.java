package com.howlingwerewolf.client;

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
        String namespace = "wolfskin_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Map<String, byte[]> contents = new LinkedHashMap<>();
        for (String name : FILES) {
            try (InputStream input = WerewolfSkinTemplate.class.getResourceAsStream("/howlingwerewolf_skin_template/" + name)) {
                if (input == null) throw new IOException("Missing bundled template file: " + name);
                byte[] data = input.readAllBytes();
                if (!name.endsWith(".png")) {
                    data = new String(data, StandardCharsets.UTF_8).replace("mywolf", namespace).getBytes(StandardCharsets.UTF_8);
                }
                contents.put(name.replace("assets/mywolf/", "assets/" + namespace + "/"), data);
            }
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

    private WerewolfSkinTemplate() {}
}
