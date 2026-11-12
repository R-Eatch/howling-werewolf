package com.howlingwerewolf.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * A long-coated monster hunter on the vanilla humanoid skeleton, so poses, held items and armor work
 * as usual. The upper 64x32 holds the body; the lower half holds the hat, the high collar and the coat tails.
 */
public final class HunterModel extends HumanoidModel<HunterRenderState> {
    private final ModelPart frontTail;
    private final ModelPart backTail;

    public HunterModel(ModelPart root) {
        super(root);
        frontTail = body.getChild("front_tail");
        backTail = body.getChild("back_tail");
        hat.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("hat_crown", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -11.0F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
        head.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(0, 32)
                .addBox(-7.0F, -7.6F, -7.0F, 14.0F, 1.0F, 14.0F), PartPose.ZERO);
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-4.5F, -2.5F, -2.5F, 9.0F, 3.0F, 5.0F), PartPose.ZERO);
        // Hung from the waist, just outside the body box, so they swing clear of the legs.
        body.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(28, 48)
                .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 8.0F, 1.0F), PartPose.offset(0.0F, 12.0F, -2.1F));
        body.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(46, 48)
                .addBox(-4.0F, 0.0F, 0.0F, 8.0F, 10.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 2.1F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(HunterRenderState state) {
        super.setupAnim(state);
        hat.visible = false;
        // The tails follow whichever leg pushes into them: forward swings lift the front, backward the back.
        frontTail.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot));
        backTail.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot));
    }
}
