package com.howlingwerewolf.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class WerewolfCapabilities {
    public static final Capability<WerewolfData> WEREWOLF = CapabilityManager.get(new CapabilityToken<>() {});

    private WerewolfCapabilities() {}
}
