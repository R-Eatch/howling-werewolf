package com.howlingwerewolf.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public final class WerewolfApi {
    private WerewolfApi() {}

    public static LazyOptional<WerewolfData> get(Player player) {
        return player.getCapability(WerewolfCapabilities.WEREWOLF);
    }
}
