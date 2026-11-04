/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.companion.CompanionSortSettings;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.*;

public final class CompanionSortingScreen extends Screen {
    private static final int WIDTH = 432, HEIGHT = 316;
    private final CompanionScreen parent;
    private final UUID id;
    private CompoundTag snapshot;
    private final Map<String, Button> controls = new HashMap<>();
    private float scale = 1;
    private int scroll, ticks, delay;

    CompanionSortingScreen(CompanionScreen parent, CompoundTag snapshot, UUID id) {
        super(Component.translatable("screen.howlingwerewolf.companion.sort"));
        this.parent = parent; this.snapshot = snapshot.copy(); this.id = id;
    }
    public void update(CompoundTag snapshot) { this.snapshot = snapshot.copy(); parent.update(snapshot); updateButtons(); }
    public void openManagement(CompoundTag snapshot) { parent.update(snapshot); minecraft.setScreen(parent); }
    private CompoundTag record() {
        ListTag list = snapshot.getListOrEmpty("Companions");
        for (int i = 0; i < list.size(); i++) if (list.getCompoundOrEmpty(i).getStringOr("Id", "").equals(id.toString())) return list.getCompoundOrEmpty(i);
        return new CompoundTag();
    }
    @Override protected void init() {
        clearWidgets(); controls.clear();
        scale = Math.max(0.1F, Math.min(1, Math.min((width - 12F) / WIDTH, (height - 12F) / HEIGHT)));
        String[] actions = {"register", "clear", "toggle", "start", "stop"};
        for (int i = 0; i < actions.length; i++) {
            String action = actions[i];
            controls.put(action, addRenderableWidget(Button.builder(text(action), button -> request(action))
                    .bounds(left() + 14 + i % 3 * 136, top() + 250 + i / 3 * 27, 130, 21).build()));
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.guide"), button -> minecraft.setScreen(new CompanionGuideScreen(this)))
                .bounds(left() + WIDTH - 66, top() + 9, 54, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(left() + 286, top() + 277, 130, 21).build());
        updateButtons();
    }
    private Component text(String action) {
        if (action.equals("toggle")) return Component.translatable("screen.howlingwerewolf.companion.sort." + (record().getBooleanOr("SortConsolidate", true) ? "consolidate" : "merge"));
        return Component.translatable("screen.howlingwerewolf.companion.sort." + action);
    }
    private void request(String action) {
        Button control = controls.get(action);
        if (control == null || !control.active) return;
        ClientPacketDistributor.sendToServer(new CompanionActionPacket(action.equals("stop") ? CompanionActionPacket.Action.REST : CompanionActionPacket.Action.SORT, id, action));
        delay = 5; updateButtons();
        if (action.equals("register")) minecraft.setScreen(null);
    }
    private void updateButtons() {
        CompoundTag record = record(); int mode = record.getIntOr("Mode", CompanionMode.DEAD.ordinal());
        boolean ready = snapshot.getBooleanOr("SystemEnabled", true) && snapshot.getBooleanOr("SortingEnabled", false) && record.getBooleanOr("Enabled", false)
                && mode != CompanionMode.HUNT.ordinal() && mode != CompanionMode.DEAD.ordinal() && delay == 0;
        controls.forEach((action, button) -> {
            button.setMessage(text(action));
            button.active = ready && (action.equals("stop") || record.getBooleanOr("SortNearby", false))
                    && (!action.equals("start") || mode != CompanionMode.SORT.ordinal() && !record.getListOrEmpty("SortContainers").isEmpty())
                    && (!action.equals("stop") || mode == CompanionMode.SORT.ordinal());
        });
    }
    private String fit(Component text, int width) {
        String value = text.getString();
        return font.width(value) <= width ? value : font.plainSubstrByWidth(value, width - font.width("…")) + "…";
    }
    @Override public void tick() {
        if (delay > 0) delay--; updateButtons();
        if (++ticks % 20 == 0) ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.REFRESH, CompanionActionPacket.NO_TARGET));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        int left = left(), top = top(); CompoundTag record = record(); ListTag positions = record.getListOrEmpty("SortContainers");
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(scale, scale);
            graphics.fill(left, top, left + WIDTH, top + HEIGHT, 0xF0181724);
            graphics.outline(left, top, WIDTH, HEIGHT, 0xFF8674BB);
            graphics.text(font, title, left + 14, top + 14, 0xFFE3D3FF, false);
            CompanionMode mode = CompanionMode.values()[Math.clamp(record.getIntOr("Mode", CompanionMode.DEAD.ordinal()), 0, CompanionMode.values().length - 1)];
            Component identity = Component.literal(record.getStringOr("Name", "")).append(" · ").append(Component.translatable(mode.translationKey()));
            graphics.text(font, fit(identity, WIDTH - 28), left + 14, top + 44, 0xFFE7E2EC, false);
            graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.sort.count", positions.size(), CompanionSortSettings.LIMIT), left + 14, top + 62, 0xFFCFCCD9, false);
            int[] center = record.getIntArray("SortCenter").orElse(new int[]{0, 0, 0});
            if (center.length == 3) graphics.text(font, fit(Component.translatable("screen.howlingwerewolf.companion.sort.center", center[0], center[1], center[2])
                    .append(" · ").append(record.getStringOr("SortDimension", "")), WIDTH - 28), left + 14, top + 79, 0xFFCFCCD9, false);
            graphics.fill(left + 14, top + 98, left + WIDTH - 14, top + 239, 0xFF211E2B);
            scroll = Math.clamp(scroll, 0, Math.max(0, positions.size() - 8));
            if (positions.size() > 8) {
                int thumbHeight = Math.max(16, 137 * 8 / positions.size());
                int thumbTop = top + 100 + (137 - thumbHeight) * scroll / (positions.size() - 8);
                graphics.fill(left + WIDTH - 22, top + 100, left + WIDTH - 18, top + 237, 0xFF51425F);
                graphics.fill(left + WIDTH - 22, thumbTop, left + WIDTH - 18, thumbTop + thumbHeight, 0xFFB7A5DB);
            }
            for (int i = scroll; i < Math.min(positions.size(), scroll + 8); i++) {
                CompoundTag position = positions.getCompoundOrEmpty(i);
                graphics.text(font, Component.literal((i + 1) + ".  " + position.getIntOr("X", 0) + ", " + position.getIntOr("Y", 0) + ", " + position.getIntOr("Z", 0)),
                        left + 24, top + 106 + (i - scroll) * 16, 0xFFE7E2EC, false);
            }
            super.extractRenderState(graphics, Math.round(mouseX / scale), Math.round(mouseY / scale), partialTick);
        } finally { graphics.pose().popMatrix(); }
    }
    private int left() { return (Math.round(width / scale) - WIDTH) / 2; }
    private int top() { return (Math.round(height / scale) - HEIGHT) / 2; }
    private MouseButtonEvent logical(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) { return super.mouseClicked(logical(event), twice); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(logical(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double x, double y) { return super.mouseDragged(logical(event), x / scale, y / scale); }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        double logicalX = x / scale, logicalY = y / scale;
        if (logicalX >= left() + 14 && logicalX < left() + WIDTH - 14 && logicalY >= top() + 98 && logicalY < top() + 239 && dy != 0) {
            scroll = Math.clamp(scroll + (dy > 0 ? -1 : 1), 0, Math.max(0, record().getListOrEmpty("SortContainers").size() - 8));
            return true;
        }
        return super.mouseScrolled(logicalX, logicalY, dx, dy);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {}
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
