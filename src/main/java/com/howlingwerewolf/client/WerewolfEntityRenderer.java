package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.WerewolfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public final class WerewolfEntityRenderer extends WerewolfLivingRenderer<WerewolfEntity, HumanoidRenderState, WerewolfPlayerModel<HumanoidRenderState>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/werewolf_entity.png");
    public WerewolfEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfPlayerModel<>(WerewolfPlayerModel.createBodyLayer().bakeRoot()), 0.60F, HumanoidRenderState::new);
    }
    @Override public Identifier getTextureLocation(HumanoidRenderState state) { return TEXTURE; }
}
