package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public final class TribeChiefRenderer extends WerewolfNpcRenderer<SettlementLeaderEntity> {
    public TribeChiefRenderer(EntityRendererProvider.Context context) {
        super(context, WerewolfForm.WEREWOLF, 0.6F);
        addLayer(new Armor(this, context));
    }

    private static final class Armor extends RenderLayer<SettlementLeaderEntity, EntityModel<SettlementLeaderEntity>> {
        private final TribeChiefRenderer chief;
        private final EntityRendererProvider.Context context;
        private final HumanoidModel<SettlementLeaderEntity> carrier;
        private final HumanoidArmorLayer<SettlementLeaderEntity, HumanoidModel<SettlementLeaderEntity>, HumanoidModel<SettlementLeaderEntity>> classic;
        private HumanoidArmorLayer<SettlementLeaderEntity, HumanoidModel<SettlementLeaderEntity>, HumanoidModel<SettlementLeaderEntity>> fitted;
        private ModernPlayerModel<SettlementLeaderEntity> fittedTo;

        Armor(TribeChiefRenderer chief, EntityRendererProvider.Context context) {
            super(chief);
            this.chief = chief;
            this.context = context;
            carrier = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER));
            classic = armor(new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                    new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)));
        }

        private HumanoidArmorLayer<SettlementLeaderEntity, HumanoidModel<SettlementLeaderEntity>, HumanoidModel<SettlementLeaderEntity>>
                armor(HumanoidModel<SettlementLeaderEntity> inner, HumanoidModel<SettlementLeaderEntity> outer) {
            var parent = new RenderLayerParent<SettlementLeaderEntity, HumanoidModel<SettlementLeaderEntity>>() {
                @Override public HumanoidModel<SettlementLeaderEntity> getModel() { return carrier; }
                @Override public ResourceLocation getTextureLocation(SettlementLeaderEntity entity) { return chief.getTextureLocation(entity); }
            };
            return new HumanoidArmorLayer<>(parent, inner, outer, context.getModelManager()) {
                @Override protected void setPartVisibility(HumanoidModel<SettlementLeaderEntity> model, EquipmentSlot slot) {
                    super.setPartVisibility(model, slot);
                    if (model instanceof ModernEquipmentLayer.RigArmorModel<?> rig) rig.slot = slot;
                }
            };
        }

        @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, SettlementLeaderEntity entity,
                                     float limb, float speed, float partial, float age, float yaw, float pitch) {
            var model = chief.modernModel();
            if (model == null) {
                carrier.attackTime = getParentModel().attackTime;
                carrier.prepareMobModel(entity, limb, speed, partial);
                carrier.setupAnim(entity, limb, speed, age, yaw, pitch);
                classic.render(pose, buffers, light, entity, limb, speed, partial, age, yaw, pitch);
                return;
            }
            if (fittedTo != model) {
                fittedTo = model;
                fitted = armor(new ModernEquipmentLayer.RigArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR), model, true),
                        new ModernEquipmentLayer.RigArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR), model, false));
            }
            fitted.render(pose, buffers, light, entity, limb, speed, partial, age, yaw, pitch);
        }
    }
}
