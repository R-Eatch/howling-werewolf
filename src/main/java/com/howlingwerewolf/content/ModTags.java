package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> WEREWOLF_MEAT = TagKey.create(Registries.ITEM,
            new ResourceLocation(HowlingWerewolf.MOD_ID, "werewolf_meat"));
    /** Datapack hook for silver weapons supplied by other mods. */
    public static final TagKey<Item> SILVER_WEAPONS = TagKey.create(Registries.ITEM,
            new ResourceLocation(HowlingWerewolf.MOD_ID, "silver_weapons"));

    private ModTags() {}
}
