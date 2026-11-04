/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.ArrayList;
import java.util.List;

/** The single home for companion instructions, with scrollable bilingual chapters. */
public final class CompanionGuideScreen extends Screen {
    private static final int PANEL_WIDTH = 432, PANEL_HEIGHT = 316, VISIBLE_LINES = 21;
    static final List<String> TOPICS = List.of("ritual", "merchant", "recruit", "commands", "inventory", "appearance", "hunt", "upgrade", "revive", "sort");
    private final Screen parent;
    private final List<Button> chapters = new ArrayList<>();
    private int chapter, scroll, maximumScroll, ticks;
    private float scale = 1;

    public CompanionGuideScreen(Screen parent) {
        super(Component.translatable("screen.howlingwerewolf.companion.guide"));
        this.parent = parent;
    }
    public void update(CompoundTag snapshot) {
        if (parent instanceof CompanionScreen screen) screen.update(snapshot);
        else if (parent instanceof CompanionCustomizeScreen screen) screen.update(snapshot);
        else if (parent instanceof CompanionSortingScreen screen) screen.update(snapshot);
    }
    public void openManagement(CompoundTag snapshot) {
        if (parent instanceof CompanionScreen screen) { screen.update(snapshot); minecraft.setScreen(screen); }
        else if (parent instanceof CompanionCustomizeScreen screen) screen.openManagement(snapshot);
        else if (parent instanceof CompanionSortingScreen screen) screen.openManagement(snapshot);
    }
    @Override protected void init() {
        clearWidgets(); chapters.clear();
        scale = Math.max(0.1F, Math.min(1, Math.min((width - 12F) / PANEL_WIDTH, (height - 12F) / PANEL_HEIGHT)));
        int left = left(), top = top();
        for (int i = 0; i < TOPICS.size(); i++) {
            int index = i;
            Button button = addRenderableWidget(Button.builder(topicTitle(i), clicked -> {
                chapter = index; scroll = maximumScroll = 0; updateButtons();
            }).bounds(left + 12, top + 50 + i * 25, 104, 21).build());
            chapters.add(button);
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(left + PANEL_WIDTH - 66, top + 9, 54, 20).build());
        updateButtons();
    }
    private Component topicTitle(int index) { return Component.translatable("screen.howlingwerewolf.companion.guide." + TOPICS.get(index) + ".title"); }
    private void updateButtons() { for (int i = 0; i < chapters.size(); i++) chapters.get(i).active = i != chapter; }
    @Override public void tick() {
        if (++ticks % 20 == 0) ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.REFRESH, CompanionActionPacket.NO_TARGET));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        int left = left(), top = top();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(scale, scale);
            graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0181724);
            graphics.outline(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF8674BB);
            graphics.text(font, title, left + 14, top + 14, 0xFFE3D3FF, false);
            graphics.fill(left + 121, top + 46, left + 122, top + 300, 0xFF51425F);
            graphics.text(font, topicTitle(chapter), left + 132, top + 47, 0xFFE3D3FF, false);
            var lines = font.split(Component.translatable("screen.howlingwerewolf.companion.guide." + TOPICS.get(chapter) + ".body"), 280);
            maximumScroll = Math.max(0, lines.size() - VISIBLE_LINES);
            scroll = Math.clamp(scroll, 0, maximumScroll);
            graphics.enableScissor(left + 130, top + 66, left + 416, top + 300);
            try {
                for (int line = scroll; line < Math.min(lines.size(), scroll + VISIBLE_LINES); line++) {
                    graphics.text(font, lines.get(line), left + 132, top + 67 + (line - scroll) * 11, 0xFFE7E2EC, false);
                }
            } finally { graphics.disableScissor(); }
            if (maximumScroll > 0) {
                int thumb = Math.max(18, 232 * VISIBLE_LINES / lines.size());
                graphics.fill(left + 420, top + 66, left + 422, top + 298, 0xFF3A3048);
                int offset = (232 - thumb) * scroll / maximumScroll;
                graphics.fill(left + 420, top + 66 + offset, left + 422, top + 66 + offset + thumb, 0xFFAE94D6);
            }
            super.extractRenderState(graphics, Math.round(mouseX / scale), Math.round(mouseY / scale), partialTick);
        } finally { graphics.pose().popMatrix(); }
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (x / scale >= left() + 124 && x / scale < left() + 424 && y / scale >= top() + 60 && y / scale < top() + 302) {
            scroll = Math.clamp(scroll - (int) Math.signum(dy) * 3, 0, maximumScroll);
            return true;
        }
        return super.mouseScrolled(x / scale, y / scale, dx, dy);
    }
    private int left() { return (Math.round(width / scale) - PANEL_WIDTH) / 2; }
    private int top() { return (Math.round(height / scale) - PANEL_HEIGHT) / 2; }
    private MouseButtonEvent logical(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return super.mouseClicked(logical(event), doubleClick); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(logical(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double x, double y) { return super.mouseDragged(logical(event), x / scale, y / scale); }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {}
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
