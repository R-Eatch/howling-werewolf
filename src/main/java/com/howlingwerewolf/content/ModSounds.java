package com.howlingwerewolf.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class ModSounds {
    private static final SoundEvent HOWL = Registry.register(BuiltInRegistries.SOUND_EVENT, ModRegistry.id("moonblood_howl"),
            SoundEvent.createFixedRangeEvent(ModRegistry.id("moonblood_howl"), 48.0F));

    /** A solitary wolf's long howl for Moonblood Surge, carried farther than the default sixteen blocks. */
    public static final Supplier<SoundEvent> MOONBLOOD_HOWL = () -> HOWL;

    /** Loads the class, registering the sounds. */
    public static void initialize() {}

    private ModSounds() {}
}
