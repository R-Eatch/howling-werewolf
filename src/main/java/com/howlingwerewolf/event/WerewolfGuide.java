package com.howlingwerewolf.event;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.companion.CompanionRecord;
import com.howlingwerewolf.companion.CompanionRules;
import com.howlingwerewolf.companion.CompanionSavedData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

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

    public static void firstCompanion(ServerPlayer player) {
        award(player, "guide/first_companion");
    }

    public static void companionMaxTier(ServerPlayer player) {
        award(player, "guide/companion_max_tier");
    }

    /** Back-fill the companion advancements for companions recruited or raised before they existed. */
    public static void companions(ServerPlayer player) {
        List<CompanionRecord> owned = CompanionSavedData.get(player.level().getServer()).ownedBy(player.getUUID());
        if (owned.isEmpty()) return;
        firstCompanion(player);
        if (owned.stream().anyMatch(record -> record.tier >= CompanionRules.MAX_TIER)) companionMaxTier(player);
    }

    private static void award(ServerPlayer player, String path) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, path));
        if (advancement == null) {
            HowlingWerewolf.LOGGER.warn("Missing werewolf guide advancement {}", path);
            return;
        }
        player.getAdvancements().award(advancement, CRITERION);
    }

    private WerewolfGuide() {}
}
