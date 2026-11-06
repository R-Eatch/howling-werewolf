/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Iron's Spells and Better Combat animate the player through playerAnimator (KosmX) or Player
 * Animation Library (ZigyTheBird), which can draw the local player's own body in first person while
 * an animation plays. A werewolf body is taller than the camera and would fill the view, so during
 * that pass it is left out. Both libraries are looked up by name; without them nothing changes.
 */
final class FirstPersonBodyCompat {
    private static final MethodHandle KOSMX_PASS = staticFlag("dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode", "isFirstPersonPass");
    private static final Class<?> ZIGY_STATE = type("com.zigythebird.playeranim.accessors.IAvatarAnimationState");
    private static final MethodHandle ZIGY_PASS = ZIGY_STATE == null ? null : instanceFlag(ZIGY_STATE, "playerAnimLib$isFirstPersonPass");

    /** True while another mod draws the local player's body in first person; {@code renderState} may be null. */
    static boolean active(Object renderState) {
        try {
            if (KOSMX_PASS != null && (boolean) KOSMX_PASS.invoke()) return true;
            return ZIGY_PASS != null && ZIGY_STATE.isInstance(renderState) && (boolean) ZIGY_PASS.invoke(renderState);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Class<?> type(String name) {
        try { return Class.forName(name, false, FirstPersonBodyCompat.class.getClassLoader()); }
        catch (ClassNotFoundException | LinkageError absent) { return null; }
    }
    private static MethodHandle staticFlag(String owner, String name) {
        Class<?> type = type(owner);
        if (type == null) return null;
        try { return MethodHandles.publicLookup().findStatic(type, name, MethodType.methodType(boolean.class)); }
        catch (ReflectiveOperationException | LinkageError changed) { return null; }
    }
    private static MethodHandle instanceFlag(Class<?> type, String name) {
        try { return MethodHandles.publicLookup().findVirtual(type, name, MethodType.methodType(boolean.class)); }
        catch (ReflectiveOperationException | LinkageError changed) { return null; }
    }

    private FirstPersonBodyCompat() {}
}
