/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class CompanionRenderer extends WerewolfNpcRenderer<WerewolfCompanionEntity> {
    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, WerewolfForm.WEREWOLF, 0.60F);
        addLayer(new CompanionCarriedItemLayer(this));
    }
}
