package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;
    private static WerewolfPlayerRenderer renderer;
    private static BeastPlayerRenderer beastRenderer;
    private static QuadrupedWerewolfRenderer quadrupedRenderer;
    private static WerewolfEquipmentRenderer equipmentRenderer;
    private static ResourceKey<Level> lastClientDimension;
    private static int dimensionResyncTicks = -1;
    private static int secondDimensionResyncTicks = -1;

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            lastClientDimension = null;
            dimensionResyncTicks = -1;
            secondDimensionResyncTicks = -1;
            return;
        }

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
            ClientPacketDistributor.sendToServer(new RequestWerewolfSyncPacket());
        }
        if (secondDimensionResyncTicks >= 0 && --secondDimensionResyncTicks == 0) {
            ClientPacketDistributor.sendToServer(new RequestWerewolfSyncPacket());
        }
        ClientPacketHandlers.applyPendingSyncs();
        while (ClientModEvents.OPEN_PROGRESSION.consumeClick()) {
            minecraft.setScreen(new WerewolfProgressionScreen());
        }
        while (ClientModEvents.TRANSFORM.consumeClick()) {
            ClientPacketDistributor.sendToServer(new TransformRequestPacket());
        }
        while (ClientModEvents.BEAST_MODE.consumeClick()) {
            ClientPacketDistributor.sendToServer(new ToggleBeastModePacket());
        }
        while (ClientModEvents.QUADRUPED_MODE.consumeClick()) {
            ClientPacketDistributor.sendToServer(new ToggleQuadrupedModePacket());
        }
        while (ClientModEvents.NIGHT_VISION.consumeClick()) {
            ClientPacketDistributor.sendToServer(new ToggleNightVisionPacket());
        }
        while (ClientModEvents.SUMMON_WOLF_SPIRIT.consumeClick()) {
            ClientPacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.SUMMON_WOLF_SPIRIT));
        }
        while (ClientModEvents.BLOODY_BITE.consumeClick()) useBloodyBite();
        while (ClientModEvents.MOONBLOOD_SURGE.consumeClick()) {
            ClientPacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.MOONBLOOD_SURGE));
        }
    }

    static void useBloodyBite() {
        Minecraft minecraft = Minecraft.getInstance();
        int targetId = minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity ? hit.getEntity().getId() : -1;
        ClientPacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.BLOODY_BITE, targetId));
    }

    static void initializeRenderers(EntityRendererProvider.Context context) {
        renderer = new WerewolfPlayerRenderer(context);
        beastRenderer = new BeastPlayerRenderer(context);
        quadrupedRenderer = new QuadrupedWerewolfRenderer(context);
        equipmentRenderer = new WerewolfEquipmentRenderer(context);
        itemModelResolver = context.getItemModelResolver();
    }

    static void extractMouthItem(AbstractClientPlayer player,
                                 net.minecraft.client.renderer.item.ItemStackRenderState item) {
        if (itemModelResolver != null) itemModelResolver.updateForLiving(item, player.getMainHandItem(),
                net.minecraft.world.item.ItemDisplayContext.GROUND, player);
    }

    @SubscribeEvent
    public static void renderWerewolfPlayer(RenderPlayerEvent.Pre<?> event) {
        var state = event.getRenderState();
        var data = WerewolfRenderData.get(state);
        WerewolfForm form = data.form();
        if (form == WerewolfForm.HUMAN || renderer == null) return;
        event.setCanceled(true);
        var camera = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
        if (camera == null) camera = new net.minecraft.client.renderer.state.level.CameraRenderState();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        try {
            switch (form) {
                case BEAST -> beastRenderer.submit(state, pose, event.getSubmitNodeCollector(), camera);
                case QUADRUPED -> quadrupedRenderer.submit(state, pose, event.getSubmitNodeCollector(), camera);
                default -> {
                    renderer.submit(state, pose, event.getSubmitNodeCollector(), camera);
                    if (data.armoredInstinct() && HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) {
                        equipmentRenderer.submit(state, pose, event.getSubmitNodeCollector(), camera);
                    }
                }
            }
        } finally {
            pose.popPose();
        }
    }

    @SubscribeEvent
    public static void renderWerewolfArm(RenderArmEvent event) {
        var data = WerewolfRenderData.forEntity(event.getPlayer());
        if (data.form() == WerewolfForm.HUMAN) return;
        if (data.form() == WerewolfForm.QUADRUPED) event.setCanceled(true);
        else WerewolfFirstPersonClawRenderer.render(event, data);
    }

    private ClientForgeEvents() {}
}
