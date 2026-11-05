/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A detached fitting-room model standing before the moon; previewing changes no live companion. */
final class CompanionPreview {
    private RemotePlayer player;
    private int x, y, width, height, moonX, moonY, radius, viewLeft, viewTop, viewRight, viewBottom;
    private float rotation = -18, zoom = 1;
    private boolean dragging;

    /** The moon sets the size and the hit area; the view may be larger, so tall ears are not cut off. */
    void stage(int moonX, int moonY, int radius, int viewLeft, int viewTop, int viewRight, int viewBottom) {
        this.moonX = moonX; this.moonY = moonY; this.radius = radius;
        this.viewLeft = viewLeft; this.viewTop = viewTop; this.viewRight = viewRight; this.viewBottom = viewBottom;
        x = moonX - radius; y = moonY - radius; width = height = radius * 2;
        dragging = false;
    }
    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    boolean click(double mouseX, double mouseY, int button) {
        if (button != 0 || !contains(mouseX, mouseY)) return false;
        dragging = true;
        return true;
    }
    boolean drag(double distanceX) {
        if (!dragging) return false;
        rotation = Mth.wrapDegrees(rotation + (float) distanceX * 1.5F);
        return true;
    }
    boolean release(int button) {
        if (button != 0 || !dragging) return false;
        dragging = false;
        return true;
    }
    boolean scroll(double mouseX, double mouseY, double amount) {
        if (!contains(mouseX, mouseY)) return false;
        zoom = Mth.clamp(zoom + (float) amount * 0.08F, 0.65F, 1.3F);
        return true;
    }
    void render(GuiGraphicsExtractor graphics, String model, String skin, float uiScale, double mouseX, double mouseY, MoonUi.Theme theme) {
        MoonUi.moon(graphics, moonX, moonY, radius, MoonUi.moonPhase(), theme);
        float feet = moonY + radius * 0.88F;
        MoonUi.pool(graphics, moonX, feet, radius * 0.62F, radius * 0.11F, theme.shadow());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) { player = null; return; }
        if (player == null || player.level() != minecraft.level) {
            player = WerewolfSkinScreen.createPreviewPlayer(minecraft.level, minecraft.player.getGameProfile());
        }
        player.tickCount = minecraft.player.tickCount;
        float horizontal = (float) Math.atan((moonX - mouseX) / 90.0);
        float vertical = (float) Math.atan((moonY - mouseY) / 120.0);
        player.yBodyRot = player.yBodyRotO = 180 + rotation + horizontal * 12;
        player.setYRot(180 + rotation + horizontal * 30);
        player.setXRot(-vertical * 14);
        player.yHeadRot = player.yHeadRotO = player.getYRot();
        player.xRotO = player.getXRot();
        float size = radius * 1.72F / 2.5F * zoom;
        Quaternionf camera = new Quaternionf().rotateX(4 * Mth.DEG_TO_RAD);
        Quaternionf pose = new Quaternionf().rotateZ(Mth.PI).mul(camera);
        graphics.enableScissor(viewLeft, viewTop, viewRight, viewBottom);
        try {
            WerewolfSkinRenderContext.withPreview(player, WerewolfForm.WEREWOLF, WerewolfModelSet.byId(model), skin, () -> {
                var state = minecraft.getEntityRenderDispatcher().getRenderer(player).createRenderState(player, 1);
                state.shadowPieces.clear(); state.outlineColor = 0; state.nameTag = null; state.scoreText = null;
                graphics.entity(state, size * uiScale,
                        new Vector3f(0, (feet - (viewTop + viewBottom) / 2F) / size, 0), pose, camera,
                        Math.round(viewLeft * uiScale), Math.round(viewTop * uiScale),
                        Math.round(viewRight * uiScale), Math.round(viewBottom * uiScale));
            });
        } finally { graphics.disableScissor(); }
    }
}
