package com.howlingwerewolf.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.howlingwerewolf.network.*;
public final class HowlingWerewolfClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ClientModEvents.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::clientTick);
        ClientPlayNetworking.registerGlobalReceiver(SyncWerewolfDataPacket.TYPE,(packet,context) -> context.client().execute(() -> ClientPacketHandlers.applySync(packet)));
        ClientPlayNetworking.registerGlobalReceiver(BadgeActivationPacket.TYPE,(packet,context) -> context.client().execute(ClientPacketHandlers::showBadgeActivation));
    }
}
