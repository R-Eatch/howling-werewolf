/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Set;

/** Original bilingual lines: fixed ritual vows, with restrained contextual variations. */
public final class CompanionDialogue {
    private static final Map<String, Integer> VARIANTS = Map.ofEntries(
            Map.entry("follow", 3), Map.entry("guard", 3), Map.entry("rest", 3),
            Map.entry("inventory", 2), Map.entry("upgrade", 2), Map.entry("renamed", 2),
            Map.entry("reskinned", 2), Map.entry("combat", 3), Map.entry("wounded", 2),
            Map.entry("victory", 2), Map.entry("farewell", 2), Map.entry("fallen", 2),
            Map.entry("idle_follow", 3), Map.entry("idle_guard", 3), Map.entry("idle_rest", 3));
    private static final Set<String> RELIABLE = Set.of("follow", "guard", "rest", "sort", "fallen", "wounded");

    public static String key(String occasion, RandomSource random) {
        int count = VARIANTS.getOrDefault(occasion, 0);
        return "dialogue.howlingwerewolf.companion." + occasion + (count == 0 ? "" : "." + random.nextInt(count));
    }
    public static void say(ServerPlayer player, CompanionRecord record, String occasion) {
        if (!record.belongsTo(player.getUUID())) return;
        // Keep the requested greeting/vow/hunt/return/revival lines reliable. Ambient responses
        // share a cooldown across entity replacements, so recall cannot reset their chat limit.
        if (VARIANTS.containsKey(occasion) && !RELIABLE.contains(occasion)
                && !CompanionSavedData.get(player.level().getServer()).allowSpeech(record.id,
                player.level().getServer().overworld().getGameTime())) return;
        String custom = CompanionSavedData.get(player.level().getServer()).address(record.owner);
        Component address = custom.isEmpty() ? Component.translatable("dialogue.howlingwerewolf.companion.address_default") : Component.literal(custom);
        player.sendSystemMessage(Component.literal("<" + record.name() + "> ")
                .append(Component.translatable(key(occasion, player.getRandom()), address)));
    }
    public static void send(ServerPlayer player, String name, String occasion, RandomSource random) {
        player.sendSystemMessage(Component.literal("<" + name + "> ").append(Component.translatable(key(occasion, random))));
    }
    private CompanionDialogue() {}
}
