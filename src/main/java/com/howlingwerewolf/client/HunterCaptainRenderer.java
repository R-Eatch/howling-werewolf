package com.howlingwerewolf.client;

import com.howlingwerewolf.entity.SettlementLeaderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;

public final class HunterCaptainRenderer extends WerewolfLivingRenderer<SettlementLeaderEntity, HunterRenderState, HunterModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("howlingwerewolf", "textures/entity/hunter_captain.png");

    public HunterCaptainRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel(context.bakeLayer(ClientModEvents.HUNTER_LAYER)), 0.5F, HunterRenderState::new);
        addLayer(new ItemInHandLayer<>(this));
    }

    @Override public Identifier getTextureLocation(HunterRenderState state) { return TEXTURE; }
}
