package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RenderArmEvent;

public final class WerewolfFirstPersonClawRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "textures/entity/werewolf.png");
    private static final ResourceLocation BEAST_TEXTURE = ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "textures/entity/beast.png");
    private static final ModelPart ROOT = createLayer().bakeRoot();
    private static final ModelPart BEAST_ROOT = createBeastLayer().bakeRoot();

    public static void render(RenderArmEvent event, boolean beastMode) {
        ModelPart modelRoot = beastMode ? BEAST_ROOT : ROOT;
        ModelPart arm = modelRoot.getChild(event.getArm() == HumanoidArm.RIGHT ? "right_arm" : "left_arm");
        arm.resetPose();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(
                RenderType.entityCutoutNoCull(beastMode ? BEAST_TEXTURE : TEXTURE));
        arm.render(pose, consumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY);
        pose.popPose();
        event.setCanceled(true);
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                net.minecraft.client.model.geom.PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(18, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                net.minecraft.client.model.geom.PartPose.offset(5.0F, 2.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static LayerDefinition createBeastLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition right = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 11.0F, 5.0F),
                net.minecraft.client.model.geom.PartPose.offset(-5.5F, 2.0F, 0.0F));
        right.addOrReplaceChild("forearm", CubeListBuilder.create()
                        .texOffs(22, 60).addBox(-4.5F, 0.0F, -3.0F, 5.0F, 10.0F, 6.0F),
                net.minecraft.client.model.geom.PartPose.offsetAndRotation(-0.25F, 7.5F, 0.0F,
                        -8.0F * net.minecraft.util.Mth.DEG_TO_RAD, 0.0F, 0.0F));
        PartDefinition left = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(46, 60).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 11.0F, 5.0F),
                net.minecraft.client.model.geom.PartPose.offset(5.5F, 2.0F, 0.0F));
        left.addOrReplaceChild("forearm", CubeListBuilder.create()
                        .texOffs(68, 60).addBox(-0.5F, 0.0F, -3.0F, 5.0F, 10.0F, 6.0F),
                net.minecraft.client.model.geom.PartPose.offsetAndRotation(0.25F, 7.5F, 0.0F,
                        -8.0F * net.minecraft.util.Mth.DEG_TO_RAD, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    private WerewolfFirstPersonClawRenderer() {}
}
