package com.howlingwerewolf.client;

import com.howlingwerewolf.HWConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Armor follows the imported bones instead of drawing a second Classic-shaped body. */
final class ModernEquipmentLayer extends RenderLayer<AvatarRenderState, ModernPlayerModel<AvatarRenderState>> {
    private final EquipmentLayerRenderer equipment;
    private final List<Piece> pieces = new ArrayList<>();

    ModernEquipmentLayer(ModernPlayerRenderer parent, EntityRendererProvider.Context context) {
        super(parent);
        equipment = context.getEquipmentRenderer();
        ArmorModelSet<HumanoidModel<AvatarRenderState>> models = ArmorModelSet.bake(
                ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new);
        add(EquipmentSlot.HEAD, "head", models.head().head);
        add(EquipmentSlot.CHEST, "ribs", models.chest().body);
        split(EquipmentSlot.CHEST, "rightArmRotation", "right_forearn", models.chest().rightArm);
        split(EquipmentSlot.CHEST, "leftArmRotation", "left_forearn", models.chest().leftArm);
        add(EquipmentSlot.LEGS, "body_lower_bone", models.legs().body);
        split(EquipmentSlot.LEGS, "right_leg_upper", "right_leg_lower", models.legs().rightLeg);
        split(EquipmentSlot.LEGS, "left_leg_upper", "left_leg_lower", models.legs().leftLeg);
        split(EquipmentSlot.FEET, "right_foot_upper", "right_foot_lower", models.feet().rightLeg);
        split(EquipmentSlot.FEET, "left_foot_upper", "left_foot_lower", models.feet().leftLeg);
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
                    var point = polygon.vertices()[vertex];
                    float y = net.minecraft.util.Mth.clamp(point.y(), minimum, maximum);
                    float u = point.u();
                    float v = point.v();
                    for (var other : polygon.vertices()) {
                        if (Math.abs(other.x() - point.x()) < 0.0001F && Math.abs(other.z() - point.z()) < 0.0001F
                                && Math.abs(other.y() - point.y()) > 0.0001F) {
                            float fraction = (y - point.y()) / (other.y() - point.y());
                            u += fraction * (other.u() - point.u());
                            v += fraction * (other.v() - point.v());
                            break;
                        }
                    }
                    vertices[vertex] = new ModelPart.Vertex(point.x(), y, point.z(), u, v);
                }
                copy.polygons[face] = new ModelPart.Polygon(vertices, polygon.normal());
            }
            cubes.add(copy);
        });
        return new ModelPart(cubes, java.util.Map.of());
    }

    private void add(EquipmentSlot slot, String bone, ModelPart part) {
        part.setInitialPose(PartPose.ZERO);
        part.resetPose();
        float[] source = BlockbenchModel.bounds(part);
        float[] target = attachmentBounds(getParentModel().rig.bone(bone));
        pieces.add(new Piece(slot, bone, new ArmorPieceModel(part), source, target));
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        if (!WerewolfRenderData.get(state).armoredInstinct() || !HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) return;
        for (Piece piece : pieces) {
            ItemStack stack = switch (piece.slot()) {
                case HEAD -> state.headEquipment;
                case CHEST -> state.chestEquipment;
                case LEGS -> state.legsEquipment;
                default -> state.feetEquipment;
            };
            if (!HumanoidArmorLayer.shouldRender(stack, piece.slot())) continue;
            var equippable = stack.get(DataComponents.EQUIPPABLE);
            pose.pushPose();
            getParentModel().rig.transform(piece.bone(), pose);
            float[] size = new float[3];
            for (int axis = 0; axis < 3; axis++) {
                size[axis] = (piece.target()[axis + 3] - piece.target()[axis] + 0.45F)
                        / Math.max(0.01F, piece.source()[axis + 3] - piece.source()[axis]);
            }
            pose.translate((piece.target()[0] - 0.225F - piece.source()[0] * size[0]) / 16,
                    (piece.target()[1] - 0.225F - piece.source()[1] * size[1]) / 16,
                    (piece.target()[2] - 0.225F - piece.source()[2] * size[2]) / 16);
            pose.scale(size[0], size[1], size[2]);
            equipment.renderLayers(piece.slot() == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                            : EquipmentClientInfo.LayerType.HUMANOID, equippable.assetId().orElseThrow(),
                    piece.model(), state, stack, pose, collector, light, state.outlineColor);
            pose.popPose();
        }
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
            for (var polygon : cube.polygons) for (var vertex : polygon.vertices()) {
                Vector3f point = transform.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
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

    private static final class ArmorPieceModel extends EntityModel<AvatarRenderState> {
        ArmorPieceModel(ModelPart root) { super(root); }
        @Override public void setupAnim(AvatarRenderState state) { resetPose(); }
    }

    private record Piece(EquipmentSlot slot, String bone, ArmorPieceModel model, float[] source, float[] target) {}
}
