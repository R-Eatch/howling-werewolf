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
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Retargets the established actions onto the imported skeleton, preserving its authored rest pose. */
final class ModernPlayerModel<T extends LivingEntity> extends EntityModel<T> implements ArmedModel, HeadedModel {
    private static final String[] FORE_RIGHT = {"rightArmRotation", "right_forearn"}, FORE_LEFT = {"leftArmRotation", "left_forearn"};
    private static final String[] HIND_RIGHT = {"rightLeg", "right_leg_lower", "right_foot_upper"}, HIND_LEFT = {"leftLeg", "left_leg_lower", "left_foot_upper"};
    private static final List<String> RELAXED_JOINTS = List.of("rightArm", "leftArm", "rightArmRotation", "leftArmRotation", "right_forearn", "left_forearn",
            "rightLeg", "leftLeg", "right_leg_lower", "left_leg_lower");
    private static final List<String> FOREARMS = List.of("right_forearn", "left_forearn");
    private static final float ELBOW_FUR_LAY = 0.5F, FOUR_LEGGED_FUR_FOLD = 0.6F;
    private static final float SHOULDER_IN = 0.40F, HIP_RANGE = 1.05F, HIP_SOCKET = 11, REACH_MARGIN = 0.96F;
    // Carriage of the gallop and prowl: root drop in pixels, then torso lean and chest and neck pitch in radians.
    private static final float GALLOP_HEIGHT = -0.6F, GALLOP_LEAN = 1.52F, GALLOP_CHEST = -0.22F, GALLOP_GAZE = 0.10F;
    private static final float FORE_BIAS = -0.10F, HIND_BIAS = 0.02F;
    private static final float PROWL_HEIGHT = 1.5F, PROWL_LEAN = 1.52F, PROWL_CHEST = -0.22F, PROWL_GAZE = 0.05F;
    private static final float PROWL_FORE_BIAS = -0.04F, PROWL_HIND_BIAS = -0.02F;
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
    private final Map<String, Float> bendDirection = new HashMap<>();

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
        if (form == WerewolfForm.BEAST) {
            restBend("right_paw", FORE_RIGHT); restBend("left_paw", FORE_LEFT);
            restBend("right_foot_lower", HIND_RIGHT); restBend("left_foot_lower", HIND_LEFT);
        }
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
            var motion = state;
            if (state.isCrouching && !state.isPassenger && !state.isInWater && !state.isFallFlying && state.isUsingItem) {
                // Aiming keeps the upright crouch, so native arm poses still point at the target.
                root.y += 2.4F;
                rig.bone("waist").xRot += 0.72F;
                rig.bone("head").xRot -= 0.72F;
                placeLimb("right_foot_lower", rightFootContact, HIND_RIGHT, "rightLeg", 0, 0, 0, 0, 1);
                placeLimb("left_foot_lower", leftFootContact, HIND_LEFT, "leftLeg", 0, 0, 0, 0, 1);
            } else if (motion.beastProwlAmount > 0 && !blockedQuadruped(state)) {
                applyBeastProwl(state, motion.beastProwlAmount);
            }
            applyBeastRun(state, motion.beastRunAmount, motion.beastLeapAmount);
        }
        layElbowFur();
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

    /**
     * The elbow tuft is modelled on the forearm. Undoing half of the bend lets it sweep out of the
     * joint between the two arm bones, instead of jutting out in line with the folded forearm.
     * On all fours the Beast folds its forearms far past any upright swing, and laying the tuft by
     * half of that turned it out sideways; there the lay stops at a small fold.
     */
    private void layElbowFur() {
        for (String bone : FOREARMS) {
            ModelPart forearm = rig.bones().get(bone);
            List<ModelPart> tufts = rig.flats().get(bone);
            if (forearm == null || tufts == null) continue;
            float bend = forearm.xRot - forearm.getInitialPose().xRot;
            float carriage = Math.max(state.beastRunAmount, state.beastProwlAmount);
            bend = Mth.lerp(carriage, bend, Mth.clamp(bend, -FOUR_LEGGED_FUR_FOLD, FOUR_LEGGED_FUR_FOLD));
            if (Math.abs(bend) < 0.001F) continue;
            for (ModelPart tuft : tufts) {
                var rest = tuft.getInitialPose();
                Vector3f euler = new Quaternionf().rotationX(-bend * ELBOW_FUR_LAY)
                        .mul(new Quaternionf().rotationZYX(rest.zRot, rest.yRot, rest.xRot))
                        .getEulerAnglesZYX(new Vector3f());
                tuft.xRot = euler.x;
                tuft.yRot = euler.y;
                tuft.zRot = euler.z;
            }
        }
    }

    private void animateTail(ModernRenderState state) {
        float phase = state.ageInTicks * 0.085F;
        float movement = Mth.clamp(state.walkAnimationSpeed, 0, 1);
        float strength = state.isCrouching ? 0.55F : form == WerewolfForm.BEAST
                && state.beastRunAmount > 0 ? 0.45F : 1;
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

    private static boolean blockedQuadruped(ModernRenderState state) {
        return state.isPassenger || state.isInWater || state.isVisuallySwimming || state.isFallFlying
                || state.isAutoSpinAttack || state.isUsingItem;
    }

    /** Levels the spine over all four limbs: the Beast's upright rig folded into a quadruped carriage. */
    private void carryOnFours(ModernRenderState state, float amount, float height, float lean, float chest, float gaze) {
        ModelPart head = rig.bone("head");
        root.y += height * amount;
        rig.bone("waist").xRot += lean * amount;
        ModelPart body = rig.bone("body");
        ModelPart upper = rig.bone("body_upper");
        body.xRot = Mth.lerp(amount, body.xRot, 0);
        upper.xRot = Mth.lerp(amount, upper.xRot, upper.getInitialPose().xRot + chest);
        // The neck cancels every pitch of the spine, so the eyes stay level through the stride.
        float level = -(lean + chest + upper.getInitialPose().xRot);
        head.xRot = Mth.lerp(amount, head.xRot, level + gaze + Mth.clamp(state.xRot * Mth.DEG_TO_RAD, -0.5F, 0.5F));
        for (String name : RELAXED_JOINTS) {
            ModelPart joint = rig.bone(name);
            joint.xRot = Mth.lerp(amount, joint.xRot, joint.getInitialPose().xRot);
            joint.yRot = Mth.lerp(amount, joint.yRot, joint.getInitialPose().yRot);
            joint.zRot = Mth.lerp(amount, joint.zRot, joint.getInitialPose().zRot);
        }
        // Bring the paws in under the chest rather than out at the shoulders.
        rig.bone("rightArm").zRot += SHOULDER_IN * amount;
        rig.bone("leftArm").zRot -= SHOULDER_IN * amount;
        // The tail hangs off the torso, so it streams behind instead of rising with the lean.
        rig.bone("tail").xRot -= lean * 0.92F * amount;
    }

    private void applyBeastRun(ModernRenderState state, float amount, float leap) {
        if (amount <= 0 || state.isCrouching || blockedQuadruped(state)) return;
        float speed = Math.min(state.walkAnimationSpeed, 1);
        float cycle = BeastGait.stride(state.walkAnimationPos, BeastGait.GALLOP_RATE);
        float pitch = BeastGait.pitch(cycle) * speed * (1 - leap);
        float flex = BeastGait.flex(cycle) * speed * (1 - leap);
        float rise = BeastGait.rise(cycle) * speed * (1 - leap);
        carryOnFours(state, amount, GALLOP_HEIGHT - rise * 0.8F - leap * 1.5F, GALLOP_LEAN - pitch * 0.09F + leap * -0.10F,
                GALLOP_CHEST + flex * 0.22F - leap * 0.12F, GALLOP_GAZE);
        rig.bone("waist").zRot += Mth.sin(cycle * Mth.TWO_PI) * 0.025F * speed * amount;
        rig.bone("jaw").xRot -= (0.10F + Math.max(0, -flex) * 0.08F) * amount;
        rig.bone("tail_main12").xRot += Mth.sin((cycle - 0.20F) * Mth.TWO_PI) * 0.14F * speed * amount;
        rig.bone("tail_12").xRot += Mth.sin((cycle - 0.32F) * Mth.TWO_PI) * 0.20F * speed * amount;
        float reach = (0.40F + speed * 0.20F) * (1 - leap * 0.2F);
        limbGallop("right_paw", rightPawContact, FORE_RIGHT, "rightArmRotation", cycle, BeastGait.FORE_LEAD, reach, true, amount, speed, leap);
        limbGallop("left_paw", leftPawContact, FORE_LEFT, "leftArmRotation", cycle, BeastGait.FORE_TRAIL, reach, true, amount, speed, leap);
        limbGallop("right_foot_lower", rightFootContact, HIND_RIGHT, "rightLeg", cycle, BeastGait.HIND_LEAD, reach, false, amount, speed, leap);
        limbGallop("left_foot_lower", leftFootContact, HIND_LEFT, "leftLeg", cycle, BeastGait.HIND_TRAIL, reach, false, amount, speed, leap);
        if (state.attackTime > 0) {
            // A running attack lifts the attacking paw; the other three limbs keep their gait.
            float eased = 1 - (float) Math.pow(1 - state.attackTime, 4);
            float swipe = Mth.sin(eased * Mth.PI) * amount;
            boolean right = state.attackArm == HumanoidArm.RIGHT;
            rig.bone(right ? "rightArmRotation" : "leftArmRotation").xRot -= swipe * 0.9F;
            rig.bone(right ? "right_forearn" : "left_forearn").xRot -= swipe * 0.25F;
        }
    }

    private void limbGallop(String paw, Vector3f contact, String[] joints, String anchor, float cycle, float start,
                            float reach, boolean fore, float amount, float speed, float leap) {
        BeastGait.Step step = BeastGait.gallop(cycle, start);
        // Airborne limbs stretch out: forelegs reach ahead, hind legs trail behind.
        float travel = Mth.lerp(leap, step.travel() * Math.max(speed, 0.2F), fore ? -0.95F : 0.95F);
        float lift = Mth.lerp(leap, step.lift() * (fore ? 0.30F : 0.20F) * speed, fore ? 0.55F : 0.42F);
        float bias = fore ? FORE_BIAS : HIND_BIAS;
        float span = reach * (fore ? 1 : 1.12F);
        // A swinging forepaw folds at the wrist; a hind paw trails from the hock.
        float tilt = step.tuck() * (fore ? -0.9F : 0.7F) * speed * (1 - leap);
        placeLimb(paw, contact, joints, anchor, bias + travel * span, lift, tilt, 1, amount);
    }

    private void applyBeastProwl(ModernRenderState state, float amount) {
        float speed = Math.min(state.walkAnimationSpeed * 1.4F, 1);
        float cycle = BeastGait.stride(state.walkAnimationPos, BeastGait.PROWL_RATE);
        carryOnFours(state, amount, PROWL_HEIGHT, PROWL_LEAN + Mth.sin(cycle * Mth.TWO_PI * 2) * 0.012F * speed,
                PROWL_CHEST, PROWL_GAZE);
        rig.bone("waist").zRot += Mth.sin(cycle * Mth.TWO_PI) * 0.03F * speed * amount;
        float reach = 0.26F * speed;
        limbProwl("right_foot_lower", rightFootContact, HIND_RIGHT, "rightLeg", cycle, BeastGait.PROWL[0], reach, false, amount, speed);
        limbProwl("right_paw", rightPawContact, FORE_RIGHT, "rightArmRotation", cycle, BeastGait.PROWL[1], reach, true, amount, speed);
        limbProwl("left_foot_lower", leftFootContact, HIND_LEFT, "leftLeg", cycle, BeastGait.PROWL[2], reach, false, amount, speed);
        limbProwl("left_paw", leftPawContact, FORE_LEFT, "leftArmRotation", cycle, BeastGait.PROWL[3], reach, true, amount, speed);
    }

    private void limbProwl(String paw, Vector3f contact, String[] joints, String anchor, float cycle, float start,
                           float reach, boolean fore, float amount, float speed) {
        BeastGait.Step step = BeastGait.prowl(cycle, start);
        placeLimb(paw, contact, joints, anchor, (fore ? PROWL_FORE_BIAS : PROWL_HIND_BIAS) + step.travel() * reach,
                step.lift() * 0.20F * speed, step.tuck() * (fore ? -0.5F : 0.4F) * speed, 1, amount);
    }

    private void placeLimb(String effector, Vector3f contact, String[] joints, String anchor,
                           float offsetZ, float lift, float pawTilt, float zWeight, float amount) {
        Vector3f current = matrix(effector).transformPosition(contact, new Vector3f());
        Vector3f initialDown = matrix(effector).transformDirection(new Vector3f(0, 1, 0));
        float pawPitch = (float) Math.atan2(initialDown.z, initialDown.y) * (1 - amount) + pawTilt * amount;
        int jointCount = joints.length;
        Vector3f origin = matrix(anchor).transformPosition(new Vector3f());
        float bottom = pawBottom(effector);
        // Lowering the hips during a blend must not push the paws through the floor
        // while only part of their inverse kinematics is blended in.
        float desiredBottom = Math.min(1.5F, Mth.lerp(amount, bottom, 1.5F - lift));
        float goalZ = zWeight > 0 ? origin.z + offsetZ : current.z;
        Vector3f target = new Vector3f(current.x, current.y + desiredBottom - bottom, Mth.lerp(amount, current.z, goalZ));
        boolean hind = jointCount == 3;
        ModelPart hip = rig.bone(joints[0]);
        float hipRest = hip.getInitialPose().xRot, hipY = hip.y, hipZ = hip.z;
        // Planar inverse kinematics keeps grounded paws on the floor through the stance.
        for (int pass = 0; pass < 3; pass++) {
            levelPaw(effector, pawPitch);
            keepWithinReach(effector, contact, joints, target);
            seedLimb(effector, contact, joints, target);
            if (hind) {
                hip.xRot = Mth.clamp(hip.xRot, hipRest - HIP_RANGE * amount, hipRest + HIP_RANGE * amount);
                swingFromSocket(hip, hipRest, hipY, hipZ);
            }
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
                    if (hind && index == 0) swingFromSocket(hip, hipRest, hipY, hipZ);
                    levelPaw(effector, pawPitch);
                    derivatives[index] = matrix(effector).transformPosition(contact, new Vector3f()).sub(end).div(0.002F);
                    joint.xRot = rotation;
                    if (hind && index == 0) swingFromSocket(hip, hipRest, hipY, hipZ);
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
                    float low = hind && index == 0 ? hipRest - HIP_RANGE * amount : centers[index] - 1.2F;
                    float high = hind && index == 0 ? hipRest + HIP_RANGE * amount : centers[index] + 1.2F;
                    joint.xRot = Mth.clamp(joint.xRot + Mth.clamp(step, -0.3F, 0.3F), low, high);
                }
                if (hind) swingFromSocket(hip, hipRest, hipY, hipZ);
            }
            levelPaw(effector, pawPitch);
            target.y += desiredBottom - pawBottom(effector);
        }
    }

    /**
     * A goal past the limb's full length has no stable solution and the joints would chatter.
     * Pulling the goal back along its own direction keeps the paw moving smoothly.
     */
    private void keepWithinReach(String effector, Vector3f contact, String[] joints, Vector3f target) {
        Vector3f root = matrix(joints[0]).transformPosition(new Vector3f());
        float length = 0;
        Vector3f previous = root;
        for (int index = 1; index < joints.length; index++) {
            Vector3f next = matrix(joints[index]).transformPosition(new Vector3f());
            length += planar(previous, next);
            previous = next;
        }
        length += planar(previous, matrix(effector).transformPosition(contact, new Vector3f()));
        float reach = length * REACH_MARGIN;
        float height = target.y - root.y, forward = target.z - root.z;
        float distance = Mth.sqrt(height * height + forward * forward);
        if (distance <= reach) return;
        target.y = root.y + height * reach / distance;
        target.z = root.z + forward * reach / distance;
    }
    private static float planar(Vector3f from, Vector3f to) {
        float y = to.y - from.y, z = to.z - from.z;
        return Mth.sqrt(y * y + z * z);
    }

    /**
     * This rig pivots the thigh at its lower end, so turning it there would pull its top off the
     * pelvis. Sliding the pivot as it turns makes the whole leg swing from the hip socket instead.
     */
    private static void swingFromSocket(ModelPart hip, float rest, float baseY, float baseZ) {
        float angle = hip.xRot - rest;
        hip.y = baseY + HIP_SOCKET * (Mth.cos(angle) - 1);
        hip.z = baseZ + HIP_SOCKET * Mth.sin(angle);
    }

    private void restBend(String effector, String[] joints) {
        Vector3f first = matrix(joints[0]).transformPosition(new Vector3f());
        Vector3f second = matrix(joints[1]).transformPosition(new Vector3f());
        Vector3f tip = matrix(effector).transformPosition(new Vector3f());
        float upper = (float) Math.atan2(second.z - first.z, second.y - first.y);
        float lower = (float) Math.atan2(tip.z - second.z, tip.y - second.y);
        bendDirection.put(joints[1], Math.signum(wrap(lower - upper)));
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
        // A limb near full extension has almost no bend to read a direction from, so each joint
        // keeps the direction it has at rest instead of flipping and snapping the paw across.
        float goalBend = bendDirection.getOrDefault(joints[1], Math.signum(bend)) * (float) Math.acos(Mth.clamp(cosine, -1, 1));
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
        Vector3f grip = actual.transformPosition(arm == HumanoidArm.RIGHT ? rightGrip : leftGrip, new Vector3f());
        var motion = state;
        boolean nativeOrientation = form != WerewolfForm.BEAST || motion.beastRunAmount <= 0 && motion.beastProwlAmount <= 0
                || state.isUsingItem || state.isFallFlying || state.isInWater;
        org.joml.Quaternionf rotation;
        if (nativeOrientation) {
            PoseStack nativePose = new PoseStack();
            nativeHands.translateToHand(arm, nativePose);
            rotation = nativePose.last().pose().getUnnormalizedRotation(new org.joml.Quaternionf()).normalize();
        } else rotation = actual.getUnnormalizedRotation(new org.joml.Quaternionf()).normalize();
        // Import normalization scales the body only. Weapons retain their native size.
        pose.translate(grip.x, grip.y, grip.z);
        pose.mulPose(rotation);
        // Cancel ItemInHandLayer's shoulder-to-hand offset: this transform already reaches the palm.
        pose.translate(arm == HumanoidArm.RIGHT ? 0.0625F : -0.0625F, -0.625F, 0.125F);
    }

    private Vector3f palm(String bone) {
        float[] box = ModernEquipmentLayer.attachmentBounds(rig.bone(bone));
        return new Vector3f((box[0] + box[3]) / 32, (box[1] + box[4]) / 32, (box[2] + box[5]) / 32);
    }

    void translateToMouth(PoseStack pose) {
        rig.transform("muzzle", pose);
        pose.translate(0, 0.03F, -0.3F);
        pose.scale(1 / rig.scale(), 1 / rig.scale(), 1 / rig.scale());
    }

    void resetPose() {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.xScale = root.yScale = root.zScale = rig.scale();
    }

    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        BlockbenchModel.render(root, pose, buffer, light, overlay, red, green, blue, alpha);
    }

    private record Binding(ModelPart source, ModelPart target) {}
}
