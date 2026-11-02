package com.howlingwerewolf.entity;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.WerewolfSkinIds;

/** NPC appearance comes from tracked server entity data, independently of player preferences. */
public interface WerewolfNpcAppearance {
    WerewolfModelSet getNpcModelSet();
    ModernSkin getNpcCoat();
    WerewolfForm getNpcForm();

    default String getNpcSkinId() {
        return getNpcModelSet() == WerewolfModelSet.MODERN ? getNpcCoat().skinId() : WerewolfSkinIds.DEFAULT_ID;
    }
}
