package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.HunterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class HunterRenderer extends HumanoidMobRenderer<HunterEntity, HunterModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/hunter.png");
    /** The Silver-Oath Watcher of the Alpha Trial wears the same cut in silver-trimmed colors. */
    private static final ResourceLocation WATCHER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/hunter_watcher.png");

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel(context.bakeLayer(ClientModEvents.HUNTER_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HunterEntity entity) {
        return entity.isTrialHunter() ? WATCHER_TEXTURE : TEXTURE;
    }
}
