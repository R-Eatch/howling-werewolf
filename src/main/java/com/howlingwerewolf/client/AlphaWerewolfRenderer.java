package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class AlphaWerewolfRenderer extends WerewolfNpcRenderer<AlphaWerewolfEntity> {
    public AlphaWerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, WerewolfForm.BEAST, 0.9F);
    }

    @Override protected void scale(HumanoidRenderState state, PoseStack pose) { pose.scale(1.06F, 1.06F, 1.06F); }
}
