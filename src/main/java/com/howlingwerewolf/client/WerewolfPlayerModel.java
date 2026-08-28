package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * Standalone original Howling Werewolf player model.
 *
 * - normal-form legs stay root siblings so the vanilla crouch/stance work remains intact;
 * - head presentation keeps the classic 1.12.2-style silhouette with one protruding muzzle cube;
 * - the hand-edited Blockbench pass adds a slight outward tilt to both cube ears while preserving
 *   the original head-bone hierarchy and all gameplay animation logic.
 */
public final class WerewolfPlayerModel<T extends LivingEntity> extends EntityModel<T>
        implements ArmedModel, HeadedModel {
    private static final float DEG = Mth.DEG_TO_RAD;

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart muzzle;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart tailBase;
    private final ModelPart tailTip;

    public WerewolfPlayerModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("torso");
        this.chest = torso.getChild("chest");
        this.head = chest.getChild("head");
        this.muzzle = head.getChild("muzzle");
        this.rightArm = chest.getChild("right_arm");
        this.leftArm = chest.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.tailBase = torso.getChild("tail_base");
        this.tailTip = tailBase.getChild("tail_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition torso = root.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(0, 28)
                        .addBox(-4.0F, 0.0F, -2.5F, 8.0F, 11.0F, 5.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        torso.addOrReplaceChild("pelvis",
                CubeListBuilder.create().texOffs(62, 28)
                        .addBox(-4.5F, 9.0F, -2.0F, 9.0F, 3.0F, 4.0F),
                PartPose.ZERO);

        PartDefinition chest = torso.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(28, 28)
                        .addBox(-5.0F, -5.0F, -3.0F, 10.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition head = chest.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -7.0F, -3.5F, 8.0F, 7.0F, 7.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        head.addOrReplaceChild("muzzle",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-2.0F, -1.5F, -3.0F, 4.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -0.8F, -3.3F));
        PartDefinition rightEar = head.addOrReplaceChild("right_ear",
                CubeListBuilder.create(),
                PartPose.offset(-2.5F, -7.0F, 0.0F));
        rightEar.addOrReplaceChild("right_ear_r1",
                CubeListBuilder.create().texOffs(76, 0)
                        .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -7.5F * DEG));
        PartDefinition leftEar = head.addOrReplaceChild("left_ear",
                CubeListBuilder.create(),
                PartPose.offset(2.5F, -7.0F, 0.0F));
        leftEar.addOrReplaceChild("left_ear_r1",
                CubeListBuilder.create().texOffs(88, 0)
                        .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 7.5F * DEG));

        chest.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-4.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offset(-4.75F, -3.0F, 0.0F));
        chest.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(18, 48)
                        .addBox(0.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offset(4.75F, -3.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(36, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.1F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(54, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.1F, 12.0F, 0.0F));

        PartDefinition tailBase = torso.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(76, 18)
                        .addBox(-2.0F, -1.0F, -0.5F, 4.0F, 7.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 48.0F * DEG, 0.0F, 0.0F));
        tailBase.addOrReplaceChild("tail_tip",
                CubeListBuilder.create().texOffs(92, 18)
                        .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 1.5F, 20.0F * DEG, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        boolean fallFlying = player.getFallFlyingTicks() > 4;
        boolean swimming = player.isVisuallySwimming();
        float swimAmount = player.getSwimAmount(0.0F);

        // Default sibling layout; crouch/fall-flying will override some of these values.
        torso.y = 1.0F;
        rightLeg.y = 12.0F;
        leftLeg.y = 12.0F;
        rightLeg.z = 0.0F;
        leftLeg.z = 0.0F;

        float headYaw = netHeadYaw * DEG;
        float headX = headPitch * DEG;
        if (fallFlying) {
            headX = -Mth.PI / 4.0F;
        } else if (swimAmount > 0.0F) {
            headX = Mth.rotLerp(swimAmount, headX, swimming ? -Mth.PI / 4.0F : headX);
        }
        head.yRot = headYaw;
        head.xRot = headX;

        float motionScale = 1.0F;
        if (fallFlying) {
            motionScale = (float) player.getDeltaMovement().lengthSqr();
            motionScale = motionScale / 0.2F;
            motionScale = motionScale * motionScale * motionScale;
            if (motionScale < 1.0F) motionScale = 1.0F;
        }

        float walkAmount = Mth.clamp(limbAmount, 0.0F, 1.0F);
        float cycle = limbSwing * 0.6662F;
        rightArm.xRot = Mth.cos(cycle + Mth.PI) * 2.0F * walkAmount * 0.5F / motionScale;
        leftArm.xRot = Mth.cos(cycle) * 2.0F * walkAmount * 0.5F / motionScale;
        rightLeg.xRot = Mth.cos(cycle) * 1.4F * walkAmount / motionScale;
        leftLeg.xRot = Mth.cos(cycle + Mth.PI) * 1.4F * walkAmount / motionScale;

        torso.xRot = 2.0F * DEG;
        chest.xRot = -1.0F * DEG;

        if (player.isPassenger()) {
            rightArm.xRot -= Mth.PI / 5.0F;
            leftArm.xRot -= Mth.PI / 5.0F;
            rightLeg.xRot = -1.4137167F;
            rightLeg.yRot = Mth.PI / 10.0F;
            rightLeg.zRot = 0.07853982F;
            leftLeg.xRot = -1.4137167F;
            leftLeg.yRot = -Mth.PI / 10.0F;
            leftLeg.zRot = -0.07853982F;
        }

        if (player.isCrouching()) {
            // Match vanilla Steve crouch more closely: body pitches forward, legs move forward,
            // body lowers slightly, but the head remains visually upright instead of rotating with
            // the whole upper body like a rigid slab.
            torso.xRot = 0.5F;
            chest.xRot += 2.0F * DEG;
            torso.y = 3.2F;
            rightLeg.z = 4.0F;
            leftLeg.z = 4.0F;
            rightLeg.y = 12.2F;
            leftLeg.y = 12.2F;
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
            head.xRot -= 0.47F;
            tailBase.xRot += 0.12F;
        }

        if (player.isSprinting() && !player.isCrouching()) {
            torso.xRot += 8.0F * DEG;
            chest.xRot += 5.0F * DEG;
            rightArm.xRot *= 1.18F;
            leftArm.xRot *= 1.18F;
        }

        applySpecialMovementPose(player, ageInTicks, swimAmount, fallFlying, swimming);
        applyHeldItemRestPose(player);
        applyUseItemPose(player);
        animateAttack(player);

        AnimationUtils.bobModelPart(rightArm, ageInTicks, 1.0F);
        AnimationUtils.bobModelPart(leftArm, ageInTicks, -1.0F);

        float idleBreath = Mth.sin(ageInTicks * 0.08F) * 0.025F;
        chest.xRot += idleBreath;
        float tailWave = Mth.sin(ageInTicks * 0.12F) * (0.10F + 0.08F * walkAmount);
        if (player.isCrouching()) tailWave *= 0.55F;
        tailBase.yRot = tailWave;
        tailTip.yRot = tailWave * 1.4F;
        muzzle.xRot = Mth.sin(ageInTicks * 0.045F) * 0.01F;
    }

    private void applySpecialMovementPose(T player, float ageInTicks, float swimAmount,
                                          boolean fallFlying, boolean swimming) {
        if (fallFlying) {
            torso.xRot = 0.06F;
            chest.xRot = 0.08F;
            rightArm.xRot = -1.35F;
            leftArm.xRot = -1.35F;
            rightArm.yRot = 0.10F;
            leftArm.yRot = -0.10F;
            rightLeg.xRot = 0.12F;
            leftLeg.xRot = 0.12F;
            tailBase.xRot += 0.22F;
            return;
        }

        if (swimAmount <= 0.0F) return;

        float swimCycle = ageInTicks % 26.0F;
        float armStroke;
        if (swimCycle < 14.0F) {
            armStroke = Mth.rotLerp(swimAmount, rightArm.xRot, 0.0F);
            rightArm.xRot = armStroke;
            leftArm.xRot = armStroke;
            rightArm.yRot = Mth.rotLerp(swimAmount, rightArm.yRot, Mth.PI);
            leftArm.yRot = Mth.rotLerp(swimAmount, leftArm.yRot, Mth.PI);
            rightArm.zRot = Mth.rotLerp(swimAmount, rightArm.zRot, Mth.PI + 1.8707964F * quadraticArmUpdate(swimCycle) / quadraticArmUpdate(14.0F));
            leftArm.zRot = Mth.rotLerp(swimAmount, leftArm.zRot, Mth.PI - 1.8707964F * quadraticArmUpdate(swimCycle) / quadraticArmUpdate(14.0F));
        } else if (swimCycle < 22.0F) {
            float phase = (swimCycle - 14.0F) / 8.0F;
            rightArm.xRot = Mth.rotLerp(swimAmount, rightArm.xRot, ((Mth.PI / 2.0F) * phase) - (Mth.PI / 2.0F));
            leftArm.xRot = Mth.rotLerp(swimAmount, leftArm.xRot, ((Mth.PI / 2.0F) * phase) - (Mth.PI / 2.0F));
            rightArm.zRot = Mth.rotLerp(swimAmount, rightArm.zRot, 5.012389F - 1.8707964F * phase);
            leftArm.zRot = Mth.rotLerp(swimAmount, leftArm.zRot, 1.2707963F + 1.8707964F * phase);
        } else {
            float phase = (swimCycle - 22.0F) / 4.0F;
            rightArm.xRot = Mth.rotLerp(swimAmount, rightArm.xRot, ((Mth.PI / 2.0F) * (1.0F - phase)) - (Mth.PI / 2.0F));
            leftArm.xRot = Mth.rotLerp(swimAmount, leftArm.xRot, ((Mth.PI / 2.0F) * (1.0F - phase)) - (Mth.PI / 2.0F));
            rightArm.zRot = Mth.rotLerp(swimAmount, rightArm.zRot, Mth.PI);
            leftArm.zRot = Mth.rotLerp(swimAmount, leftArm.zRot, Mth.PI);
        }

        rightLeg.xRot = Mth.rotLerp(swimAmount, rightLeg.xRot, 0.3F * Mth.cos(ageInTicks * 0.33333334F + Mth.PI));
        leftLeg.xRot = Mth.rotLerp(swimAmount, leftLeg.xRot, 0.3F * Mth.cos(ageInTicks * 0.33333334F));
        if (swimming) torso.xRot += 6.0F * DEG;
    }

    private static float quadraticArmUpdate(float progress) {
        return -65.0F * progress + progress * progress;
    }

    private void applyHeldItemRestPose(T player) {
        if (player.isUsingItem()) return;
        if (!player.getMainHandItem().isEmpty()) {
            ModelPart arm = player.getMainArm() == HumanoidArm.RIGHT ? rightArm : leftArm;
            arm.xRot = arm.xRot * 0.5F - Mth.PI / 10.0F;
            arm.yRot = 0.0F;
        }
        if (!player.getOffhandItem().isEmpty()) {
            ModelPart arm = player.getMainArm() == HumanoidArm.RIGHT ? leftArm : rightArm;
            arm.xRot = arm.xRot * 0.5F - Mth.PI / 10.0F;
            arm.yRot = 0.0F;
        }
    }

    private void applyUseItemPose(T player) {
        if (!player.isUsingItem()) return;
        HumanoidArm mainArm = player.getMainArm();
        boolean useRight = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? mainArm == HumanoidArm.RIGHT
                : mainArm == HumanoidArm.LEFT;
        poseArm(useRight ? rightArm : leftArm, useRight, player.getUseItem());
    }

    private static void poseArm(ModelPart arm, boolean right, ItemStack stack) {
        UseAnim anim = stack.getUseAnimation();
        switch (anim) {
            case BLOCK -> {
                arm.xRot = arm.xRot * 0.5F - 0.9424779F;
                arm.yRot = (right ? -1.0F : 1.0F) * 0.5235988F;
            }
            case BOW -> {
                arm.yRot = right ? -0.1F : 0.1F;
                arm.xRot = -1.45F;
            }
            case SPEAR -> {
                arm.xRot = arm.xRot * 0.5F - Mth.PI;
                arm.yRot = 0.0F;
            }
            case CROSSBOW -> {
                arm.yRot = right ? -0.8F : 0.8F;
                arm.xRot = -0.95F;
            }
            case SPYGLASS -> {
                arm.xRot = Mth.clamp(-1.9198622F - 0.2617994F, -2.4F, 3.3F);
                arm.yRot = (right ? -0.15F : 0.15F);
            }
            case DRINK, EAT -> {
                arm.xRot = arm.xRot * 0.5F - 1.15F;
                arm.yRot = (right ? -1.0F : 1.0F) * 0.20F;
            }
            default -> {
            }
        }
    }

    private void animateAttack(T player) {
        if (attackTime <= 0.0F || player.isUsingItem()) return;

        boolean left = player.getMainArm() == HumanoidArm.LEFT;
        ModelPart attackingArm = left ? leftArm : rightArm;

        // Follow vanilla HumanoidModel's attack curve rather than throwing the arm straight
        // forward. The torso twists first, then the active arm sweeps across the body.
        float bodySwing = Mth.sin(Mth.sqrt(attackTime) * (Mth.PI * 2.0F)) * 0.20F;
        if (left) bodySwing = -bodySwing;
        chest.yRot += bodySwing;

        rightArm.yRot += bodySwing;
        leftArm.yRot += bodySwing;
        leftArm.xRot += bodySwing;

        float eased = 1.0F - attackTime;
        eased *= eased;
        eased *= eased;
        eased = 1.0F - eased;
        float sweep = Mth.sin(eased * Mth.PI);
        float headCompensation = Mth.sin(attackTime * Mth.PI)
                * -(head.xRot - 0.7F) * 0.75F;

        attackingArm.xRot -= sweep * 1.20F + headCompensation;
        attackingArm.yRot += bodySwing * 2.0F;
        attackingArm.zRot += Mth.sin(attackTime * Mth.PI) * -0.40F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getHead() {
        return head;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        torso.translateAndRotate(poseStack);
        chest.translateAndRotate(poseStack);
        (arm == HumanoidArm.RIGHT ? rightArm : leftArm).translateAndRotate(poseStack);
    }
}
