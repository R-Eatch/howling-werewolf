/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.AbstractClientPlayer;

import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class PunchyCompat {
    static void register() {
        if (!FabricLoader.getInstance().isModLoaded("punchy")) return;

        try {
            Class<?> api = Class.forName("punchy.api.PunchyApi");
            Class<?> handCondition = Class.forName("punchy.api.PunchyApi$HandCondition");
            var condition = MethodHandles.lookup().findStatic(PunchyCompat.class, "leaveHandToMinecraft",
                    MethodType.methodType(boolean.class, Object.class, boolean.class));
            api.getMethod("leaveHandsToMinecraft", String.class, handCondition)
                    .invoke(null, HowlingWerewolf.MOD_ID, MethodHandleProxies.asInterfaceInstance(handCondition, condition));
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // Older Punchy versions lack the hand ownership API.
        } catch (ReflectiveOperationException | LinkageError e) {
            HowlingWerewolf.LOGGER.warn("Could not register Punchy hand compatibility", e);
        }
    }

    private static boolean leaveHandToMinecraft(Object player, boolean mainHand) {
        // Return the empty main hand to the vanilla renderer, which invokes the werewolf arm mixin.
        return mainHand && player instanceof AbstractClientPlayer clientPlayer
                && clientPlayer.getMainHandItem().isEmpty()
                && !clientPlayer.isInvisible() && !clientPlayer.isScoping()
                && WerewolfApi.get(clientPlayer).map(WerewolfData::isTransformed).orElse(false);
    }

    private PunchyCompat() {}
}
