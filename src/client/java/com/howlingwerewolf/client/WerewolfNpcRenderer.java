package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Selects from server-synchronized entity data, never the viewer's settings. */
abstract class WerewolfNpcRenderer<T extends LivingEntity>
        extends WerewolfLivingRenderer<T, HumanoidRenderState, EntityModel<HumanoidRenderState>> {
    private final EntityModel<HumanoidRenderState> classic;
    private ModernPlayerModel<HumanoidRenderState> modern;
    private int modernRevision = -1;
    private final WerewolfForm form;

    WerewolfNpcRenderer(EntityRendererProvider.Context context, WerewolfForm form, float shadow) {
        super(context, classicModel(form), shadow, HumanoidRenderState::new);
        this.classic = model;
        this.form = form;
    }

    private static EntityModel<HumanoidRenderState> classicModel(WerewolfForm form) {
        return switch (form) {
            case BEAST -> new BeastPlayerModel<>(BeastPlayerModel.createBodyLayer().bakeRoot());
            case QUADRUPED -> new QuadrupedWerewolfModel<>(QuadrupedWerewolfModel.createBodyLayer().bakeRoot());
            default -> new WerewolfPlayerModel<>(WerewolfPlayerModel.createBodyLayer().bakeRoot());
        };
    }

    @Override public void extractRenderState(T entity, HumanoidRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        WerewolfRenderData.set(state, WerewolfRenderData.forEntity(entity, partialTick));
    }

    /** Mob name rules: no tag without a custom name, and a custom name shows when aimed at or marked visible. */
    @Override protected boolean shouldShowName(T entity, double distance) {
        return super.shouldShowName(entity, distance) && (entity.shouldShowName()
                || entity.hasCustomName() && entity == entityRenderDispatcher.crosshairPickEntity);
    }

    @Override public void submit(HumanoidRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (WerewolfRenderData.get(state).modelSet() == WerewolfModelSet.MODERN) {
            if (modernRevision != ModernModelResources.revision()) {
                modern = new ModernPlayerModel<>(form);
                modernRevision = ModernModelResources.revision();
            }
            model = modern;
        } else {
            model = classic;
        }
        super.submit(state, pose, collector, camera);
    }

    @Override public Identifier getTextureLocation(HumanoidRenderState state) {
        var data = WerewolfRenderData.get(state);
        return WerewolfSkinCatalog.texture(data.skinId(), data.modelSet(), form);
    }

    ModernPlayerModel<HumanoidRenderState> modernModel() { return modern; }
}
