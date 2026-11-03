package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.entity.AlphaMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class AlphaMinionRenderer extends WerewolfNpcRenderer<AlphaMinionEntity> {
    public AlphaMinionRenderer(EntityRendererProvider.Context context) { super(context, WerewolfForm.QUADRUPED, 0.5F); }
}
