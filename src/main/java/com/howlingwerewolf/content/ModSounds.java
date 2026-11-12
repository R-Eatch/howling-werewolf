package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HowlingWerewolf.MOD_ID);

    /** A solitary wolf's long howl for Moonblood Surge, carried farther than the default sixteen blocks. */
    public static final DeferredHolder<SoundEvent, SoundEvent> MOONBLOOD_HOWL = SOUNDS.register("moonblood_howl",
            () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "moonblood_howl"), 48.0F));

    private ModSounds() {}
}
