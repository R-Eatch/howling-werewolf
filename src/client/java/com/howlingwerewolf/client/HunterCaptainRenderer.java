package com.howlingwerewolf.client;

import com.howlingwerewolf.entity.SettlementLeaderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;

public final class HunterCaptainRenderer extends WerewolfLivingRenderer<SettlementLeaderEntity, HunterRenderState, HunterModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("howlingwerewolf", "textures/entity/hunter_captain.png");

    public HunterCaptainRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel(context.bakeLayer(ClientModEvents.HUNTER_LAYER)), 0.5F, HunterRenderState::new);
        addLayer(new ItemInHandLayer<>(this));
        addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer()));
    }

    @Override public Identifier getTextureLocation(HunterRenderState state) { return TEXTURE; }
}
