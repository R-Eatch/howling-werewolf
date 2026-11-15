package com.howlingwerewolf.client;

import com.howlingwerewolf.HWConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

/** Elytra follows the imported back once, including crouching and flight. */
final class ModernWingsLayer extends RenderLayer<AvatarRenderState, ModernPlayerModel<AvatarRenderState>> {
    private final EquipmentLayerRenderer equipment;
    private final BackElytraModel wings;
    private final BackElytraModel babyWings;
    private final float[] back;

    ModernWingsLayer(ModernPlayerRenderer parent, EntityRendererProvider.Context context) {
        super(parent);
        equipment = context.getEquipmentRenderer();
        wings = new BackElytraModel(context.bakeLayer(ModelLayers.ELYTRA));
        babyWings = new BackElytraModel(context.bakeLayer(ModelLayers.ELYTRA_BABY));
        back = ModernEquipmentLayer.attachmentBounds(getParentModel().rig.bone("ribs"));
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        if (!WerewolfRenderData.get(state).armoredInstinct() || !HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) return;
        var stack = state.chestEquipment;
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) return;
        Identifier texture = state.skin.elytra() != null ? state.skin.elytra().texturePath()
                : state.skin.cape() != null && state.showCape ? state.skin.cape().texturePath() : null;
        pose.pushPose();
        getParentModel().rig.transform("ribs", pose);
        pose.translate(0, back[1] / 16, back[5] / 16 + 0.03F);
        equipment.renderLayers(EquipmentClientInfo.LayerType.WINGS, equippable.assetId().get(),
                state.isBaby ? babyWings : wings, state, stack, pose, collector, light, texture, state.outlineColor, 0);
        pose.popPose();
    }

    static final class BackElytraModel extends ElytraModel {
        BackElytraModel(ModelPart root) { super(root); }
        @Override public void setupAnim(HumanoidRenderState state) {
            super.setupAnim(state);
            // The attachment already follows crouching hips and shoulders.
            root().getChild("left_wing").y = root().getChild("right_wing").y = 0;
        }
    }
}
