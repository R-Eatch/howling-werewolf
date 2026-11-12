package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.HunterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;

public final class HunterRenderer extends WerewolfLivingRenderer<HunterEntity, HunterRenderState, HunterModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/hunter.png");
    private static final Identifier WATCHER_TEXTURE = Identifier.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/hunter_watcher.png");

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel(context.bakeLayer(ClientModEvents.HUNTER_LAYER)), 0.5F, HunterRenderState::new);
        addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public void extractRenderState(HunterEntity entity, HunterRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.trialHunter = entity.isTrialHunter();
    }

    @Override
    public Identifier getTextureLocation(HunterRenderState state) {
        return state.trialHunter ? WATCHER_TEXTURE : TEXTURE;
    }
}
