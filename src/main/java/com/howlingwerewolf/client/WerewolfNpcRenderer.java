package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.entity.WerewolfNpcAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** The server's tracked appearance is the only NPC model selector. */
class WerewolfNpcRenderer<T extends LivingEntity & WerewolfNpcAppearance> extends LivingEntityRenderer<T, EntityModel<T>> {
    private final EntityModel<T> classic;
    private ModernPlayerModel<T> modern;
    private final WerewolfForm form;
    private int revision = -1;

    WerewolfNpcRenderer(EntityRendererProvider.Context context, WerewolfForm form, float shadow) {
        super(context, classicModel(form), shadow);
        classic = model;
        this.form = form;
    }

    private static <T extends LivingEntity> EntityModel<T> classicModel(WerewolfForm form) {
        return switch (form) {
            case BEAST -> new BeastPlayerModel<>(BeastPlayerModel.createBodyLayer().bakeRoot());
            case QUADRUPED -> new QuadrupedWerewolfModel<>(QuadrupedWerewolfModel.createBodyLayer().bakeRoot());
            default -> new WerewolfPlayerModel<>(WerewolfPlayerModel.createBodyLayer().bakeRoot());
        };
    }

    @Override public void render(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (entity.getNpcModelSet() == WerewolfModelSet.MODERN) {
            if (revision != ModernModelResources.revision()) {
                modern = new ModernPlayerModel<>(form);
                revision = ModernModelResources.revision();
            }
            model = modern;
        } else model = classic;
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(T entity) {
        return WerewolfSkinCatalog.texture(entity.getNpcSkinId(), entity.getNpcModelSet(), form);
    }
}
