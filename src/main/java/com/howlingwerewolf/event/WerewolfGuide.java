package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class WerewolfGuide {
    private static final String CRITERION = "complete";

    public static void infection(ServerPlayer player) {
        award(player, "guide/infection");
    }

    public static void firstFullMoon(ServerPlayer player) {
        award(player, "guide/first_full_moon");
    }

    public static void firstPoint(ServerPlayer player) {
        award(player, "guide/first_point");
    }

    public static void firstBeastTransformation(ServerPlayer player) {
        award(player, "guide/first_beast");
    }

    public static void defeatAlpha(ServerPlayer player) {
        award(player, "guide/defeat_alpha");
    }

    public static void lastAlpha(ServerPlayer player) {
        award(player, "guide/last_alpha");
    }

    private static void award(ServerPlayer player, String path) {
        Advancement advancement = player.getServer().getAdvancements()
                .getAdvancement(new ResourceLocation(HowlingWerewolf.MOD_ID, path));
        if (advancement == null) {
            HowlingWerewolf.LOGGER.warn("Missing werewolf guide advancement {}", path);
            return;
        }
        player.getAdvancements().award(advancement, CRITERION);
    }

    private WerewolfGuide() {}
}
