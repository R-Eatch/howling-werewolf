package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
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
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;
    private static WerewolfPlayerRenderer renderer;
    private static BeastPlayerRenderer beastRenderer;
    private static QuadrupedWerewolfRenderer quadrupedRenderer;
    private static WerewolfEquipmentRenderer equipmentRenderer;
    private static final java.util.Map<WerewolfForm, ModernPlayerRenderer> modernRenderers = new java.util.EnumMap<>(WerewolfForm.class);
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
        while (ClientModEvents.OPEN_COMPANIONS.consumeClick()) {
            ClientPacketDistributor.sendToServer(new com.howlingwerewolf.network.CompanionActionPacket(
                    com.howlingwerewolf.network.CompanionActionPacket.Action.OPEN,
                    com.howlingwerewolf.network.CompanionActionPacket.NO_TARGET));
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
        for (WerewolfForm form : ModernModelResources.FORMS) modernRenderers.put(form, new ModernPlayerRenderer(context, form));
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
        // Another mod is drawing our body in first person: a werewolf body would fill the camera.
        if (FirstPersonBodyCompat.active(state)) return;
        var camera = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
        if (camera == null) camera = new net.minecraft.client.renderer.state.level.CameraRenderState();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        try {
            if (data.modelSet() == WerewolfModelSet.MODERN) {
                modernRenderers.get(form).submit(state, pose, event.getSubmitNodeCollector(), camera);
                return;
            }
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
        else if (data.modelSet() == WerewolfModelSet.MODERN) ModernFirstPersonClawRenderer.render(event, data);
        else WerewolfFirstPersonClawRenderer.render(event, data);
    }

    /**
     * The empty main hand of a transformed player is drawn here, a step before PlayerRenderer.renderRightHand.
     * Player-model mods such as MCA cancel that method at its head to draw their own arm, which also skips Forge's
     * RenderArmEvent; this keeps vanilla's empty-hand placement and hands the arm to {@link #renderWerewolfArm}.
     */
    @SubscribeEvent
    public static void renderWerewolfHand(RenderHandEvent event) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND || !event.getItemStack().isEmpty()
                || player == null || player.isInvisible() || player.isScoping()) return;
        if (!WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false)) return;
        event.setCanceled(true);
        net.minecraft.world.entity.HumanoidArm arm = player.getMainArm();
        float side = arm == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float swing = event.getSwingProgress(), root = net.minecraft.util.Mth.sqrt(swing);
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        // ItemInHandRenderer.renderPlayerArm, unchanged.
        pose.translate(side * (-0.3F * net.minecraft.util.Mth.sin(root * (float) Math.PI) + 0.64000005F),
                0.4F * net.minecraft.util.Mth.sin(root * (float) (Math.PI * 2)) - 0.6F - event.getEquipProgress() * 0.6F,
                -0.4F * net.minecraft.util.Mth.sin(swing * (float) Math.PI) - 0.71999997F);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(side * 45.0F));
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(side * net.minecraft.util.Mth.sin(root * (float) Math.PI) * 70.0F));
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(side * net.minecraft.util.Mth.sin(swing * swing * (float) Math.PI) * -20.0F));
        pose.translate(side * -1.0F, 3.6F, 3.5F);
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(side * 120.0F));
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(side * -135.0F));
        pose.translate(side * 5.6F, 0.0F, 0.0F);
        renderWerewolfArm(new RenderArmEvent(pose, event.getSubmitNodeCollector(), event.getPackedLight(), player, arm));
        pose.popPose();
    }

    private ClientForgeEvents() {}
}
