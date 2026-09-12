package com.howlingwerewolf.client;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

/** Shared texture lookup for world, inventory preview and first-person claws. */
public final class WerewolfSkinTextures {
    private static final Map<WerewolfSkin, Map<WerewolfForm, ResourceLocation>> TEXTURES = createTextures();

    private static Map<WerewolfSkin, Map<WerewolfForm, ResourceLocation>> createTextures() {
        Map<WerewolfSkin, Map<WerewolfForm, ResourceLocation>> textures = new EnumMap<>(WerewolfSkin.class);
        for (WerewolfSkin skin : WerewolfSkin.values()) {
            String base = "textures/entity/" + (skin == WerewolfSkin.ADRIAN ? "" : "skins/" + skin.getId() + "/");
            Map<WerewolfForm, ResourceLocation> forms = new EnumMap<>(WerewolfForm.class);
            forms.put(WerewolfForm.WEREWOLF, new ResourceLocation(HowlingWerewolf.MOD_ID, base + "werewolf.png"));
            forms.put(WerewolfForm.QUADRUPED, new ResourceLocation(HowlingWerewolf.MOD_ID, base + "quadruped_werewolf.png"));
            forms.put(WerewolfForm.BEAST, new ResourceLocation(HowlingWerewolf.MOD_ID, base + "beast.png"));
            textures.put(skin, forms);
        }
        return textures;
    }

    public static ResourceLocation get(WerewolfSkin skin, WerewolfForm form) {
        return TEXTURES.get(skin).get(form);
    }

    private WerewolfSkinTextures() {}
}
