package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Objects;

/** A render-only override, scoped to one preview entity and restored even if rendering fails. */
public final class WerewolfSkinRenderContext {
    private static final ThreadLocal<Preview> PREVIEW = new ThreadLocal<>();

    public static void withPreview(AbstractClientPlayer player, WerewolfForm form,
                                   WerewolfSkin skin, Runnable render) {
        Preview previous = PREVIEW.get();
        PREVIEW.set(new Preview(Objects.requireNonNull(player), Objects.requireNonNull(form),
                Objects.requireNonNull(skin)));
        try {
            render.run();
        } finally {
            if (previous == null) PREVIEW.remove();
            else PREVIEW.set(previous);
        }
    }

    public static boolean isPreview(AbstractClientPlayer player) {
        Preview preview = PREVIEW.get();
        return preview != null && preview.player() == player;
    }

    public static WerewolfForm getForm(AbstractClientPlayer player) {
        if (isPreview(player)) return PREVIEW.get().form();
        return WerewolfApi.get(player).map(data -> data.getForm()).orElse(WerewolfForm.HUMAN);
    }

    public static WerewolfSkin getSkin(AbstractClientPlayer player) {
        if (isPreview(player)) return PREVIEW.get().skin();
        return WerewolfApi.get(player).map(data -> data.getSkin()).orElse(WerewolfSkin.ADRIAN);
    }

    private record Preview(AbstractClientPlayer player, WerewolfForm form, WerewolfSkin skin) {}

    private WerewolfSkinRenderContext() {}
}
