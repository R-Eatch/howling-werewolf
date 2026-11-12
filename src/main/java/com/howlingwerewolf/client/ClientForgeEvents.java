package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.network.ModNetwork;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientForgeEvents {
    private static WerewolfPlayerRenderer renderer;
    private static BeastPlayerRenderer beastRenderer;
    private static QuadrupedWerewolfRenderer quadrupedRenderer;
    private static WerewolfEquipmentRenderer equipmentRenderer;
    private static EntityRendererProvider.Context rendererContext;
    private static final java.util.Map<WerewolfForm, ModernPlayerRenderer> MODERN = new java.util.EnumMap<>(WerewolfForm.class);
    private static int modernRevision = -1;
    private static ResourceKey<Level> lastClientDimension;
    private static int dimensionResyncTicks = -1;
    private static int secondDimensionResyncTicks = -1;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            ModNetwork.CHANNEL.sendToServer(new RequestWerewolfSyncPacket());
        }
        if (secondDimensionResyncTicks >= 0 && --secondDimensionResyncTicks == 0) {
            ModNetwork.CHANNEL.sendToServer(new RequestWerewolfSyncPacket());
        }
        ClientPacketHandlers.applyPendingSyncs();
        while (ClientModEvents.OPEN_PROGRESSION.consumeClick()) {
            minecraft.setScreen(new WerewolfProgressionScreen());
        }
        while (ClientModEvents.TRANSFORM.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new TransformRequestPacket());
        }
        while (ClientModEvents.BEAST_MODE.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ToggleBeastModePacket());
        }
        while (ClientModEvents.QUADRUPED_MODE.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ToggleQuadrupedModePacket());
        }
        while (ClientModEvents.NIGHT_VISION.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ToggleNightVisionPacket());
        }
        while (ClientModEvents.SUMMON_WOLF_SPIRIT.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new UseAbilityPacket(WerewolfAbility.SUMMON_WOLF_SPIRIT));
        }
        while (ClientModEvents.BLOODY_BITE.consumeClick()) useBloodyBite();
        while (ClientModEvents.MOONBLOOD_SURGE.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new UseAbilityPacket(WerewolfAbility.MOONBLOOD_SURGE));
        }
        while (ClientModEvents.OPEN_COMPANIONS.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new com.howlingwerewolf.network.CompanionActionPacket(
                    com.howlingwerewolf.network.CompanionActionPacket.Action.OPEN,
                    com.howlingwerewolf.network.CompanionActionPacket.NO_TARGET));
        }
    }

    static void useBloodyBite() {
        Minecraft minecraft = Minecraft.getInstance();
        int targetId = minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity ? hit.getEntity().getId() : -1;
        ModNetwork.CHANNEL.sendToServer(new UseAbilityPacket(WerewolfAbility.BLOODY_BITE, targetId));
    }

    @SubscribeEvent
    public static void renderWerewolfPlayer(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        WerewolfForm form = WerewolfSkinRenderContext.getForm(player);
        if (form == WerewolfForm.HUMAN) return;

        event.setCanceled(true);
        // Another mod is drawing our body in first person: a werewolf body would fill the camera.
        if (FirstPersonBodyCompat.active(null)) return;
        if (renderer == null || beastRenderer == null || quadrupedRenderer == null
                || equipmentRenderer == null) {
            Minecraft minecraft = Minecraft.getInstance();
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                    minecraft.getEntityRenderDispatcher(), minecraft.getItemRenderer(), minecraft.getBlockRenderer(),
                    minecraft.getEntityRenderDispatcher().getItemInHandRenderer(), minecraft.getResourceManager(),
                    minecraft.getEntityModels(), minecraft.font);
            rendererContext = context;
            renderer = new WerewolfPlayerRenderer(context);
            beastRenderer = new BeastPlayerRenderer(context);
            quadrupedRenderer = new QuadrupedWerewolfRenderer(context);
            equipmentRenderer = new WerewolfEquipmentRenderer(context);
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        try {
            if (WerewolfSkinRenderContext.getModelSet(player) == com.howlingwerewolf.WerewolfModelSet.MODERN) {
                if (modernRevision != ModernModelResources.revision()) {
                    MODERN.clear();
                    modernRevision = ModernModelResources.revision();
                }
                MODERN.computeIfAbsent(form, selected -> new ModernPlayerRenderer(rendererContext, selected))
                        .render(player, player.getYRot(), event.getPartialTick(), poseStack,
                                event.getMultiBufferSource(), event.getPackedLight());
                return;
            }
            if (form == WerewolfForm.BEAST) {
                beastRenderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                        event.getMultiBufferSource(), event.getPackedLight());
            } else if (form == WerewolfForm.QUADRUPED) {
                quadrupedRenderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                        event.getMultiBufferSource(), event.getPackedLight());
            } else {
                renderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                        event.getMultiBufferSource(), event.getPackedLight());
                boolean armoredInstinct = WerewolfSkinRenderContext.isPreview(player) || WerewolfApi.get(player)
                        .map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT) && data.showsEquipment()).orElse(false);
                if (armoredInstinct && HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) {
                    equipmentRenderer.prepare(player, event.getRenderer(), event.getPartialTick());
                    equipmentRenderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                            event.getMultiBufferSource(), event.getPackedLight());
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void renderWerewolfArm(RenderArmEvent event) {
        WerewolfApi.get(event.getPlayer()).ifPresent(data -> {
            if (!data.isTransformed()) return;
            if (data.isQuadrupedMode()) event.setCanceled(true);
            else if (data.getModelSet() == com.howlingwerewolf.WerewolfModelSet.MODERN) ModernFirstPersonClawRenderer.render(event, data);
            else WerewolfFirstPersonClawRenderer.render(event, data.isBeastMode());
        });
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
        renderWerewolfArm(new RenderArmEvent(pose, event.getMultiBufferSource(), event.getPackedLight(), player, arm));
        pose.popPose();
    }

    private ClientForgeEvents() {}
}
