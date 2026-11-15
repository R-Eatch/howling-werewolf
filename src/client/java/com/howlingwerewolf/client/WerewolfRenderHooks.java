package com.howlingwerewolf.client;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.EnumMap;
import java.util.Map;

/** Adapts the vanilla extraction and submission phases to the transformed avatar models. */
public final class WerewolfRenderHooks {
    private static EntityRendererProvider.Context rendererContext;
    private static WerewolfPlayerRenderer werewolf;
    private static BeastPlayerRenderer beast;
    private static QuadrupedWerewolfRenderer quadruped;
    private static WerewolfEquipmentRenderer equipment;
    private static final Map<WerewolfForm, ModernPlayerRenderer> MODERN = new EnumMap<>(WerewolfForm.class);
    private static int modernRevision = -1;

    static void initialize(EntityRendererProvider.Context context) {
        rendererContext = context;
        werewolf = new WerewolfPlayerRenderer(context);
        beast = new BeastPlayerRenderer(context);
        quadruped = new QuadrupedWerewolfRenderer(context);
        equipment = new WerewolfEquipmentRenderer(context);
        MODERN.clear();
    }

    public static void extract(AbstractClientPlayer player, AvatarRenderState state, float partialTick) {
        var data = WerewolfRenderData.forEntity(player, partialTick);
        if (data.form() == WerewolfForm.QUADRUPED && rendererContext != null) {
            rendererContext.getItemModelResolver().updateForLiving(data.mouthItem(), player.getMainHandItem(),
                    ItemDisplayContext.GROUND, player);
        }
        WerewolfRenderData.set(state, data);
        if (data.preview()) {
            state.nameTag = null;
            state.scoreText = null;
        }
    }

    /** Returns whether this avatar's vanilla submission has been replaced. */
    public static boolean submit(AvatarRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        var data = WerewolfRenderData.get(state);
        var form = data.form();
        if (form == WerewolfForm.HUMAN || rendererContext == null) return false;
        if (FirstPersonBodyCompat.active(state)) return true;
        pose.pushPose();
        try {
            if (data.modelSet() == WerewolfModelSet.MODERN) {
                if (modernRevision != ModernModelResources.revision()) {
                    MODERN.clear();
                    modernRevision = ModernModelResources.revision();
                }
                MODERN.computeIfAbsent(form, selected -> new ModernPlayerRenderer(rendererContext, selected))
                        .submit(state, pose, collector, camera);
            } else {
                switch (form) {
                    case BEAST -> beast.submit(state, pose, collector, camera);
                    case QUADRUPED -> quadruped.submit(state, pose, collector, camera);
                    default -> {
                        werewolf.submit(state, pose, collector, camera);
                        if (data.armoredInstinct() && HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) {
                            equipment.submit(state, pose, collector, camera);
                        }
                    }
                }
            }
        } finally {
            pose.popPose();
        }
        return true;
    }

    public static void renderArm(ArmRenderContext context, AvatarRenderState state) {
        var data = WerewolfRenderData.get(state);
        if (data.form() == WerewolfForm.HUMAN) return;
        if (data.form() == WerewolfForm.QUADRUPED) context.setCanceled(true);
        else if (data.modelSet() == WerewolfModelSet.MODERN) ModernFirstPersonClawRenderer.render(context, data);
        else WerewolfFirstPersonClawRenderer.render(context, data);
    }

    private WerewolfRenderHooks() {}
}
