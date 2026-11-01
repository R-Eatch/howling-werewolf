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
    private final HumanoidArmorLayer<AbstractClientPlayer, HumanoidModel<AbstractClientPlayer>, RigArmorModel> armor;

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
            @Override protected void setPartVisibility(RigArmorModel model, EquipmentSlot slot) {
                super.setPartVisibility(model, slot);
                model.slot = slot;
            }
        };
    }

    static boolean enabled(AbstractClientPlayer player) {
        return HWConfig.SHOW_WEREWOLF_EQUIPMENT.get() && (WerewolfSkinRenderContext.isPreview(player)
                || WerewolfApi.get(player).map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT)).orElse(false));
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
        final List<float[]> candidates = new ArrayList<>();
        bone.visit(new PoseStack(), (transform, path, index, cube) -> {
            if (!path.startsWith("/cube_") || path.indexOf('/', 1) >= 0) return;
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
        });
        for (float[] box : candidates) {
            float volume = (box[3] - box[0]) * (box[4] - box[1]) * (box[5] - box[2]);
            if (volume > largest) { bounds = box; largest = volume; }
        }
        if (bounds == null) throw new IllegalArgumentException("Armor anchor has no geometry");
        return bounds;
    }


    private static final class RigArmorModel extends HumanoidModel<AbstractClientPlayer> {
        private final ModernPlayerModel<AbstractClientPlayer> rig;
        private final List<Piece> pieces = new ArrayList<>();
        private EquipmentSlot slot;

        RigArmorModel(ModelPart root, ModernPlayerModel<AbstractClientPlayer> rig, boolean inner) {
            super(root);
            this.rig = rig;
            if (inner) {
                add(EquipmentSlot.LEGS, "body_lower_bone", body);
                split(EquipmentSlot.LEGS, "right_leg_upper", "right_leg_lower", rightLeg);
                split(EquipmentSlot.LEGS, "left_leg_upper", "left_leg_lower", leftLeg);
            } else {
                add(EquipmentSlot.HEAD, "head", head);
                add(EquipmentSlot.CHEST, "ribs", body);
                split(EquipmentSlot.CHEST, "rightArmRotation", "right_forearn", rightArm);
                split(EquipmentSlot.CHEST, "leftArmRotation", "left_forearn", leftArm);
                split(EquipmentSlot.FEET, "right_foot_upper", "right_foot_lower", rightLeg);
                split(EquipmentSlot.FEET, "left_foot_upper", "left_foot_lower", leftLeg);
            }
        }

        private void split(EquipmentSlot slot, String upper, String lower, ModelPart source) {
        source.setInitialPose(PartPose.ZERO);
        source.resetPose();
        float[] bounds = BlockbenchModel.bounds(source);
        float middle = (bounds[1] + bounds[4]) / 2;
        add(slot, upper, clip(source, bounds[1], middle));
        add(slot, lower, clip(source, middle, bounds[4]));
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
            pieces.add(new Piece(slot, bone, part, BlockbenchModel.bounds(part), attachmentBounds(rig.rig.bone(bone))));
        }

        @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
            for (Piece piece : pieces) {
                if (piece.slot() != slot) continue;
                pose.pushPose();
                rig.rig.transform(piece.bone(), pose);
                float[] size = new float[3];
                for (int axis = 0; axis < 3; axis++) size[axis] = (piece.target()[axis + 3] - piece.target()[axis] + 0.45F)
                        / Math.max(0.01F, piece.source()[axis + 3] - piece.source()[axis]);
                pose.translate((piece.target()[0] - 0.225F - piece.source()[0] * size[0]) / 16,
                        (piece.target()[1] - 0.225F - piece.source()[1] * size[1]) / 16,
                        (piece.target()[2] - 0.225F - piece.source()[2] * size[2]) / 16);
                pose.scale(size[0], size[1], size[2]);
                piece.part().render(pose, buffer, light, overlay, color);
                pose.popPose();
            }
        }

        private record Piece(EquipmentSlot slot, String bone, ModelPart part, float[] source, float[] target) {}
    }
}
