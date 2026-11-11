package com.howlingwerewolf.client;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.capability.WerewolfApi;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/** Native armor passes draw joint segments attached to the imported skeleton. */
final class ModernEquipmentLayer extends RenderLayer<AbstractClientPlayer, ModernPlayerModel<AbstractClientPlayer>> {
    private final HumanoidModel<AbstractClientPlayer> carrier;
    // The model type stays HumanoidModel so javac emits no casting bridge: armor mods may pass their own model here.
    private final HumanoidArmorLayer<AbstractClientPlayer, HumanoidModel<AbstractClientPlayer>, HumanoidModel<AbstractClientPlayer>> armor;

    ModernEquipmentLayer(ModernPlayerRenderer parent, EntityRendererProvider.Context context) {
        super(parent);
        carrier = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER));
        RenderLayerParent<AbstractClientPlayer, HumanoidModel<AbstractClientPlayer>> bridge = new RenderLayerParent<>() {
            @Override public HumanoidModel<AbstractClientPlayer> getModel() { return carrier; }
            @Override public ResourceLocation getTextureLocation(AbstractClientPlayer player) { return parent.getTextureLocation(player); }
        };
        armor = new HumanoidArmorLayer<>(bridge,
                new RigArmorModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR), parent.getModel(), true),
                new RigArmorModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR), parent.getModel(), false),
                context.getModelManager()) {
            @Override protected void setPartVisibility(HumanoidModel<AbstractClientPlayer> model, EquipmentSlot slot) {
                super.setPartVisibility(model, slot);
                if (model instanceof RigArmorModel rig) rig.slot = slot;
            }
        };
    }

    static boolean enabled(AbstractClientPlayer player) {
        return HWConfig.SHOW_WEREWOLF_EQUIPMENT.get() && (WerewolfSkinRenderContext.isPreview(player)
                || WerewolfApi.get(player).map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT) && data.showsEquipment()).orElse(false));
    }

    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                 float limb, float speed, float partial, float age, float yaw, float pitch) {
        if (!enabled(player)) return;
        getParentModel().copyPropertiesTo(carrier);
        armor.render(pose, buffers, light, player, limb, speed, partial, age, yaw, pitch);
    }

    static float[] attachmentBounds(ModelPart bone) {
        PoseStack pose = new PoseStack();
        bone.translateAndRotate(pose);
        Matrix4f inverse = new Matrix4f(pose.last().pose()).invert();
        float[] bounds = null;
        float largest = -1;
        final List<float[]> candidates = new ArrayList<>(), upright = new ArrayList<>();
        bone.visit(new PoseStack(), (transform, path, index, cube) -> {
            if (!path.startsWith("/cube_") || path.indexOf('/', 1) >= 0) return;
            ModelPart part = bone.getChild(path.substring(1));
            boolean straight = part.xRot == 0 && part.yRot == 0 && part.zRot == 0;
            float[] box = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                    Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
            for (var polygon : cube.polygons) for (var vertex : polygon.vertices) {
                Vector3f point = transform.pose().transformPosition(vertex.pos.x / 16, vertex.pos.y / 16, vertex.pos.z / 16, new Vector3f());
                inverse.transformPosition(point).mul(16);
                box[0] = Math.min(box[0], point.x); box[3] = Math.max(box[3], point.x);
                box[1] = Math.min(box[1], point.y); box[4] = Math.max(box[4], point.y);
                box[2] = Math.min(box[2], point.z); box[5] = Math.max(box[5], point.z);
            }
            candidates.add(box);
            if (straight) upright.add(box);
        });
        // Fur tufts are tilted blocks and cards; the torso or limb itself is the largest unrotated cube.
        for (float[] box : upright.isEmpty() ? candidates : upright) {
            float volume = (box[3] - box[0]) * (box[4] - box[1]) * (box[5] - box[2]);
            if (volume > largest) { bounds = box; largest = volume; }
        }
        if (bounds == null) throw new IllegalArgumentException("Armor anchor has no geometry");
        return bounds;
    }


    private static final class RigArmorModel extends HumanoidModel<AbstractClientPlayer> {
        private final ModernPlayerModel<AbstractClientPlayer> rig;
        private final List<Piece> pieces = new ArrayList<>();
        /** Room left around the bone: the outer layer sits outside the inner one, as vanilla inflates it more. */
        private final float margin;
        private EquipmentSlot slot;

        RigArmorModel(ModelPart root, ModernPlayerModel<AbstractClientPlayer> rig, boolean inner) {
            super(root);
            this.rig = rig;
            this.margin = inner ? 0.45F : 0.9F;
            // Cuts follow the vanilla layer layout along a limb box: leggings cover its upper three
            // quarters (thigh, then shin), boots its lower half (ankle, then paw), sleeves split at the elbow,
            // and the chestplate covers the chest and the belly above the leggings' belt.
            if (inner) {
                add(EquipmentSlot.LEGS, "body_lower_bone", body);
                split(EquipmentSlot.LEGS, "right_leg_upper", "right_leg_lower", rightLeg, 0, 0.375F, 0.75F);
                split(EquipmentSlot.LEGS, "left_leg_upper", "left_leg_lower", leftLeg, 0, 0.375F, 0.75F);
            } else {
                add(EquipmentSlot.HEAD, "head", head);
                split(EquipmentSlot.CHEST, "ribs", "body_lower_bone", body, 0, 0.5F, 1);
                split(EquipmentSlot.CHEST, "rightArmRotation", "right_forearn", rightArm, 0, 0.5F, 1);
                split(EquipmentSlot.CHEST, "leftArmRotation", "left_forearn", leftArm, 0, 0.5F, 1);
                shoulder("rightArmRotation", rightArm);
                shoulder("leftArmRotation", leftArm);
                split(EquipmentSlot.FEET, "right_foot_upper", "right_foot_lower", rightLeg, 0.5F, 0.75F, 1);
                split(EquipmentSlot.FEET, "left_foot_upper", "left_foot_lower", leftLeg, 0.5F, 0.75F, 1);
            }
        }

        /** Cuts a vanilla limb at fractions of its height and fits each segment to one rig bone. */
        private void split(EquipmentSlot slot, String upper, String lower, ModelPart source, float top, float joint, float bottom) {
            source.setInitialPose(PartPose.ZERO);
            source.resetPose();
            float[] bounds = BlockbenchModel.bounds(source);
            float height = bounds[4] - bounds[1];
            add(slot, upper, clip(source, bounds[1] + height * top, bounds[1] + height * joint));
            add(slot, lower, clip(source, bounds[1] + height * joint, bounds[1] + height * bottom));
        }

        /**
         * Caps the shoulder fur: the sleeve's top is laid over the upper arm's tilted shoulder block in that
         * block's own frame, so the fur no longer juts out above the sleeve and the plate swings with the arm.
         */
        private void shoulder(String bone, ModelPart sleeve) {
            ModelPart block = null;
            float largest = 0;
            for (var entry : rig.rig.bone(bone).children.entrySet()) {
                ModelPart part = entry.getValue();
                if (!entry.getKey().startsWith("cube_") || part.cubes.isEmpty()
                        || part.xRot == 0 && part.yRot == 0 && part.zRot == 0) continue;
                ModelPart.Cube cube = part.cubes.get(0);
                float volume = (cube.maxX - cube.minX) * (cube.maxY - cube.minY) * (cube.maxZ - cube.minZ);
                if (volume > largest) { block = part; largest = volume; }
            }
            if (block == null) return;
            ModelPart.Cube cube = block.cubes.get(0);
            float[] bounds = BlockbenchModel.bounds(sleeve);
            ModelPart top = clip(sleeve, bounds[1], bounds[1] + (bounds[4] - bounds[1]) * 0.35F);
            pieces.add(new Piece(EquipmentSlot.CHEST, bone, block, top, BlockbenchModel.bounds(top),
                    new float[] {cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ}));
        }

    /** Split a vanilla sleeve/leg at the joint, retaining its original UVs and armor trim mapping. */
        static ModelPart clip(ModelPart source, float minimum, float maximum) {
        List<ModelPart.Cube> cubes = new ArrayList<>();
        source.visit(new PoseStack(), (pose, path, index, original) -> {
            if (original.maxY < minimum || original.minY > maximum) return;
            var faces = java.util.EnumSet.noneOf(Direction.class);
            for (int face = 0; face < original.polygons.length; face++) faces.add(Direction.values()[face]);
            ModelPart.Cube copy = new ModelPart.Cube(0, 0, original.minX, minimum, original.minZ,
                    original.maxX - original.minX, maximum - minimum, original.maxZ - original.minZ,
                    0, 0, 0, false, 64, 64, faces);
            for (int face = 0; face < original.polygons.length; face++) {
                var polygon = original.polygons[face];
                ModelPart.Vertex[] vertices = new ModelPart.Vertex[4];
                for (int vertex = 0; vertex < 4; vertex++) {
                    var point = polygon.vertices[vertex];
                    float y = net.minecraft.util.Mth.clamp(point.pos.y, minimum, maximum);
                    float u = point.u;
                    float v = point.v;
                    for (var other : polygon.vertices) {
                        if (Math.abs(other.pos.x - point.pos.x) < 0.0001F && Math.abs(other.pos.z - point.pos.z) < 0.0001F
                                && Math.abs(other.pos.y - point.pos.y) > 0.0001F) {
                            float fraction = (y - point.pos.y) / (other.pos.y - point.pos.y);
                            u += fraction * (other.u - point.u);
                            v += fraction * (other.v - point.v);
                            break;
                        }
                    }
                    vertices[vertex] = new ModelPart.Vertex(point.pos.x, y, point.pos.z, u, v);
                }
                System.arraycopy(vertices, 0, copy.polygons[face].vertices, 0, vertices.length);
                copy.polygons[face].normal.set(polygon.normal);
            }
            cubes.add(copy);
        });
        return new ModelPart(cubes, java.util.Map.of());
    }


        private void add(EquipmentSlot slot, String bone, ModelPart part) {
            part.setInitialPose(PartPose.ZERO);
            part.resetPose();
            pieces.add(new Piece(slot, bone, null, part, BlockbenchModel.bounds(part), attachmentBounds(rig.rig.bone(bone))));
        }

        @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
            for (Piece piece : pieces) {
                if (piece.slot() != slot) continue;
                pose.pushPose();
                rig.rig.transform(piece.bone(), pose);
                if (piece.block() != null) piece.block().translateAndRotate(pose);
                float[] size = new float[3];
                for (int axis = 0; axis < 3; axis++) size[axis] = (piece.target()[axis + 3] - piece.target()[axis] + margin)
                        / Math.max(0.01F, piece.source()[axis + 3] - piece.source()[axis]);
                pose.translate((piece.target()[0] - margin / 2 - piece.source()[0] * size[0]) / 16,
                        (piece.target()[1] - margin / 2 - piece.source()[1] * size[1]) / 16,
                        (piece.target()[2] - margin / 2 - piece.source()[2] * size[2]) / 16);
                pose.scale(size[0], size[1], size[2]);
                BlockbenchModel.render(piece.part(), pose, buffer, light, overlay, red, green, blue, alpha);
                pose.popPose();
            }
        }

        /** {@code block} is set when the piece follows one tilted block of the bone rather than the bone itself. */
        private record Piece(EquipmentSlot slot, String bone, @org.jetbrains.annotations.Nullable ModelPart block,
                             ModelPart part, float[] source, float[] target) {}
    }
}
