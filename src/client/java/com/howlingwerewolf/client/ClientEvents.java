package com.howlingwerewolf.client;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.network.RequestWerewolfSyncPacket;
import com.howlingwerewolf.network.TransformRequestPacket;
import com.howlingwerewolf.network.ToggleBeastModePacket;
import com.howlingwerewolf.network.ToggleQuadrupedModePacket;
import com.howlingwerewolf.network.ToggleNightVisionPacket;
import com.howlingwerewolf.network.UseAbilityPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientEvents {
    private static EntityRendererProvider.Context rendererContext;
    private static int modernRevision = -1;
    private static WerewolfPlayerRenderer renderer;
    private static BeastPlayerRenderer beastRenderer;
    private static QuadrupedWerewolfRenderer quadrupedRenderer;
    private static WerewolfEquipmentRenderer equipmentRenderer;
    private static final java.util.Map<WerewolfForm, ModernPlayerRenderer> modernRenderers = new java.util.EnumMap<>(WerewolfForm.class);
    private static ResourceKey<Level> lastClientDimension;
    private static int dimensionResyncTicks = -1;
    private static int secondDimensionResyncTicks = -1;

    public static void clientTick(Minecraft minecraftUnused) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            lastClientDimension = null;
            dimensionResyncTicks = -1;
            secondDimensionResyncTicks = -1;
            return;
        }
        if (!ClientPlayNetworking.canSend(RequestWerewolfSyncPacket.TYPE)) return;

        ResourceKey<Level> currentDimension = minecraft.level.dimension();
        if (lastClientDimension == null || !lastClientDimension.equals(currentDimension)) {
            lastClientDimension = currentDimension;
            ClientPacketHandlers.clearPendingSyncs();
            // Server-side dimension events may sync before the client has finished swapping its
            // LocalPlayer/Level. Request authoritative state shortly afterwards, then once more
            // after one second as a safety net for slower integrated/dedicated servers.
            dimensionResyncTicks = 5;
            secondDimensionResyncTicks = 20;
        }
        if (dimensionResyncTicks >= 0 && --dimensionResyncTicks == 0) {
            ClientPlayNetworking.send(new RequestWerewolfSyncPacket());
        }
        if (secondDimensionResyncTicks >= 0 && --secondDimensionResyncTicks == 0) {
            ClientPlayNetworking.send(new RequestWerewolfSyncPacket());
        }
        ClientPacketHandlers.applyPendingSyncs();
        while (ClientModEvents.OPEN_PROGRESSION.consumeClick()) {
            minecraft.setScreen(new WerewolfProgressionScreen());
        }
        while (ClientModEvents.OPEN_COMPANIONS.consumeClick()) {
            ClientPlayNetworking.send(new com.howlingwerewolf.network.CompanionActionPacket(
                    com.howlingwerewolf.network.CompanionActionPacket.Action.OPEN,
                    com.howlingwerewolf.network.CompanionActionPacket.NO_TARGET));
        }
        while (ClientModEvents.TRANSFORM.consumeClick()) {
            ClientPlayNetworking.send(new TransformRequestPacket());
        }
        while (ClientModEvents.BEAST_MODE.consumeClick()) {
            ClientPlayNetworking.send(new ToggleBeastModePacket());
        }
        while (ClientModEvents.QUADRUPED_MODE.consumeClick()) {
            ClientPlayNetworking.send(new ToggleQuadrupedModePacket());
        }
        while (ClientModEvents.NIGHT_VISION.consumeClick()) {
            ClientPlayNetworking.send(new ToggleNightVisionPacket());
        }
        while (ClientModEvents.SUMMON_WOLF_SPIRIT.consumeClick()) {
            ClientPlayNetworking.send(new UseAbilityPacket(WerewolfAbility.SUMMON_WOLF_SPIRIT));
        }
        while (ClientModEvents.BLOODY_BITE.consumeClick()) useBloodyBite();
        while (ClientModEvents.MOONBLOOD_SURGE.consumeClick()) {
            ClientPlayNetworking.send(new UseAbilityPacket(WerewolfAbility.MOONBLOOD_SURGE));
        }
    }

    static void useBloodyBite() {
        Minecraft minecraft = Minecraft.getInstance();
        int targetId = minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity ? hit.getEntity().getId() : -1;
        ClientPlayNetworking.send(new UseAbilityPacket(WerewolfAbility.BLOODY_BITE, targetId));
    }

    private static void initializeRenderers() {
        Minecraft minecraft = Minecraft.getInstance();
        var dispatcher = minecraft.getEntityRenderDispatcher();
        rendererContext = new EntityRendererProvider.Context(dispatcher, minecraft.getItemRenderer(),
                minecraft.getBlockRenderer(), dispatcher.getItemInHandRenderer(),
                minecraft.getResourceManager(), minecraft.getEntityModels(), minecraft.font);
        renderer = new WerewolfPlayerRenderer(rendererContext);
        beastRenderer = new BeastPlayerRenderer(rendererContext);
        quadrupedRenderer = new QuadrupedWerewolfRenderer(rendererContext);
        equipmentRenderer = new WerewolfEquipmentRenderer(rendererContext);
    }

    public static boolean renderWerewolfPlayer(PlayerRenderer vanilla, AbstractClientPlayer player, float yaw,
                                                float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        WerewolfForm form = WerewolfSkinRenderContext.getForm(player);
        if (form == WerewolfForm.HUMAN) return false;
        // The transformed body would fill the camera during another mod's first-person pass.
        if (FirstPersonBodyCompat.active(null)) return true;
        if (rendererContext == null) initializeRenderers();

        pose.pushPose();
        try {
            if (WerewolfSkinRenderContext.getModelSet(player) == WerewolfModelSet.MODERN) {
                if (modernRevision != ModernModelResources.revision()) {
                    modernRenderers.clear();
                    modernRevision = ModernModelResources.revision();
                }
                modernRenderers.computeIfAbsent(form, selected -> new ModernPlayerRenderer(rendererContext, selected))
                        .render(player, yaw, partial, pose, buffers, light);
            } else if (form == WerewolfForm.BEAST) {
                beastRenderer.render(player, yaw, partial, pose, buffers, light);
            } else if (form == WerewolfForm.QUADRUPED) {
                quadrupedRenderer.render(player, yaw, partial, pose, buffers, light);
            } else {
                renderer.render(player, yaw, partial, pose, buffers, light);
                boolean armoredInstinct = WerewolfSkinRenderContext.isPreview(player) || WerewolfApi.get(player)
                        .map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT) && data.showsEquipment()).orElse(false);
                if (armoredInstinct && HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) {
                    equipmentRenderer.prepare(player, vanilla, partial);
                    equipmentRenderer.render(player, yaw, partial, pose, buffers, light);
                }
            }
        } finally {
            pose.popPose();
        }
        return true;
    }

    public static void renderWerewolfArm(ArmRenderContext event) {
        WerewolfApi.get(event.getPlayer()).ifPresent(data -> {
            if (!data.isTransformed()) return;
            if (data.isQuadrupedMode()) event.setCanceled(true);
            else if (data.getModelSet() == WerewolfModelSet.MODERN) ModernFirstPersonClawRenderer.render(event, data);
            else WerewolfFirstPersonClawRenderer.render(event, data.isBeastMode());
        });
    }

    private ClientEvents() {}
}
