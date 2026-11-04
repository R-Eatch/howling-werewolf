/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** The shared window: one frame and tab bar, so moving between companion pages keeps its place. */
abstract class CompanionFrameScreen extends Screen {
    protected static final int WIDTH = 470, HEIGHT = 290, BODY_TOP = 34;
    enum Tab { PACK, SORT, LOOK, GUIDE }
    /** Where the tab underline last rested, so it glides to the next page instead of jumping. */
    private static float underlineX = -1;

    protected final Screen parent;
    protected CompoundTag snapshot;
    protected @Nullable UUID selected;
    protected float scale = 1;
    protected final CompanionPreview preview = new CompanionPreview();
    protected boolean previewActive;
    private final Map<Tab, UiButton> tabs = new EnumMap<>(Tab.class);
    private int refreshTicks;
    private long lastFrame = Util.getMillis();

    protected CompanionFrameScreen(Component title, @Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(title);
        this.parent = parent;
        this.snapshot = snapshot.copy();
        this.selected = selected;
        reselect();
    }

    protected abstract Tab tab();
    protected abstract void build(int left, int top);
    protected abstract void body(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY);
    /** Drawn above the controls, for text that sits on card buttons. */
    protected void overlay(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY) {}
    protected void refreshControls() {}
    protected void onSnapshot(CompoundTag previous) {}
    /** A hint for something drawn directly rather than a button. */
    protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) { return null; }

    /** Packets from the server land here for whichever page is open. */
    final void receive(CompoundTag next, boolean open) {
        CompoundTag previous = snapshot;
        snapshot = next.copy();
        if (open) {
            String target = next.getStringOr("OpenTarget", "");
            if (!target.isEmpty()) try { selected = UUID.fromString(target); } catch (IllegalArgumentException ignored) {}
            reselect();
            if (tab() != Tab.PACK) { openTab(Tab.PACK); return; }
        }
        reselect();
        if (!rosterIds(previous).equals(rosterIds(snapshot))) { rebuildWidgets(); }
        onSnapshot(previous);
        updateTabs();
        refreshControls();
    }
    private void reselect() {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        if (recordFor(selected) != null) return;
        selected = null;
        if (!entries.isEmpty()) {
            try { selected = UUID.fromString(entries.getCompoundOrEmpty(0).getStringOr("Id", "")); } catch (IllegalArgumentException ignored) {}
        }
    }
    private static String rosterIds(CompoundTag data) {
        StringBuilder ids = new StringBuilder();
        ListTag entries = data.getListOrEmpty("Companions");
        for (int i = 0; i < entries.size(); i++) ids.append(entries.getCompoundOrEmpty(i).getStringOr("Id", "")).append(';');
        return ids.toString();
    }
    protected final @Nullable CompoundTag selectedRecord() { return recordFor(selected); }
    private @Nullable CompoundTag recordFor(@Nullable UUID id) {
        if (id == null) return null;
        ListTag entries = snapshot.getListOrEmpty("Companions");
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompoundOrEmpty(i);
            if (id.toString().equals(entry.getStringOr("Id", ""))) return entry;
        }
        return null;
    }
    protected static CompanionMode mode(CompoundTag entry) {
        return CompanionMode.values()[Math.clamp(entry.getIntOr("Mode", CompanionMode.DEAD.ordinal()), 0, CompanionMode.values().length - 1)];
    }
    protected final boolean systemEnabled() { return snapshot.getBooleanOr("SystemEnabled", true); }
    protected final void send(CompanionActionPacket.Action action, UUID target, String value) {
        if (minecraft.getConnection() == null) return;
        ClientPacketDistributor.sendToServer(new CompanionActionPacket(action, target, value));
    }

    private void openTab(Tab destination) {
        if (destination == tab()) return;
        minecraft.setScreen(switch (destination) {
            case PACK -> new CompanionScreen(parent, snapshot, selected);
            case SORT -> new CompanionSortingScreen(parent, snapshot, selected);
            case LOOK -> new CompanionCustomizeScreen(parent, snapshot, selected);
            case GUIDE -> new CompanionGuideScreen(parent, snapshot, selected);
        });
    }
    protected final void showTab(Tab destination) { openTab(destination); }

    @Override protected final void init() {
        clearWidgets();
        tabs.clear();
        scale = Math.max(0.1F, Math.min(1, Math.min((width - 8F) / WIDTH, (height - 8F) / HEIGHT)));
        int left = left(), top = top();
        int x = left + 92;
        for (Tab each : Tab.values()) {
            Component label = Component.translatable("screen.howlingwerewolf.companion.tab." + each.name().toLowerCase(java.util.Locale.ROOT));
            int tabWidth = Math.clamp(font.width(label) + 20, 52, 84);
            UiButton button = new UiButton(x, top + 7, tabWidth, 20, label, Look.TAB, () -> openTab(each));
            button.selected(each == tab());
            tabs.put(each, addRenderableWidget(button));
            x += tabWidth + 2;
        }
        addRenderableWidget(new UiButton(left + WIDTH - 26, top + 7, 20, 20, Component.literal("×"), Look.TAB, this::onClose)
                .hint(() -> Component.translatable("gui.done")));
        build(left, top);
        updateTabs();
        refreshControls();
    }
    private void updateTabs() {
        CompoundTag record = selectedRecord();
        boolean present = record != null && mode(record).hasEntity() && systemEnabled();
        for (var entry : tabs.entrySet()) {
            UiButton button = entry.getValue();
            button.active = switch (entry.getKey()) {
                case PACK, GUIDE -> true;
                case SORT -> present && snapshot.getBooleanOr("SortingEnabled", false);
                case LOOK -> present;
            };
        }
    }

    @Override public void tick() {
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            send(CompanionActionPacket.Action.REFRESH, CompanionActionPacket.NO_TARGET, "");
        }
        refreshControls();
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        float logicalX = mouseX / scale, logicalY = mouseY / scale;
        int left = left(), top = top();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(scale, scale);
            CompanionUi.card(graphics, left, top, WIDTH, HEIGHT, CompanionUi.PANEL, CompanionUi.LINE);
            graphics.fill(left + 1, top + 29, left + WIDTH - 1, top + 30, CompanionUi.LINE);
            graphics.item(new ItemStack(ModItems.MOONBANE_PEARL.get()), left + 8, top + 7);
            CompanionUi.label(graphics, font, Component.translatable("screen.howlingwerewolf.companions"), left + 28, top + 13, 60, CompanionUi.MOON);
            drawUnderline(graphics, top);
            body(graphics, left, top, logicalX, logicalY);
            super.extractRenderState(graphics, Math.round(logicalX), Math.round(logicalY), partialTick);
            overlay(graphics, left, top, logicalX, logicalY);
            Component hint = null;
            for (var child : children()) {
                if (child instanceof UiButton button && button.visible && button.isHovered()) hint = button.hintText();
            }
            if (hint == null) hint = areaHint(left, top, logicalX, logicalY);
            if (hint != null) graphics.setTooltipForNextFrame(font, font.split(hint, 210), mouseX, mouseY);
        } finally { graphics.pose().popMatrix(); }
    }
    private void drawUnderline(GuiGraphicsExtractor graphics, int top) {
        UiButton active = tabs.get(tab());
        if (active == null) return;
        long now = Util.getMillis();
        float blend = Math.min(1, (now - lastFrame) / 70F);
        lastFrame = now;
        float goal = active.getX() + 10;
        underlineX = underlineX < 0 ? goal : Mth.lerp(blend, underlineX, goal);
        graphics.fill(Math.round(underlineX), top + 27, Math.round(underlineX) + active.getWidth() - 20, top + 29, CompanionUi.MOON);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {}

    protected final int left() { return (Math.round(width / scale) - WIDTH) / 2; }
    protected final int top() { return (Math.round(height / scale) - HEIGHT) / 2; }
    private MouseButtonEvent logical(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(logical(event), doubleClick)
                || previewActive && preview.click(event.x() / scale, event.y() / scale, event.button());
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        return previewActive && preview.release(event.button()) || super.mouseReleased(logical(event));
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double x, double y) {
        return previewActive && preview.drag(x / scale) || super.mouseDragged(logical(event), x / scale, y / scale);
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        return previewActive && preview.scroll(x / scale, y / scale, dy) || super.mouseScrolled(x / scale, y / scale, dx, dy);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
