/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import net.minecraft.util.Mth;

/**
 * Timing of a big quadruped, in fractions of one stride. The gallop follows the rotary sequence of
 * wolves and big cats: hind pair lands one after the other and drives the body forward, the fore
 * pair reaches out and lands the same way, then all four limbs gather under a flexed spine in a
 * moment of suspension. The prowl is the slow lateral-sequence walk of a stalking cat.
 */
final class BeastGait {
    /** Radians of walk animation per stride phase; the walk position advances about 1 per tick at a sprint. */
    static final float GALLOP_RATE = 0.70F, PROWL_RATE = 0.55F;
    private static final float GALLOP_DUTY = 0.32F, PROWL_DUTY = 0.68F;
    static final float HIND_TRAIL = 0.00F, HIND_LEAD = 0.11F, FORE_TRAIL = 0.46F, FORE_LEAD = 0.56F;
    static final float[] PROWL = {0.00F, 0.25F, 0.50F, 0.75F};

    record Step(float travel, float lift, float tuck) {}

    static float stride(float walkPosition, float rate) {
        float cycle = walkPosition * rate / Mth.TWO_PI;
        return cycle - (float) Math.floor(cycle);
    }
    /** Paw travel runs from -1 (reaching forward) to +1 (pushing behind) while on the ground. */
    static Step gallop(float cycle, float start) { return step(cycle, start, GALLOP_DUTY); }
    static Step prowl(float cycle, float start) { return step(cycle, start, PROWL_DUTY); }

    private static Step step(float cycle, float start, float duty) {
        float phase = cycle - start;
        phase -= (float) Math.floor(phase);
        if (phase < duty) return new Step(phase / duty * 2 - 1, 0, 0);
        float swing = (phase - duty) / (1 - duty);
        float ease = swing * swing * (3 - 2 * swing);
        float arc = Mth.sin(swing * Mth.PI);
        return new Step(1 - 2 * ease, arc, arc * arc);
    }
    /** Nose rises while the hind pair drives, and dips as the fore pair takes the weight. */
    static float pitch(float cycle) { return Mth.sin((cycle - 0.04F) * Mth.TWO_PI); }
    /** +1 with the spine gathered, -1 with it stretched out. */
    static float flex(float cycle) { return Mth.cos((cycle - 0.93F) * Mth.TWO_PI); }
    /** Highest in the two suspensions, lowest when each pair is loaded. */
    static float rise(float cycle) { return Mth.cos((cycle - 0.42F) * 2 * Mth.TWO_PI); }

    private BeastGait() {}
}
