package com.howlingwerewolf.client;

import com.google.gson.JsonObject;
import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.HumanoidArm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Geometry and all 27 original PNGs are read directly from the three bundled bbmodel files. */
public final class ModernModelResources extends SimplePreparableReloadListener<ModernModelResources.Prepared> implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "modern_models");
    }

    static final List<WerewolfForm> FORMS = List.of(WerewolfForm.WEREWOLF, WerewolfForm.QUADRUPED, WerewolfForm.BEAST);
    private static final ResourceLocation PLAYER_GEOMETRY = ResourceLocation.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "models/entity/modern/werewolf_player.bbmodel");
    private static Map<WerewolfForm, BlockbenchModel> models = Map.of();
    private static PlayerParts playerParts;
    private static int revision;

    protected record Prepared(Map<WerewolfForm, BlockbenchModel> models, Map<ResourceLocation, byte[]> textures,
                              PlayerParts playerParts) {}

    record PlayerParts(ModelPart head, ModelPart tail, ModelPart tailTip, ModelPart rightArm, ModelPart leftArm) {
        ModelPart arm(HumanoidArm side) { return side == HumanoidArm.RIGHT ? rightArm : leftArm; }
    }

    static ResourceLocation modelId(WerewolfForm form) {
        return ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "models/entity/modern/" + form.id() + ".bbmodel");
    }

    static ResourceLocation textureId(ModernSkin skin, WerewolfForm form) {
        return ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "textures/entity/modern/" + skin.id() + "/" + form.id() + ".png");
    }

    static BlockbenchModel model(WerewolfForm form) {
        BlockbenchModel model = models.get(form);
        if (model == null) throw new IllegalStateException("Modern models have not finished loading");
        return model;
    }

    static int revision() { return revision; }
    static PlayerParts playerParts() { return playerParts; }

    @Override
    protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<WerewolfForm, BlockbenchModel> definitions = new EnumMap<>(WerewolfForm.class);
        Map<ResourceLocation, byte[]> textures = new LinkedHashMap<>();
        for (WerewolfForm form : FORMS) {
            try {
                JsonObject json = readGeometry(manager, modelId(form));
                BlockbenchModel definition = new BlockbenchModel(json);
                definition.bake(ModernPlayerModel.height(form));
                definitions.put(form, definition);
                String suffix = switch (form) {
                    case BEAST -> "_Beast";
                    case QUADRUPED -> "_Wolf";
                    default -> "_Werewolf";
                };
                for (ModernSkin skin : ModernSkin.values()) {
                    JsonObject texture = null;
                    for (var candidate : json.getAsJsonArray("textures")) {
                        if ((skin.displayName() + suffix).equals(GsonHelper.getAsString(candidate.getAsJsonObject(), "name"))) {
                            texture = candidate.getAsJsonObject();
                            break;
                        }
                    }
                    if (texture == null) throw new IOException("Missing embedded texture: " + skin.displayName() + suffix);
                    String source = GsonHelper.getAsString(texture, "source");
                    String prefix = "data:image/png;base64,";
                    if (!source.startsWith(prefix)) throw new IOException("Texture must be an embedded PNG");
                    byte[] png = Base64.getDecoder().decode(source.substring(prefix.length()));
                    validatePng(png);
                    textures.put(textureId(skin, form), png);
                }
            } catch (IOException error) {
                throw new UncheckedIOException("Unable to load Modern model " + modelId(form), error);
            } catch (RuntimeException error) {
                throw new IllegalArgumentException("Invalid Modern model " + modelId(form), error);
            }
        }
        try {
            var geometry = new BlockbenchModel(readGeometry(manager, PLAYER_GEOMETRY));
            var bones = geometry.bake(ModernPlayerModel.height(WerewolfForm.WEREWOLF));
            // The parent PlayerModel supplies the animated head, arm and torso transforms.
            // Read the named subtrees directly at their Blockbench size, without the scaled root pose.
            var parts = new PlayerParts(atParentOrigin(bones.bone("head")), bones.bone("tail_main12"), bones.bone("tail_12"),
                    atParentOrigin(bones.bone("rightArm")), atParentOrigin(bones.bone("leftArm")));
            return new Prepared(Map.copyOf(definitions), Map.copyOf(textures), parts);
        } catch (IOException error) {
            throw new UncheckedIOException("Unable to load Modern model " + PLAYER_GEOMETRY, error);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Invalid Modern model " + PLAYER_GEOMETRY, error);
        }
    }

    private static JsonObject readGeometry(ResourceManager manager, ResourceLocation id) throws IOException {
        try (InputStream input = manager.open(id)) {
            byte[] bytes = input.readNBytes(8 * 1024 * 1024 + 1);
            if (bytes.length > 8 * 1024 * 1024) throw new IOException("Model exceeds 8 MiB");
            return GsonHelper.parse(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static ModelPart atParentOrigin(ModelPart part) {
        part.setInitialPose(PartPose.ZERO);
        part.loadPose(PartPose.ZERO);
        return part;
    }

    @Override
    protected void apply(Prepared prepared, ResourceManager manager, ProfilerFiller profiler) {
        var textureManager = Minecraft.getInstance().getTextureManager();
        for (var entry : prepared.textures().entrySet()) {
            ResourceLocation id = entry.getKey();
            // A normal resource-pack PNG can override an embedded palette without editing the model.
            if (manager.getResource(id).isPresent()) {
                textureManager.release(id);
                continue;
            }
            try {
                NativeImage image = NativeImage.read(new ByteArrayInputStream(entry.getValue()));
                textureManager.register(id, new DynamicTexture(image));
            } catch (IOException error) {
                throw new UncheckedIOException("Unable to decode embedded Modern texture " + id, error);
            }
        }
        models = prepared.models();
        playerParts = prepared.playerParts();
        revision++;
        HowlingWerewolf.LOGGER.info("Loaded 3 Modern form models, the player overlay, and 9 skin sets (27 embedded textures)");
    }

    private static void validatePng(byte[] png) throws IOException {
        if (png.length < 33 || png.length > 8 * 1024 * 1024 || ByteBuffer.wrap(png).getLong() != 0x89504E470D0A1A0AL) {
            throw new IOException("Invalid embedded PNG");
        }
        int width = ByteBuffer.wrap(png, 16, 4).getInt();
        int height = ByteBuffer.wrap(png, 20, 4).getInt();
        if (width < 1 || height < 1 || width > 2048 || height > 2048) throw new IOException("Embedded PNG is too large");
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(png))) {
            if (image.getWidth() != width || image.getHeight() != height) throw new IOException("PNG dimensions disagree");
        }
    }
}
