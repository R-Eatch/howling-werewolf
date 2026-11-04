/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import net.minecraft.util.Mth;

/** Paired forelimbs recover first; the hind pair follows half a stride later. */
final class BeastGait {
    private static final float SWING = 0.42F;
    record Step(float travel, float lift) {}

    static Step step(float phase) {
        float cycle = phase / Mth.TWO_PI;
        cycle -= (float) Math.floor(cycle);
        if (cycle < SWING) {
            float swing = cycle / SWING;
            float lift = Mth.sin(swing * Mth.PI);
            return new Step(Mth.cos(swing * Mth.PI), lift * lift);
        }
        // A nearly constant backward sweep keeps stance paws from cycling like wheels.
        return new Step(-1 + 2 * (cycle - SWING) / (1 - SWING), 0);
    }

    private BeastGait() {}
}
