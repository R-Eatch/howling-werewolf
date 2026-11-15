package com.howlingwerewolf.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reads Blockbench 5 modded-entity projects without generating Java geometry. */
final class BlockbenchModel {
    private final JsonObject document;
    private final Map<String, JsonObject> groups = new HashMap<>();
    private final Map<String, JsonObject> elements = new HashMap<>();
    private final int textureWidth;
    private final int textureHeight;

    BlockbenchModel(JsonObject document) {
        this.document = document;
        JsonObject meta = document.getAsJsonObject("meta");
        if (!"5.0".equals(GsonHelper.getAsString(meta, "format_version"))
                || !"modded_entity".equals(GsonHelper.getAsString(meta, "model_format"))
                || !GsonHelper.getAsBoolean(meta, "box_uv", false)
                || !GsonHelper.getAsBoolean(document, "modded_entity_flip_y", true)) {
            throw new IllegalArgumentException("Expected a Blockbench 5.0 modded_entity project with box UV and flip Y");
        }
        JsonObject resolution = document.getAsJsonObject("resolution");
        textureWidth = GsonHelper.getAsInt(resolution, "width");
        textureHeight = GsonHelper.getAsInt(resolution, "height");
        if (textureWidth < 1 || textureWidth > 2048 || textureHeight < 1 || textureHeight > 2048) {
            throw new IllegalArgumentException("Invalid model UV resolution");
        }
        index(document.getAsJsonArray("groups"), groups, 256);
        index(document.getAsJsonArray("elements"), elements, 1024);
        for (JsonObject element : elements.values()) {
            if (!"cube".equals(GsonHelper.getAsString(element, "type", "cube"))
                    || !GsonHelper.getAsBoolean(element, "box_uv", true)) {
                throw new IllegalArgumentException("Only box-UV cubes are supported");
            }
        }
    }

    /** {@code flats} lists the zero-thickness fur cards each bone carries directly. */
    record Baked(ModelPart root, Map<String, ModelPart> bones, Map<String, List<ModelPart>> paths,
                 Map<String, List<ModelPart>> flats, float scale) {
        ModelPart bone(String name) {
            ModelPart part = bones.get(name);
            if (part == null) throw new IllegalArgumentException("Missing Blockbench bone: " + name);
            return part;
        }
        void transform(String name, PoseStack pose) {
            for (ModelPart part : paths.get(name)) part.translateAndRotate(pose);
        }
    }

    Baked bake(float height) {
        Map<String, ModelPart> bones = new LinkedHashMap<>();
        Map<String, List<ModelPart>> paths = new HashMap<>();
        Map<String, List<ModelPart>> flats = new HashMap<>();
        ModelPart root = new ModelPart(List.of(), children(document.getAsJsonArray("outliner"),
                new float[3], bones, paths, flats, "", new HashSet<>(), 0));
        float[] bounds = bounds(root);
        float scale = height * 16.0F / (bounds[4] - bounds[1]);
        if (!Float.isFinite(scale) || scale <= 0.0F) throw new IllegalArgumentException("Empty model bounds");
        PartPose rootPose = PartPose.offset(0.0F, 24.0F - bounds[4] * scale, 0.0F);
        root.setInitialPose(rootPose);
        root.loadPose(rootPose);
        root.xScale = root.yScale = root.zScale = scale;
        for (String name : paths.keySet()) {
            List<ModelPart> path = new ArrayList<>();
            path.add(root);
            path.addAll(paths.get(name));
            paths.put(name, List.copyOf(path));
        }
        return new Baked(root, Map.copyOf(bones), Map.copyOf(paths), Map.copyOf(flats), scale);
    }

    private Map<String, ModelPart> children(JsonArray outliner, float[] parentOrigin,
                                           Map<String, ModelPart> bones, Map<String, List<ModelPart>> paths,
                                           Map<String, List<ModelPart>> flats, String owner,
                                           Set<String> visited, int depth) {
        if (depth > 64) throw new IllegalArgumentException("Model hierarchy is too deep");
        Map<String, ModelPart> result = new LinkedHashMap<>();
        for (JsonElement entry : outliner) {
            String uuid = entry.isJsonPrimitive() ? entry.getAsString() : entry.getAsJsonObject().get("uuid").getAsString();
            if (!visited.add(uuid)) throw new IllegalArgumentException("Repeated model node: " + uuid);
            if (entry.isJsonPrimitive()) {
                JsonObject cube = elements.get(uuid);
                if (cube == null) throw new IllegalArgumentException("Missing cube: " + uuid);
                if (!GsonHelper.getAsBoolean(cube, "export", true)) continue;
                float[] origin = vector(cube, "origin");
                ModelPart part = new ModelPart(List.of(cube(cube, origin)), Map.of());
                setPose(part, origin, parentOrigin, vector(cube, "rotation"));
                result.put("cube_" + uuid, part);
                if (vector(cube, "to")[1] == vector(cube, "from")[1]) flats.computeIfAbsent(owner, ignored -> new ArrayList<>()).add(part);
            } else {
                JsonObject group = groups.get(uuid);
                if (group == null) throw new IllegalArgumentException("Missing group: " + uuid);
                if (!GsonHelper.getAsBoolean(group, "export", true)) continue;
                float[] origin = vector(group, "origin");
                Set<String> before = new HashSet<>(paths.keySet());
                String name = GsonHelper.getAsString(group, "name");
                ModelPart part = new ModelPart(List.of(), children(entry.getAsJsonObject().getAsJsonArray("children"),
                        origin, bones, paths, flats, name, visited, depth + 1));
                setPose(part, origin, parentOrigin, vector(group, "rotation"));
                if (bones.put(name, part) != null) throw new IllegalArgumentException("Duplicate bone name: " + name);
                for (String child : paths.keySet()) {
                    if (!before.contains(child)) {
                        List<ModelPart> path = new ArrayList<>();
                        path.add(part);
                        path.addAll(paths.get(child));
                        paths.put(child, path);
                    }
                }
                paths.put(name, List.of(part));
                result.put(name, part);
            }
        }
        return result;
    }

