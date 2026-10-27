package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public final class AlphaWerewolfRenderer extends WerewolfLivingRenderer<AlphaWerewolfEntity, HumanoidRenderState, BeastPlayerModel<HumanoidRenderState>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            HowlingWerewolf.MOD_ID, "textures/entity/alpha_werewolf.png");
    public AlphaWerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastPlayerModel<>(BeastPlayerModel.createBodyLayer().bakeRoot()), 0.9F, HumanoidRenderState::new);
    }
    @Override public Identifier getTextureLocation(HumanoidRenderState state) { return TEXTURE; }

    @Override protected void scale(HumanoidRenderState state, PoseStack pose) { pose.scale(1.06F, 1.06F, 1.06F); }
}
