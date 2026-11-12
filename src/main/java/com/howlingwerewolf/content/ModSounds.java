package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HowlingWerewolf.MOD_ID);

    /** A solitary wolf's long howl for Moonblood Surge, carried farther than the default sixteen blocks. */
    public static final RegistryObject<SoundEvent> MOONBLOOD_HOWL = SOUNDS.register("moonblood_howl",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(HowlingWerewolf.MOD_ID, "moonblood_howl"), 48.0F));

    private ModSounds() {}
}
