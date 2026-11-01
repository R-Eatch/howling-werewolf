package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Retargets the established actions onto the imported skeleton, preserving its authored rest pose. */
final class ModernPlayerModel<T extends LivingEntity> extends EntityModel<T> implements ArmedModel, HeadedModel {
    private final WerewolfForm form;
    private final ModelPart root;
    private final ModelPart driverRoot;
    private ModernRenderState state;
    final BlockbenchModel.Baked rig;
    private final EntityModel<T> driver;
    private final HumanoidModel<T> nativeHands = new HumanoidModel<>(
            LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 64).bakeRoot());
    private final List<Binding> bindings = new ArrayList<>();
    private final Vector3f rightPawContact;
    private final Vector3f leftPawContact;
    private final Vector3f rightFootContact;
    private final Vector3f leftFootContact;
    private final Vector3f rightGrip;
    private final Vector3f leftGrip;

    ModernPlayerModel(WerewolfForm form) {
        this(form, ModernModelResources.model(form).bake(height(form)));
    }

    ModernPlayerModel(WerewolfForm form, BlockbenchModel.Baked rig) {
        super();
        this.root = rig.root();
        this.form = form;
        this.rig = rig;
        if (form == WerewolfForm.BEAST) {
            driverRoot = BeastPlayerModel.createBodyLayer().bakeRoot();
            driver = new BeastPlayerModel<>(driverRoot);
            bind("torso", "body"); bind("torso/head", "head"); bind("torso/head/jaw", "jaw");
            bind("torso/right_upper_arm", "rightArm"); bind("torso/left_upper_arm", "leftArm");
            bind("torso/right_upper_arm/right_forearm", "right_forearn");
            bind("torso/left_upper_arm/left_forearm", "left_forearn");
            bind("right_thigh", "rightLeg"); bind("left_thigh", "leftLeg");
            bind("right_thigh/right_shin", "right_leg_lower"); bind("left_thigh/left_shin", "left_leg_lower");
            bind("tail_base", "tail"); bind("tail_base/tail_mid", "tail_main12");
            bind("tail_base/tail_mid/tail_tip", "tail_12");
        } else if (form == WerewolfForm.QUADRUPED) {
            driverRoot = QuadrupedWerewolfModel.createBodyLayer().bakeRoot();
            driver = new QuadrupedWerewolfModel<>(driverRoot);
            bind("head", "head"); bind("head/real_head", "muzzle");
            bind("right_hind_leg", "rightLeg"); bind("left_hind_leg", "leftLeg");
            bind("right_front_leg", "front_RightLeg"); bind("left_front_leg", "front_LeftLeg");
            bind("tail", "tail"); bind("tail/real_tail", "tail_main12");
        } else {
            driverRoot = WerewolfPlayerModel.createBodyLayer().bakeRoot();
            driver = new WerewolfPlayerModel<>(driverRoot);
            bind("torso", "body"); bind("torso/chest", "body_upper"); bind("torso/chest/head", "head");
            bind("torso/chest/head/muzzle", "jaw");
            bind("torso/chest/right_arm", "rightArm"); bind("torso/chest/left_arm", "leftArm");
            bind("right_leg", "rightLeg"); bind("left_leg", "leftLeg");
            bind("torso/tail_base", "tail"); bind("torso/tail_base/tail_tip", "tail_main12");
        }
        rightPawContact = form == WerewolfForm.BEAST ? contact("right_paw") : null;
        leftPawContact = form == WerewolfForm.BEAST ? contact("left_paw") : null;
        rightFootContact = form == WerewolfForm.BEAST ? contact("right_foot_lower") : null;
        leftFootContact = form == WerewolfForm.BEAST ? contact("left_foot_lower") : null;
        rightGrip = form == WerewolfForm.QUADRUPED ? null : palm("right_paw");
        leftGrip = form == WerewolfForm.QUADRUPED ? null : palm("left_paw");
    }

    static float height(WerewolfForm form) {
        return switch (form) {
            case BEAST -> 2.95F;
            case QUADRUPED -> 0.85F;
            default -> 1.95F;
        };
    }

    private void bind(String source, String target) {
        ModelPart part = driverRoot;
        for (String segment : source.split("/")) part = part.getChild(segment);
        bindings.add(new Binding(part, rig.bone(target)));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSpeed, float age, float yaw, float pitch) {
        state = ModernRenderState.sample(entity, limbSwing, limbSpeed, age, pitch, attackTime);
        resetPose();
        driver.attackTime = attackTime;
        driver.riding = riding;
        driver.young = young;
        driver.prepareMobModel(entity, limbSwing, limbSpeed, state.partialTick);
        driver.setupAnim(entity, limbSwing, limbSpeed, age, yaw, pitch);
        nativeHands.attackTime = attackTime;
        nativeHands.riding = riding;
        nativeHands.young = young;
        nativeHands.crouching = entity.isCrouching();
        ModernArmPoses.apply(nativeHands, entity);
        nativeHands.prepareMobModel(entity, limbSwing, limbSpeed, state.partialTick);
        nativeHands.setupAnim(entity, limbSwing, limbSpeed, age, yaw, pitch);
        for (Binding binding : bindings) {
            ModelPart source = binding.source();
            ModelPart target = binding.target();
            var rest = source.getInitialPose();
            target.xRot += source.xRot - rest.xRot;
            target.yRot += source.yRot - rest.yRot;
            target.zRot += source.zRot - rest.zRot;
            target.x += (source.x - rest.x) / rig.scale();
            target.y += (source.y - rest.y) / rig.scale();
            target.z += (source.z - rest.z) / rig.scale();
        }
        applyNativeHands(state);
        if (form == WerewolfForm.BEAST) {
            if (state.isCrouching && !state.isPassenger && !state.isInWater && !state.isFallFlying) {
                // Lower the head by leaning from the hips. Deeply lowering the hip itself
                // folds this digitigrade rig's long hocks back through the feet.
                root.y += 2.4F;
                rig.bone("waist").xRot += 0.72F;
                rig.bone("head").xRot -= 0.72F;
                runLeg("right_foot_lower", rightFootContact, new String[]{"rightLeg", "right_leg_lower", "right_foot_upper"}, "rightLeg", 0, 0, false, 1);
                runLeg("left_foot_lower", leftFootContact, new String[]{"leftLeg", "left_leg_lower", "left_foot_upper"}, "leftLeg", 0, 0, false, 1);
            }
            applyBeastRun(state, state.beastRunAmount);
        }
        animateTail(state);
    }

    private void applyNativeHands(ModernRenderState state) {
        if (form == WerewolfForm.QUADRUPED || state.leftHandItemStack.isEmpty() && state.rightHandItemStack.isEmpty()) return;
        // AvatarRenderer already extracts Steve's ITEM/BOW/CROSSBOW/SPYGLASS/TRIDENT/SPEAR poses.
        // Keep both arms from the same native pose so aiming and two-handed use stay coordinated.
        for (HumanoidArm arm : HumanoidArm.values()) {
            ModelPart source = (arm == HumanoidArm.RIGHT ? nativeHands.rightArm : nativeHands.leftArm);
            ModelPart target = rig.bone(arm == HumanoidArm.RIGHT ? "rightArm" : "leftArm");
            target.xRot = target.getInitialPose().xRot + source.xRot;
            target.yRot = target.getInitialPose().yRot + source.yRot;
            target.zRot = target.getInitialPose().zRot + source.zRot;
        }
        rig.bone("body").yRot = nativeHands.body.yRot;
        rig.bone("body_upper").yRot = rig.bone("body_upper").getInitialPose().yRot;
    }

    private void animateTail(ModernRenderState state) {
        float phase = state.ageInTicks * 0.085F;
        float movement = Mth.clamp(state.walkAnimationSpeed, 0, 1);
        float strength = state.isCrouching ? 0.55F : form == WerewolfForm.BEAST
                && state.beastRunAmount > 0 ? 0.7F : 1;
        String[] names = {"tail", "tail_main12", "tail_12"};
        float[] amplitudes = {0.09F, 0.13F, 0.18F};
        for (int index = 0; index < names.length; index++) {
            ModelPart tail = rig.bone(names[index]);
            float lag = index * 0.45F;
            tail.yRot = tail.getInitialPose().yRot + strength * (
                    Mth.sin(phase - lag) * amplitudes[index]
                    + Mth.sin(state.ageInTicks * 0.031F - lag) * 0.025F
                    + Mth.sin(state.walkAnimationPos * 0.6F - lag) * movement * 0.045F);
            tail.xRot += Mth.sin(state.ageInTicks * 0.06F - lag) * 0.012F * strength;
        }
    }

    private void applyBeastRun(ModernRenderState state, float amount) {
        if (amount <= 0 || state.isCrouching || state.isInWater || state.isVisuallySwimming || state.isFallFlying
                || state.isAutoSpinAttack || state.isPassenger || state.isUsingItem) return;
        ModelPart body = rig.bone("body");
        ModelPart upper = rig.bone("body_upper");
        ModelPart head = rig.bone("head");
        root.y += 6.5F * amount;
        rig.bone("waist").xRot += 0.78F * amount;
        body.xRot = Mth.lerp(amount, body.xRot, 0.05F);
        upper.xRot = Mth.lerp(amount, upper.xRot, upper.getInitialPose().xRot + 0.07F);
        head.xRot = Mth.lerp(amount, head.xRot, -1.48F + Mth.clamp(state.xRot * Mth.DEG_TO_RAD, -0.5F, 0.5F));
        for (String name : List.of("rightArm", "leftArm", "right_forearn", "left_forearn",
                "rightLeg", "leftLeg", "right_leg_lower", "left_leg_lower")) {
            ModelPart joint = rig.bone(name);
            joint.xRot = Mth.lerp(amount, joint.xRot, joint.getInitialPose().xRot);
        }
        float cycle = state.walkAnimationPos * 0.8F;
        float stride = 0.28F + Math.min(state.walkAnimationSpeed, 1) * 0.12F;
        runLeg("right_paw", rightPawContact, new String[]{"rightArmRotation", "right_forearn"}, "rightArmRotation", cycle, stride, true, amount);
        runLeg("left_paw", leftPawContact, new String[]{"leftArmRotation", "left_forearn"}, "leftArmRotation", cycle + Mth.PI, stride, true, amount);
        runLeg("right_foot_lower", rightFootContact, new String[]{"rightLeg", "right_leg_lower", "right_foot_upper"}, "rightLeg", cycle + Mth.PI, stride, false, amount);
        runLeg("left_foot_lower", leftFootContact, new String[]{"leftLeg", "left_leg_lower", "left_foot_upper"}, "leftLeg", cycle, stride, false, amount);
        rig.bone("jaw").xRot -= 0.08F * amount;
        if (state.attackTime > 0) {
            // A running attack lifts the attacking paw; the other three limbs keep their gait.
            float eased = 1 - (float) Math.pow(1 - state.attackTime, 4);
            float swipe = Mth.sin(eased * Mth.PI) * amount;
            boolean right = state.attackArm == HumanoidArm.RIGHT;
            rig.bone(right ? "rightArmRotation" : "leftArmRotation").xRot -= swipe * 0.9F;
            rig.bone(right ? "right_forearn" : "left_forearn").xRot -= swipe * 0.25F;
        }
    }

    private void runLeg(String effector, Vector3f contact, String[] joints, String anchor,
                        float phase, float stride, boolean front, float amount) {
        Vector3f current = matrix(effector).transformPosition(contact, new Vector3f());
        Vector3f initialDown = matrix(effector).transformDirection(new Vector3f(0, 1, 0));
        float pawPitch = (float) Math.atan2(initialDown.z, initialDown.y) * (1 - amount);
        int jointCount = joints.length;
        Vector3f origin = matrix(anchor).transformPosition(new Vector3f());
        float bottom = pawBottom(effector);
        float desiredBottom = Mth.lerp(amount, bottom, 1.5F - Math.max(0, Mth.sin(phase)) * 0.18F);
        Vector3f target = new Vector3f(current.x, current.y + desiredBottom - bottom,
                origin.z + (front ? 0.12F : 0.08F) + Mth.cos(phase) * stride);
        target.z = Mth.lerp(amount, current.z, target.z);
        // Planar inverse kinematics places the paws on the ground during the stance half-cycle.
        for (int pass = 0; pass < 3; pass++) {
            levelPaw(effector, pawPitch);
            seedLimb(effector, contact, joints, target);
            float[] centers = new float[jointCount];
            for (int index = 0; index < jointCount; index++) centers[index] = rig.bone(joints[index]).xRot;
            for (int iteration = 0; iteration < 16; iteration++) {
                levelPaw(effector, pawPitch);
                Vector3f end = matrix(effector).transformPosition(contact, new Vector3f());
                float errorY = target.y - end.y;
                float errorZ = target.z - end.z;
                if (errorY * errorY + errorZ * errorZ < 0.000001F) break;
                Vector3f[] derivatives = new Vector3f[jointCount];
                for (int index = 0; index < jointCount; index++) {
                    ModelPart joint = rig.bone(joints[index]);
                    float rotation = joint.xRot;
                    float pawRotation = rig.bone(effector).xRot;
                    joint.xRot += 0.002F;
                    levelPaw(effector, pawPitch);
                    derivatives[index] = matrix(effector).transformPosition(contact, new Vector3f()).sub(end).div(0.002F);
                    joint.xRot = rotation;
                    rig.bone(effector).xRot = pawRotation;
                }
                // Damped least squares remains stable when a limb approaches full extension.
                float aa = 0.0005F;
                float ab = 0;
                float bb = 0.0005F;
                for (Vector3f derivative : derivatives) {
                    aa += derivative.y * derivative.y;
                    ab += derivative.y * derivative.z;
                    bb += derivative.z * derivative.z;
                }
                float determinant = aa * bb - ab * ab;
                float correctionY = (bb * errorY - ab * errorZ) / determinant;
                float correctionZ = (aa * errorZ - ab * errorY) / determinant;
                for (int index = 0; index < jointCount; index++) {
                    ModelPart joint = rig.bone(joints[index]);
                    float step = derivatives[index].y * correctionY + derivatives[index].z * correctionZ;
                    joint.xRot = Mth.clamp(joint.xRot + Mth.clamp(step, -0.3F, 0.3F), centers[index] - 1.2F, centers[index] + 1.2F);
                }
            }
            levelPaw(effector, pawPitch);
            target.y += desiredBottom - pawBottom(effector);
        }
    }

    private void seedLimb(String effector, Vector3f contact, String[] joints, Vector3f target) {
        Vector3f first = matrix(joints[0]).transformPosition(new Vector3f());
        Vector3f second = matrix(joints[1]).transformPosition(new Vector3f());
        Vector3f tip = matrix(effector).transformPosition(new Vector3f());
        Vector3f end = matrix(effector).transformPosition(contact, new Vector3f());
        float firstY = second.y - first.y;
        float firstZ = second.z - first.z;
        float secondY = tip.y - second.y;
        float secondZ = tip.z - second.z;
        float goalY = target.y - end.y + tip.y - first.y;
        float goalZ = target.z - end.z + tip.z - first.z;
        float lengthA = Mth.sqrt(firstY * firstY + firstZ * firstZ);
        float lengthB = Mth.sqrt(secondY * secondY + secondZ * secondZ);
        if (lengthA < 0.0001F || lengthB < 0.0001F) return;
        float angleA = (float) Math.atan2(firstZ, firstY);
        float bend = wrap((float) Math.atan2(secondZ, secondY) - angleA);
        float cosine = (goalY * goalY + goalZ * goalZ - lengthA * lengthA - lengthB * lengthB) / (2 * lengthA * lengthB);
        float goalBend = Math.copySign((float) Math.acos(Mth.clamp(cosine, -1, 1)), bend);
        float goalAngle = (float) Math.atan2(goalZ, goalY)
                - (float) Math.atan2(lengthB * Mth.sin(goalBend), lengthA + lengthB * Mth.cos(goalBend));
        ModelPart upper = rig.bone(joints[0]);
        ModelPart lower = rig.bone(joints[1]);
        upper.xRot += wrap(goalAngle - angleA);
        lower.xRot += wrap(goalBend - bend);
    }

    private static float wrap(float angle) {
        return Mth.wrapDegrees(angle * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
    }

    private void levelPaw(String effector, float pitch) {
        ModelPart paw = rig.bone(effector);
        PoseStack pose = new PoseStack();
        var path = rig.paths().get(effector);
        for (int index = 0; index < path.size() - 1; index++) path.get(index).translateAndRotate(pose);
        pose.mulPose(new org.joml.Quaternionf().rotationZYX(paw.zRot, paw.yRot, 0));
        Vector3f y = pose.last().pose().transformDirection(new Vector3f(0, 1, 0));
        Vector3f z = pose.last().pose().transformDirection(new Vector3f(0, 0, 1));
        float sin = Mth.sin(pitch);
        float cos = Mth.cos(pitch);
        float rotation = (float) Math.atan2(-(y.z * cos - y.y * sin), z.z * cos - z.y * sin);
        float downY = y.y * Mth.cos(rotation) + z.y * Mth.sin(rotation);
        float downZ = y.z * Mth.cos(rotation) + z.z * Mth.sin(rotation);
        if (downY * cos + downZ * sin < 0) rotation += Mth.PI;
        paw.xRot = rotation;
    }


    private float pawBottom(String bone) {
        PoseStack pose = new PoseStack();
        var path = rig.paths().get(bone);
        for (int index = 0; index < path.size() - 1; index++) path.get(index).translateAndRotate(pose);
        float[] bottom = {Float.NEGATIVE_INFINITY};
        rig.bone(bone).visit(pose, (transform, name, index, cube) -> {
            for (var polygon : cube.polygons) for (var vertex : polygon.vertices) {
                float y = transform.pose().transformPosition(vertex.pos.x / 16, vertex.pos.y / 16, vertex.pos.z / 16, new Vector3f()).y;
                bottom[0] = Math.max(bottom[0], y);
            }
        });
        return bottom[0];
    }

    private Matrix4f matrix(String bone) {
        PoseStack pose = new PoseStack();
        rig.transform(bone, pose);
        return new Matrix4f(pose.last().pose());
    }

    private Vector3f contact(String bone) {
        ModelPart part = rig.bone(bone);
        PoseStack pose = new PoseStack();
        part.translateAndRotate(pose);
        Matrix4f inverse = new Matrix4f(pose.last().pose()).invert();
        float[] bounds = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        part.visit(new PoseStack(), (transform, path, index, cube) -> {
            for (var polygon : cube.polygons) for (var vertex : polygon.vertices) {
                Vector3f point = transform.pose().transformPosition(vertex.pos.x / 16, vertex.pos.y / 16, vertex.pos.z / 16, new Vector3f());
                inverse.transformPosition(point);
                bounds[0] = Math.min(bounds[0], point.x); bounds[3] = Math.max(bounds[3], point.x);
                bounds[1] = Math.min(bounds[1], point.y); bounds[4] = Math.max(bounds[4], point.y);
                bounds[2] = Math.min(bounds[2], point.z); bounds[5] = Math.max(bounds[5], point.z);
            }
        });
        return new Vector3f((bounds[0] + bounds[3]) / 2, bounds[4], (bounds[2] + bounds[5]) / 2);
    }

    @Override public ModelPart getHead() { return rig.bone("head"); }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack pose) {
        String paw = arm == HumanoidArm.RIGHT ? "right_paw" : "left_paw";
        Matrix4f actual = matrix(paw);
        // One model-space pixel below the palm center, without changing native item scale or aim.
        Vector3f grip = actual.transformPosition(arm == HumanoidArm.RIGHT ? rightGrip : leftGrip, new Vector3f()).add(0, 1.0F / 16, 0);
        boolean nativeOrientation = form != WerewolfForm.BEAST || state.beastRunAmount <= 0
                || state.isUsingItem || state.isCrouching || state.isFallFlying || state.isInWater;
        org.joml.Quaternionf rotation;
        if (nativeOrientation) {
            PoseStack nativePose = new PoseStack();
            nativeHands.translateToHand(arm, nativePose);
            rotation = nativePose.last().pose().getUnnormalizedRotation(new org.joml.Quaternionf()).normalize();
        } else rotation = actual.getUnnormalizedRotation(new org.joml.Quaternionf()).normalize();
        // Import normalization scales the body only. Weapons retain their native size.
        pose.mulPose(new Matrix4f().rotation(rotation).setTranslation(grip));
        // Cancel ItemInHandLayer's shoulder-to-hand offset: this transform already reaches the palm.
        pose.translate(arm == HumanoidArm.RIGHT ? 0.0625F : -0.0625F, -0.625F, 0.125F);
    }

    private Vector3f palm(String bone) {
        float[] box = ModernEquipmentLayer.attachmentBounds(rig.bone(bone));
        return new Vector3f((box[0] + box[3]) / 32, (box[1] + box[4]) / 32, (box[2] + box[5]) / 32);
    }

    void translateToMouth(PoseStack pose) {
        rig.transform("muzzle", pose);
        // Lift the item into the bite line by one normalized model pixel.
        pose.translate(0, 0.03F - 1.0F / (16 * rig.scale()), -0.3F);
        pose.scale(1 / rig.scale(), 1 / rig.scale(), 1 / rig.scale());
    }

    void resetPose() {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.xScale = root.yScale = root.zScale = rig.scale();
    }

    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(pose, buffer, light, overlay, color);
    }

    private record Binding(ModelPart source, ModelPart target) {}
}
