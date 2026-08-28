package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

/**
 * Project-local, vanilla-compatible wolf geometry with player-driven animation. Keeping the layer
 * definition here makes the four-legged model part of this mod's compiled JAR while retaining the
 * standard 64x32 wolf UV layout.
 */
public final class QuadrupedWerewolfModel extends EntityModel<AbstractClientPlayer> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart realHead;
    private final ModelPart body;
    private final ModelPart upperBody;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart realTail;

    public QuadrupedWerewolfModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.realHead = head.getChild("real_head");
        this.body = root.getChild("body");
        this.upperBody = root.getChild("upper_body");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = root.getChild("tail");
        this.realTail = tail.getChild("real_tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offset(-1.0F, 13.5F, -7.0F));
        head.addOrReplaceChild("real_head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F)
                        .texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3.0F, 3.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, Mth.HALF_PI, 0.0F, 0.0F));
        root.addOrReplaceChild("upper_body", CubeListBuilder.create()
                        .texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F),
                PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, Mth.HALF_PI, 0.0F, 0.0F));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 18)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);
        root.addOrReplaceChild("right_hind_leg", leg,
                PartPose.offset(-2.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leg,
                PartPose.offset(0.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", leg,
                PartPose.offset(-2.5F, 16.0F, -4.0F));
        root.addOrReplaceChild("left_front_leg", leg,
                PartPose.offset(0.5F, 16.0F, -4.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 8.0F));
        tail.addOrReplaceChild("real_tail", CubeListBuilder.create()
                        .texOffs(9, 18).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 8.0F, 2.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(AbstractClientPlayer player, float limbSwing, float limbAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        head.setPos(-1.0F, 13.5F, -7.0F);
        body.setPos(0.0F, 14.0F, 2.0F);
        body.xRot = Mth.HALF_PI;
        upperBody.setPos(-1.0F, 14.0F, -3.0F);
        upperBody.xRot = body.xRot;
        rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
        leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
        rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
        leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
        tail.setPos(0.0F, 12.0F, 8.0F);

        float speed = Mth.clamp(limbAmount, 0.0F, 1.0F);
        rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * speed;
        leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * speed;
        rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * speed;
        leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * speed;

        if (player.isCrouching()) {
            head.y += 1.0F;
            upperBody.y += 1.0F;
            body.y += 1.0F;
            tail.y += 1.0F;
        }
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        tail.yRot = Mth.sin(ageInTicks * 0.16F) * (0.18F + speed * 0.22F);
        tail.xRot = 0.75F + speed * 0.18F;
        realHead.zRot = Mth.sin(ageInTicks * 0.09F) * 0.025F;
        realTail.zRot = Mth.sin(ageInTicks * 0.16F) * 0.08F;
    }

    public void translateToMouth(PoseStack poseStack) {
        head.translateAndRotate(poseStack);
        realHead.translateAndRotate(poseStack);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
