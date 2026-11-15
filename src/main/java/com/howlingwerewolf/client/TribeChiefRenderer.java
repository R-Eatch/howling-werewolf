package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class TribeChiefRenderer extends WerewolfNpcRenderer<SettlementLeaderEntity> {
    public TribeChiefRenderer(EntityRendererProvider.Context context) {
        super(context, WerewolfForm.WEREWOLF, 0.6F);
        var carrier = new HumanoidModel<HumanoidRenderState>(context.bakeLayer(ModelLayers.PLAYER));
        var classic = new HumanoidArmorLayer<HumanoidRenderState, HumanoidModel<HumanoidRenderState>, HumanoidModel<HumanoidRenderState>>(
                () -> carrier,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer());
        var modern = new ModernEquipmentLayer<>(this::modernModel, context);
        addLayer(new RenderLayer<HumanoidRenderState, EntityModel<HumanoidRenderState>>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, HumanoidRenderState state,
                               float yRot, float xRot) {
                if (WerewolfRenderData.get(state).modelSet() == WerewolfModelSet.MODERN) {
                    modern.submitArmor(pose, collector, light, state);
                } else {
                    classic.submit(pose, collector, light, state, yRot, xRot);
                }
            }
        });
    }
}
