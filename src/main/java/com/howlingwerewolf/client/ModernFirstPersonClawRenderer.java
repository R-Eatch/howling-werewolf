package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RenderArmEvent;

import java.util.EnumMap;
import java.util.Map;

/** Uses the actual imported arm and paw geometry with the selected Modern palette. */
final class ModernFirstPersonClawRenderer {
    private static final Map<WerewolfForm, ModernPlayerModel<net.minecraft.client.renderer.entity.state.AvatarRenderState>> ARMS = new EnumMap<>(WerewolfForm.class);
    private static int revision = -1;

    static void render(RenderArmEvent event, WerewolfRenderData data) {
        if (revision != ModernModelResources.revision()) {
            ARMS.clear();
            revision = ModernModelResources.revision();
        }
        ModernPlayerModel<net.minecraft.client.renderer.entity.state.AvatarRenderState> model = ARMS.computeIfAbsent(data.form(), ModernPlayerModel::new);
        model.resetPose();
        boolean right = event.getArm() == HumanoidArm.RIGHT;
        ModelPart arm = model.rig.bone(right ? "rightArmRotation" : "leftArmRotation");
        arm.setPos(right ? -5 : 5, 2, 0);
        arm.setRotation(0, 0, 0);
        float scale = data.form() == WerewolfForm.BEAST ? 0.65F : 0.9F;
        arm.xScale = arm.yScale = arm.zScale = scale;
        event.getSubmitNodeCollector().submitModelPart(arm, event.getPoseStack(), RenderTypes.entityCutout(
                WerewolfSkinCatalog.texture(data.skinId(), WerewolfModelSet.MODERN, data.form())),
                event.getPackedLight(), OverlayTexture.NO_OVERLAY, null);
        event.setCanceled(true);
    }

    private ModernFirstPersonClawRenderer() {}
}
