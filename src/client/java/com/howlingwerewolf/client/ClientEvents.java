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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientEvents {
    private static net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;
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

    public static boolean renderWerewolfPlayer(net.minecraft.client.renderer.entity.state.AvatarRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.CameraRenderState camera) {
        var data=WerewolfRenderData.get(state);
        WerewolfForm form=data.form();
        if (form==WerewolfForm.HUMAN || renderer==null) return false;
        pose.pushPose();
        try {
            if (data.modelSet()==WerewolfModelSet.MODERN) modernRenderers.get(form).submit(state,pose,collector,camera);
            else switch(form) {
                case BEAST -> beastRenderer.submit(state,pose,collector,camera);
                case QUADRUPED -> quadrupedRenderer.submit(state,pose,collector,camera);
                default -> {
                    renderer.submit(state,pose,collector,camera);
                    if (data.armoredInstinct() && HWConfig.SHOW_WEREWOLF_EQUIPMENT.get()) equipmentRenderer.submit(state,pose,collector,camera);
                }
            }
        } finally { pose.popPose(); }
        return true;
    }

    public static void renderWerewolfArm(ArmRenderContext event) {
        var data = WerewolfRenderData.forEntity(event.getPlayer());
        if (data.form() == WerewolfForm.HUMAN) return;
        if (data.form() == WerewolfForm.QUADRUPED) event.setCanceled(true);
        else if (data.modelSet() == WerewolfModelSet.MODERN) ModernFirstPersonClawRenderer.render(event, data);
        else WerewolfFirstPersonClawRenderer.render(event, data);
    }

    private ClientEvents() {}
}
