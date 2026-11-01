package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class AlphaWerewolfRenderer extends WerewolfNpcRenderer<AlphaWerewolfEntity> {
    public AlphaWerewolfRenderer(EntityRendererProvider.Context context) { super(context, WerewolfForm.BEAST, 0.85F); }
}
