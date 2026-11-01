package com.howlingwerewolf.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Supplier;

/** Extracts all animation inputs before the render submission phase. */
abstract class WerewolfLivingRenderer<T extends LivingEntity, S extends HumanoidRenderState, M extends EntityModel<S>>
        extends LivingEntityRenderer<T, S, M> {
    private final Supplier<S> stateFactory;

    protected WerewolfLivingRenderer(EntityRendererProvider.Context context, M model, float shadow, Supplier<S> stateFactory) {
        super(context, model, shadow);
        this.stateFactory = stateFactory;
    }

    // AvatarRenderState is reserved for players: the dispatcher routes that type to AvatarRenderer.
    @Override public S createRenderState() { return stateFactory.get(); }

    @Override
    public void extractRenderState(T entity, S state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, itemModelResolver);
    }
}
