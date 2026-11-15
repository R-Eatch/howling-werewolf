package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.network.RequestWerewolfSyncPacket;
import com.howlingwerewolf.network.TransformRequestPacket;
import com.howlingwerewolf.network.ToggleBeastModePacket;
import com.howlingwerewolf.network.ToggleQuadrupedModePacket;
import com.howlingwerewolf.network.ToggleNightVisionPacket;
import com.howlingwerewolf.network.UseAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientEvents {
    private static ResourceKey<Level> lastClientDimension;
    private static int dimensionResyncTicks = -1;
    private static int secondDimensionResyncTicks = -1;

    public static void clientTick(Minecraft minecraftUnused) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPounceTarget.tick();
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
            minecraft.gui.setScreen(new WerewolfProgressionScreen());
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
        while (ClientModEvents.POUNCE.consumeClick()) {
            ClientPounceTarget.toggle();
        }
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

    private ClientEvents() {}
}
