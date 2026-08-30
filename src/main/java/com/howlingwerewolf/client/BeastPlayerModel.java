package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * Original high-tier Beast form for Howling Werewolf.
 * Geometry, UVs and motion were designed specifically for this project.
 * The established animation rig uses the hand-edited Blockbench ear tilt/inflate and
 * its synchronized entity texture.
 */
public final class BeastPlayerModel<T extends LivingEntity> extends EntityModel<T>
        implements ArmedModel, HeadedModel {
    private static final float DEG = Mth.DEG_TO_RAD;

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightUpperArm;
    private final ModelPart rightForearm;
    private final ModelPart leftUpperArm;
    private final ModelPart leftForearm;
    private final ModelPart rightThigh;
    private final ModelPart rightShin;
    private final ModelPart leftThigh;
    private final ModelPart leftShin;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;

    public BeastPlayerModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("torso");
        this.head = torso.getChild("head");
        this.jaw = head.getChild("jaw");
        this.rightUpperArm = torso.getChild("right_upper_arm");
        this.rightForearm = rightUpperArm.getChild("right_forearm");
        this.leftUpperArm = torso.getChild("left_upper_arm");
        this.leftForearm = leftUpperArm.getChild("left_forearm");
        this.rightThigh = root.getChild("right_thigh");
        this.rightShin = rightThigh.getChild("right_shin");
        this.leftThigh = root.getChild("left_thigh");
        this.leftShin = leftThigh.getChild("left_shin");
        this.tailBase = root.getChild("tail_base");
        this.tailMid = tailBase.getChild("tail_mid");
        this.tailTip = tailMid.getChild("tail_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition torso = root.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(0, 34)
                        .addBox(-6.0F, -12.0F, -3.5F, 12.0F, 14.0F, 7.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));
        // Overlapping hip mass prevents visible seams between the independently animated
        // torso and digitigrade thighs, including during crouch and attack twists.
        root.addOrReplaceChild("pelvis",
                CubeListBuilder.create().texOffs(88, 108)
                        .addBox(-5.0F, 5.0F, -3.0F, 10.0F, 4.0F, 6.0F),
                PartPose.ZERO);
        torso.addOrReplaceChild("chest_mane",
                CubeListBuilder.create().texOffs(40, 34)
                        .addBox(-7.0F, -5.0F, -4.5F, 14.0F, 7.0F, 9.0F),
                PartPose.offset(0.0F, -7.5F, 0.0F));

        PartDefinition head = torso.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0F, -7.0F, -4.5F, 10.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, -12.0F, -0.5F));
        head.addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-2.5F, -1.5F, -4.0F, 5.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -0.75F, -4.2F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(66, 0)
                        .addBox(-2.5F, -0.25F, -3.0F, 5.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 1.15F, -4.0F));
        PartDefinition rightEar = head.addOrReplaceChild("right_ear",
                CubeListBuilder.create(),
                PartPose.offset(-3.0F, -7.0F, 0.0F));
        rightEar.addOrReplaceChild("right_ear_r1",
                CubeListBuilder.create().texOffs(90, 0)
                        .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -5.0F * DEG));
        PartDefinition leftEar = head.addOrReplaceChild("left_ear",
                CubeListBuilder.create(),
                PartPose.offset(3.0F, -7.0F, 0.0F));
        leftEar.addOrReplaceChild("left_ear_r1",
                CubeListBuilder.create().texOffs(104, 0)
                        .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 5.0F * DEG));

        PartDefinition rightUpperArm = torso.addOrReplaceChild("right_upper_arm",
                CubeListBuilder.create().texOffs(0, 60)
                        .addBox(-5.0F, -2.5F, -2.5F, 5.0F, 11.0F, 5.0F),
                PartPose.offset(-6.0F, -8.0F, 0.0F));
        rightUpperArm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create().texOffs(22, 60)
                        .addBox(-4.5F, 0.0F, -3.0F, 5.0F, 10.0F, 6.0F),
                PartPose.offsetAndRotation(-0.25F, 7.5F, 0.0F, -8.0F * DEG, 0.0F, 0.0F));
        PartDefinition leftUpperArm = torso.addOrReplaceChild("left_upper_arm",
                CubeListBuilder.create().texOffs(46, 60)
                        .addBox(0.0F, -2.5F, -2.5F, 5.0F, 11.0F, 5.0F),
                PartPose.offset(6.0F, -8.0F, 0.0F));
        leftUpperArm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create().texOffs(68, 60)
                        .addBox(-0.5F, 0.0F, -3.0F, 5.0F, 10.0F, 6.0F),
                PartPose.offsetAndRotation(0.25F, 7.5F, 0.0F, -8.0F * DEG, 0.0F, 0.0F));

        PartDefinition rightThigh = root.addOrReplaceChild("right_thigh",
                CubeListBuilder.create().texOffs(0, 82)
                        .addBox(-2.5F, -1.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offset(-2.75F, 7.0F, 0.0F));
        rightThigh.addOrReplaceChild("right_shin",
                CubeListBuilder.create().texOffs(22, 82)
                        .addBox(-2.5F, 0.0F, -2.0F, 5.0F, 8.0F, 4.0F)
                        .texOffs(22, 96).addBox(-3.0F, 7.0F, -4.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.5F, 8.0F * DEG, 0.0F, 0.0F));
        PartDefinition leftThigh = root.addOrReplaceChild("left_thigh",
                CubeListBuilder.create().texOffs(46, 82)
                        .addBox(-2.5F, -1.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offset(2.75F, 7.0F, 0.0F));
        leftThigh.addOrReplaceChild("left_shin",
                CubeListBuilder.create().texOffs(68, 82)
                        .addBox(-2.5F, 0.0F, -2.0F, 5.0F, 8.0F, 4.0F)
                        .texOffs(68, 96).addBox(-3.0F, 7.0F, -4.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.5F, 8.0F * DEG, 0.0F, 0.0F));

        PartDefinition tailBase = root.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(92, 30)
                        .addBox(-2.0F, -1.0F, -1.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 2.5F, 42.0F * DEG, 0.0F, 0.0F));
        PartDefinition tailMid = tailBase.addOrReplaceChild("tail_mid",
                CubeListBuilder.create().texOffs(92, 46)
                        .addBox(-2.0F, 0.0F, -1.25F, 4.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 6.5F, 1.5F, 18.0F * DEG, 0.0F, 0.0F));
        tailMid.addOrReplaceChild("tail_tip",
                CubeListBuilder.create().texOffs(108, 46)
                        .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 6.5F, 1.0F, 10.0F * DEG, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        boolean fallFlying = player.getFallFlyingTicks() > 4;
        boolean swimming = player.isVisuallySwimming();
        float swimAmount = player.getSwimAmount(0.0F);

        head.yRot = netHeadYaw * DEG;
        head.xRot = fallFlying ? -Mth.PI / 5.0F : headPitch * DEG;
        torso.xRot = 8.0F * DEG;

        float speed = Mth.clamp(limbAmount, 0.0F, 1.0F);
        float cycle = limbSwing * 0.58F;
        rightUpperArm.xRot = Mth.cos(cycle + Mth.PI) * 1.15F * speed;
        leftUpperArm.xRot = Mth.cos(cycle) * 1.15F * speed;
        rightForearm.xRot = rightUpperArm.xRot * 0.35F;
        leftForearm.xRot = leftUpperArm.xRot * 0.35F;
        rightThigh.xRot = Mth.cos(cycle) * 1.05F * speed;
        leftThigh.xRot = Mth.cos(cycle + Mth.PI) * 1.05F * speed;
        rightShin.xRot = Math.max(0.0F, -rightThigh.xRot) * 0.45F;
        leftShin.xRot = Math.max(0.0F, -leftThigh.xRot) * 0.45F;

        if (player.isPassenger()) {
            rightThigh.xRot = -1.2F;
            leftThigh.xRot = -1.2F;
            rightThigh.yRot = 0.35F;
            leftThigh.yRot = -0.35F;
        }

        if (player.isCrouching()) {
            // Heavy Beast crouch: lower the hips and bend the limbs without tipping the
            // whole creature forward. This keeps the torso connected to the upper legs.
            torso.xRot += 6.0F * DEG;
            torso.y = 5.0F;
            rightThigh.y = 8.0F;
            leftThigh.y = 8.0F;
            rightThigh.z = 2.0F;
            leftThigh.z = 2.0F;
            rightThigh.xRot -= 0.22F;
            leftThigh.xRot -= 0.22F;
            rightShin.xRot += 0.45F;
            leftShin.xRot += 0.45F;
            rightUpperArm.xRot += 0.24F;
            leftUpperArm.xRot += 0.24F;
            head.xRot -= 6.0F * DEG;
            tailBase.xRot += 6.0F * DEG;
        }

        if (player.isSprinting() && !player.isCrouching()) {
            torso.xRot += 10.0F * DEG;
            head.xRot -= 6.0F * DEG;
            rightUpperArm.xRot *= 1.20F;
            leftUpperArm.xRot *= 1.20F;
            rightThigh.xRot *= 1.15F;
            leftThigh.xRot *= 1.15F;
        }

        applySpecialMovementPose(player, ageInTicks, swimAmount, fallFlying, swimming);
        applyHeldItemRestPose(player);
        applyUseItemPose(player);
        animateAttack(player);

        AnimationUtils.bobModelPart(rightUpperArm, ageInTicks, 1.0F);
        AnimationUtils.bobModelPart(leftUpperArm, ageInTicks, -1.0F);

        float breathe = Mth.sin(ageInTicks * 0.07F) * 0.035F;
        torso.xRot += breathe;
        jaw.xRot = Math.max(0.0F, Mth.sin(ageInTicks * 0.055F)) * 0.05F;
        float wag = Mth.sin(ageInTicks * 0.10F) * (0.11F + speed * 0.09F);
        if (player.isCrouching()) wag *= 0.65F;
        tailBase.yRot = wag;
        tailMid.yRot = wag * 1.35F;
        tailTip.yRot = wag * 1.7F;
    }

    private void applySpecialMovementPose(T player, float ageInTicks, float swimAmount,
                                          boolean fallFlying, boolean swimming) {
        if (fallFlying) {
            torso.xRot += 8.0F * DEG;
            rightUpperArm.xRot = -1.30F;
            leftUpperArm.xRot = -1.30F;
            rightForearm.xRot = -0.35F;
            leftForearm.xRot = -0.35F;
            rightThigh.xRot = 0.10F;
            leftThigh.xRot = 0.10F;
            return;
        }

        if (swimAmount <= 0.0F) return;

        rightUpperArm.xRot = Mth.rotLerp(swimAmount, rightUpperArm.xRot, -1.10F);
        leftUpperArm.xRot = Mth.rotLerp(swimAmount, leftUpperArm.xRot, -1.10F);
        rightForearm.xRot = Mth.rotLerp(swimAmount, rightForearm.xRot, -0.35F);
        leftForearm.xRot = Mth.rotLerp(swimAmount, leftForearm.xRot, -0.35F);
        rightUpperArm.zRot = Mth.rotLerp(swimAmount, rightUpperArm.zRot,
                0.25F * Mth.sin(ageInTicks * 0.45F + Mth.PI));
        leftUpperArm.zRot = Mth.rotLerp(swimAmount, leftUpperArm.zRot,
                0.25F * Mth.sin(ageInTicks * 0.45F));
        rightThigh.xRot = Mth.rotLerp(swimAmount, rightThigh.xRot, 0.25F * Mth.cos(ageInTicks * 0.33F + Mth.PI));
        leftThigh.xRot = Mth.rotLerp(swimAmount, leftThigh.xRot, 0.25F * Mth.cos(ageInTicks * 0.33F));
        if (swimming) torso.xRot += 6.0F * DEG;
    }

    private void applyHeldItemRestPose(T player) {
        if (player.isUsingItem()) return;
        if (!player.getMainHandItem().isEmpty()) {
            ModelPart upper = player.getMainArm() == HumanoidArm.RIGHT ? rightUpperArm : leftUpperArm;
            ModelPart fore = player.getMainArm() == HumanoidArm.RIGHT ? rightForearm : leftForearm;
            upper.xRot = upper.xRot * 0.5F - Mth.PI / 10.0F;
            fore.xRot -= 0.10F;
            upper.yRot = 0.0F;
        }
        if (!player.getOffhandItem().isEmpty()) {
            ModelPart upper = player.getMainArm() == HumanoidArm.RIGHT ? leftUpperArm : rightUpperArm;
            ModelPart fore = player.getMainArm() == HumanoidArm.RIGHT ? leftForearm : rightForearm;
            upper.xRot = upper.xRot * 0.5F - Mth.PI / 10.0F;
            fore.xRot -= 0.10F;
            upper.yRot = 0.0F;
        }
    }

    private void applyUseItemPose(T player) {
        if (!player.isUsingItem()) return;
        HumanoidArm mainArm = player.getMainArm();
        boolean useRight = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? mainArm == HumanoidArm.RIGHT
                : mainArm == HumanoidArm.LEFT;
        poseArm(useRight ? rightUpperArm : leftUpperArm,
                useRight ? rightForearm : leftForearm,
                useRight, player.getUseItem());
    }

    private static void poseArm(ModelPart upper, ModelPart forearm, boolean right, ItemStack stack) {
        UseAnim anim = stack.getUseAnimation();
        switch (anim) {
            case BLOCK -> {
                upper.xRot = upper.xRot * 0.5F - 0.85F;
                upper.yRot = (right ? -1.0F : 1.0F) * 0.45F;
                forearm.xRot = -0.65F;
            }
            case BOW, CROSSBOW -> {
                upper.yRot = right ? -0.18F : 0.18F;
                upper.xRot = -1.35F;
                forearm.xRot = -0.45F;
            }
            case SPEAR -> {
                upper.xRot = upper.xRot * 0.5F - Mth.PI;
                forearm.xRot = -0.45F;
            }
            case DRINK, EAT -> {
                upper.xRot = upper.xRot * 0.5F - 1.0F;
                upper.yRot = (right ? -1.0F : 1.0F) * 0.20F;
                forearm.xRot = -0.40F;
            }
            default -> {
            }
        }
    }

    private void animateAttack(T player) {
        if (attackTime <= 0.0F || player.isUsingItem()) return;

        boolean left = player.getMainArm() == HumanoidArm.LEFT;
        ModelPart upper = left ? leftUpperArm : rightUpperArm;
        ModelPart fore = left ? leftForearm : rightForearm;

        float bodySwing = Mth.sin(Mth.sqrt(attackTime) * (Mth.PI * 2.0F)) * 0.18F;
        if (left) bodySwing = -bodySwing;
        torso.yRot += bodySwing;

        float eased = 1.0F - attackTime;
        eased *= eased;
        eased *= eased;
        eased = 1.0F - eased;
        float sweep = Mth.sin(eased * Mth.PI);
        float headCompensation = Mth.sin(attackTime * Mth.PI)
                * -(head.xRot - 0.65F) * 0.65F;

        upper.xRot -= sweep * 1.15F + headCompensation;
        upper.yRot += bodySwing * 1.8F;
        upper.zRot += Mth.sin(attackTime * Mth.PI) * -0.32F;
        fore.xRot -= sweep * 0.35F;
        jaw.xRot += Mth.sin(attackTime * Mth.PI) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, int color) {
        root.render(poseStack, consumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart getHead() {
        return head;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        torso.translateAndRotate(poseStack);
        ModelPart upper = arm == HumanoidArm.RIGHT ? rightUpperArm : leftUpperArm;
        ModelPart forearm = arm == HumanoidArm.RIGHT ? rightForearm : leftForearm;
        upper.translateAndRotate(poseStack);
        forearm.translateAndRotate(poseStack);
    }
}
