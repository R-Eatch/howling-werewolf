package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.capability.WerewolfData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.client.event.RenderArmEvent;
import java.util.EnumMap;
import java.util.Map;

final class ModernFirstPersonClawRenderer {
    private static final Map<WerewolfForm, ModernPlayerModel<net.minecraft.client.player.AbstractClientPlayer>> ARMS = new EnumMap<>(WerewolfForm.class);
    private static int revision = -1;

    static void render(RenderArmEvent event, WerewolfData data) {
        if (revision != ModernModelResources.revision()) {
            ARMS.clear();
            revision = ModernModelResources.revision();
        }
        ModernPlayerModel<?> model = ARMS.computeIfAbsent(data.getForm(), ModernPlayerModel::new);
        model.resetPose();
        boolean right = event.getArm() == HumanoidArm.RIGHT;
        ModelPart arm = model.rig.bone(right ? "rightArmRotation" : "leftArmRotation");
        arm.setPos(right ? -5 : 5, 2, 0);
        arm.setRotation(0, 0, 0);
        float scale = data.getForm() == WerewolfForm.BEAST ? 0.65F : 0.9F;
        arm.xScale = arm.yScale = arm.zScale = scale;
        BlockbenchModel.render(arm, event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.entityCutout(
                WerewolfSkinCatalog.texture(data.getSkinId(), WerewolfModelSet.MODERN, data.getForm()))),
                event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        event.setCanceled(true);
    }
}
