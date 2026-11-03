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

/** A detached fitting-room model; previewing never changes a live companion or player. */
final class CompanionPreview {
    private RemotePlayer player;
    private int x, y, width, height;
    private float rotation, zoom = 1;
    private boolean dragging;

    void bounds(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
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
    void render(GuiGraphicsExtractor graphics, String model, String skin, float uiScale, double mouseX, double mouseY) {
        graphics.fill(x, y, x + width, y + height, 0xFF211E2C);
        graphics.outline(x, y, width, height, 0xFF51425F);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) { player = null; return; }
        if (player == null || player.level() != minecraft.level) {
            player = WerewolfSkinScreen.createPreviewPlayer(minecraft.level, minecraft.player.getGameProfile());
        }
        player.tickCount = minecraft.player.tickCount;
        float horizontal = (float) Math.atan((x + width / 2.0 - mouseX) / 80.0);
        float vertical = (float) Math.atan((y + height / 2.0 - mouseY) / 100.0);
        player.yBodyRot = player.yBodyRotO = 180 + rotation + horizontal * 20;
        player.setYRot(180 + rotation + horizontal * 40);
        player.setXRot(-vertical * 20);
        player.yHeadRot = player.yHeadRotO = player.getYRot();
        player.xRotO = player.getXRot();
        float size = Math.min(width / 2.1F, height / 2.5F) * zoom;
        Quaternionf camera = new Quaternionf().rotateX(vertical * 20 * Mth.DEG_TO_RAD);
        Quaternionf pose = new Quaternionf().rotateZ(Mth.PI).mul(camera);
        graphics.enableScissor(x, y, x + width, y + height);
        try {
            WerewolfSkinRenderContext.withPreview(player, WerewolfForm.WEREWOLF, WerewolfModelSet.byId(model), skin, () -> {
                var state = minecraft.getEntityRenderDispatcher().getRenderer(player).createRenderState(player, 1);
                state.shadowPieces.clear(); state.outlineColor = 0; state.nameTag = null; state.scoreText = null;
                graphics.entity(state, size * uiScale,
                        new Vector3f(0, (height / 2.0F - 12) / size, 0), pose, camera,
                        Math.round(x * uiScale), Math.round(y * uiScale),
                        Math.round((x + width) * uiScale), Math.round((y + height) * uiScale));
            });
        } finally { graphics.disableScissor(); }
    }
}
