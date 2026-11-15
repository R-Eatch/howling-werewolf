package com.howlingwerewolf.client;

import com.howlingwerewolf.entity.SettlementLeaderEntity;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public final class HunterCaptainRenderer extends HumanoidMobRenderer<SettlementLeaderEntity, HunterModel<SettlementLeaderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("howlingwerewolf", "textures/entity/hunter_captain.png");

    public HunterCaptainRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel<>(context.bakeLayer(ClientModEvents.HUNTER_LAYER)), 0.5F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override public ResourceLocation getTextureLocation(SettlementLeaderEntity entity) { return TEXTURE; }
}
