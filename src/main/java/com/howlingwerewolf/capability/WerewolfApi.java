package com.howlingwerewolf.capability;

import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class WerewolfApi {
    private WerewolfApi() {}

    public static Optional<WerewolfData> get(Player player) {
        return Optional.of(player.getData(ModAttachments.WEREWOLF));
    }
}
