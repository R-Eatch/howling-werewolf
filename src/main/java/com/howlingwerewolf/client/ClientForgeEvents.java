package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfAbility;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientForgeEvents {
    private static WerewolfPlayerRenderer renderer;
    private static BeastPlayerRenderer beastRenderer;
    private static QuadrupedWerewolfRenderer quadrupedRenderer;
    private static WerewolfEquipmentRenderer equipmentRenderer;
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
        boolean transformed = WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false);
        if (!transformed) return;

        event.setCanceled(true);
        if (renderer == null || beastRenderer == null || quadrupedRenderer == null
                || equipmentRenderer == null) {
            Minecraft minecraft = Minecraft.getInstance();
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                    minecraft.getEntityRenderDispatcher(), minecraft.getItemRenderer(), minecraft.getBlockRenderer(),
                    minecraft.getEntityRenderDispatcher().getItemInHandRenderer(), minecraft.getResourceManager(),
                    minecraft.getEntityModels(), minecraft.font);
            renderer = new WerewolfPlayerRenderer(context);
            beastRenderer = new BeastPlayerRenderer(context);
            quadrupedRenderer = new QuadrupedWerewolfRenderer(context);
            equipmentRenderer = new WerewolfEquipmentRenderer(context);
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        boolean beast = WerewolfApi.get(player).map(data -> data.isBeastMode()).orElse(false);
        boolean quadruped = WerewolfApi.get(player).map(data -> data.isQuadrupedMode()).orElse(false);
        if (beast) {
            beastRenderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                    event.getMultiBufferSource(), event.getPackedLight());
        } else if (quadruped) {
            quadrupedRenderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                    event.getMultiBufferSource(), event.getPackedLight());
        } else {
            renderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                    event.getMultiBufferSource(), event.getPackedLight());
            boolean armoredInstinct = WerewolfApi.get(player)
                    .map(data -> data.hasAbility(WerewolfAbility.ARMORED_INSTINCT)).orElse(false);
            if (armoredInstinct && HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) {
                equipmentRenderer.prepare(player, event.getRenderer(), event.getPartialTick());
                equipmentRenderer.render(player, player.getYRot(), event.getPartialTick(), poseStack,
                        event.getMultiBufferSource(), event.getPackedLight());
            }
        }
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void renderWerewolfArm(RenderArmEvent event) {
        WerewolfApi.get(event.getPlayer()).ifPresent(data -> {
            if (!data.isTransformed()) return;
            if (data.isQuadrupedMode()) event.setCanceled(true);
            else WerewolfFirstPersonClawRenderer.render(event, data.isBeastMode());
        });
    }

    private ClientForgeEvents() {}
}
