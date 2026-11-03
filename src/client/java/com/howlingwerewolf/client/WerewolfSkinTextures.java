package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import net.minecraft.resources.Identifier;

/** Shared texture lookup for world, inventory preview and first-person claws. */
public final class WerewolfSkinTextures {
    public static Identifier get(String skinId, WerewolfForm form) {
        return WerewolfSkinCatalog.texture(skinId, form);
    }

    public static Identifier get(WerewolfSkin skin, WerewolfForm form) {
        return get("howlingwerewolf:" + skin.getId(), form);
    }

    private WerewolfSkinTextures() {}
}
