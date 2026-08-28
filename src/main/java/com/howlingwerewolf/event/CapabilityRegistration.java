package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfData;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CapabilityRegistration {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WerewolfData.class);
    }

    private CapabilityRegistration() {}
}
