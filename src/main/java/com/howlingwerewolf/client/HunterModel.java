package com.howlingwerewolf.client;

import com.howlingwerewolf.entity.HunterEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Steve-proportioned hunter using the complete 64x64 player-skin UV plus a low hat brim. */
public final class HunterModel extends PlayerModel<HunterEntity> {
    public HunterModel(ModelPart root) {
        super(root, false);
    }

    public static LayerDefinition createBodyLayer() {
        // PlayerModel supplies independent left/right limb UVs and all skin overlay layers.
        // PlayerModel 可完整使用 64x64 皮肤的独立左右肢体 UV 与全部外层纹理。
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("hat_brim",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-5.0F, -0.5F, -5.0F, 10.0F, 1.0F, 10.0F,
                                new CubeDeformation(0.05F)),
                PartPose.offset(0.0F, -6.7F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
