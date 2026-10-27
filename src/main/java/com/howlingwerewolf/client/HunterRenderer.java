package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.HunterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;

public final class HunterRenderer extends WerewolfLivingRenderer<HunterEntity, HumanoidRenderState, HunterModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/hunter.png");

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel(context.bakeLayer(ClientModEvents.HUNTER_LAYER)), 0.5F, HumanoidRenderState::new);
        addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }
}
