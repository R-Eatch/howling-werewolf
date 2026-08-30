package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Reuses the player's normal werewolf rig for the biped mob. / 双足生物复用玩家普通狼人骨架。 */
public final class WerewolfEntityRenderer
        extends LivingEntityRenderer<WerewolfEntity, WerewolfPlayerModel<WerewolfEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/werewolf_entity.png");

    public WerewolfEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfPlayerModel<>(WerewolfPlayerModel.createBodyLayer().bakeRoot()), 0.60F);
    }

    @Override
    public ResourceLocation getTextureLocation(WerewolfEntity entity) {
        return TEXTURE;
    }
}