    private ModelPart.Cube cube(JsonObject json, float[] origin) {
        float[] from = vector(json, "from");
        float[] to = vector(json, "to");
        float[] size = {to[0] - from[0], to[1] - from[1], to[2] - from[2]};
        for (float value : size) if (value < 0) throw new IllegalArgumentException("Negative cube size");
        JsonObject faces = json.getAsJsonObject("faces");
        float[] north = faceUv(faces, "north");
        float[] east = faceUv(faces, "east");
        float u = Math.min(east[0], east[2]);
        float v = Math.min(north[1], north[3]) - Math.abs(east[2] - east[0]);
        if (json.has("uv_offset")) {
            u = json.getAsJsonArray("uv_offset").get(0).getAsFloat();
            v = json.getAsJsonArray("uv_offset").get(1).getAsFloat();
        }
        boolean mirror = GsonHelper.getAsBoolean(json, "mirror_uv", false);
        float inflate = number(json, "inflate", 0);
        var directions = EnumSet.allOf(Direction.class);
        ModelPart.Cube cube = new EditedModelCube(origin[0] - to[0], origin[1] - to[1], from[2] - origin[2],
                size[0], size[1], size[2], inflate, mirror, textureWidth, textureHeight, directions);
        // Blockbench rounds box-UV sizes separately from the actual fractional geometry.
        ModelPart.Cube uv = new ModelPart.Cube(0, 0, 0, 0, 0,
                Math.abs(north[2] - north[0]), Math.abs(north[3] - north[1]), Math.abs(east[2] - east[0]),
                0, 0, 0, mirror, textureWidth, textureHeight, directions);
        for (int index = 0; index < cube.polygons.length; index++) {
            ModelPart.Vertex[] points = cube.polygons[index].vertices.clone();
            ModelPart.Vertex[] tex = uv.polygons[index].vertices;
            for (int vertex = 0; vertex < points.length; vertex++) {
                points[vertex] = points[vertex].remap(tex[vertex].u + u / textureWidth, tex[vertex].v + v / textureHeight);
            }
            System.arraycopy(points, 0, cube.polygons[index].vertices, 0, points.length);
        }
        return cube;
    }

    static float[] bounds(ModelPart root) {
        float[] result = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            for (var polygon : cube.polygons) for (var vertex : polygon.vertices) {
                Vector3f point = pose.pose().transformPosition(vertex.pos.x / 16, vertex.pos.y / 16, vertex.pos.z / 16, new Vector3f()).mul(16);
                result[0] = Math.min(result[0], point.x); result[3] = Math.max(result[3], point.x);
                result[1] = Math.min(result[1], point.y); result[4] = Math.max(result[4], point.y);
                result[2] = Math.min(result[2], point.z); result[5] = Math.max(result[5], point.z);
            }
        });
        return result;
    }

    private static void setPose(ModelPart part, float[] origin, float[] parent, float[] rotation) {
        // Same coordinate conversion as Blockbench's official Java modded-entity exporter.
        PartPose pose = PartPose.offsetAndRotation(parent[0] - origin[0], parent[1] - origin[1], origin[2] - parent[2],
                -rotation[0] * Mth.DEG_TO_RAD, -rotation[1] * Mth.DEG_TO_RAD, rotation[2] * Mth.DEG_TO_RAD);
        part.setInitialPose(pose);
        part.loadPose(pose);
    }

    private static void index(JsonArray entries, Map<String, JsonObject> target, int limit) {
        if (entries.size() > limit) throw new IllegalArgumentException("Too many model nodes");
        for (JsonElement entry : entries) {
            JsonObject object = entry.getAsJsonObject();
            if (target.put(GsonHelper.getAsString(object, "uuid"), object) != null) {
                throw new IllegalArgumentException("Duplicate model UUID");
            }
        }
    }

    private static float[] vector(JsonObject object, String key) {
        float[] result = new float[3];
        if (!object.has(key)) return result;
        JsonArray array = object.getAsJsonArray(key);
        if (array.size() != 3) throw new IllegalArgumentException("Invalid vector: " + key);
        for (int index = 0; index < 3; index++) {
            result[index] = array.get(index).getAsFloat();
            if (!Float.isFinite(result[index]) || Math.abs(result[index]) > 4096) throw new IllegalArgumentException("Invalid vector: " + key);
        }
        return result;
    }

    private static float number(JsonObject object, String key, float fallback) {
        float value = GsonHelper.getAsFloat(object, key, fallback);
        if (!Float.isFinite(value) || Math.abs(value) > 4096) throw new IllegalArgumentException("Invalid number: " + key);
        return value;
    }

    private static float[] faceUv(JsonObject faces, String name) {
        JsonArray array = faces.getAsJsonObject(name).getAsJsonArray("uv");
        float[] values = new float[4];
        for (int index = 0; index < 4; index++) {
            values[index] = array.get(index).getAsFloat();
            if (!Float.isFinite(values[index]) || Math.abs(values[index]) > 4096) throw new IllegalArgumentException("Invalid face UV");
        }
        return values;
    }
}
