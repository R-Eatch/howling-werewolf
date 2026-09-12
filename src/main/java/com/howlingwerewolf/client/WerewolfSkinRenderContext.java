package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Objects;

/** A render-only override, scoped to one preview entity and restored even if rendering fails. */
public final class WerewolfSkinRenderContext {
    private static final ThreadLocal<Preview> PREVIEW = new ThreadLocal<>();

    public static void withPreview(AbstractClientPlayer player, WerewolfForm form,
                                   WerewolfSkin skin, Runnable render) {
        withPreview(player, form, "howlingwerewolf:" + skin.getId(), render);
    }

    public static void withPreview(AbstractClientPlayer player, WerewolfForm form,
                                   String skinId, Runnable render) {
        Preview previous = PREVIEW.get();
        PREVIEW.set(new Preview(Objects.requireNonNull(player), Objects.requireNonNull(form),
                Objects.requireNonNull(skinId)));
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
        return WerewolfSkin.byId(getSkinId(player));
    }

    public static String getSkinId(AbstractClientPlayer player) {
        if (isPreview(player)) return PREVIEW.get().skinId();
        return WerewolfApi.get(player).map(data -> data.getSkinId()).orElse(WerewolfSkinIds.DEFAULT_ID);
    }

    private record Preview(AbstractClientPlayer player, WerewolfForm form, String skinId) {}

    private WerewolfSkinRenderContext() {}
}
