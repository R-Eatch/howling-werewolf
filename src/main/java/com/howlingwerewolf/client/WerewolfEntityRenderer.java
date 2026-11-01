package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class WerewolfEntityRenderer extends WerewolfNpcRenderer<WerewolfEntity> {
    public WerewolfEntityRenderer(EntityRendererProvider.Context context) {
        super(context, WerewolfForm.WEREWOLF, 0.60F);
    }
}
