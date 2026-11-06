/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import com.howlingwerewolf.client.MoonInput.KeyEvent;
import com.howlingwerewolf.client.MoonInput.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The companion window is opened far more often than the progression screen, so it speaks the same
 * language in a lighter form: a sheet over the world that keeps the game in view and opens at once.
 * The tabs sit in its upper band, and the lower band reads out whatever the pointer rests on.
 */
abstract class CompanionFrameScreen extends MoonInput.Page {
    protected static final int WIDTH = 520, HEIGHT = 306, TOP_BAND = 26, LOWER_BAND = 46;
    private static final int TITLE_ROOM = 136, COMFORT_WIDTH = 640, COMFORT_HEIGHT = 360;
    enum Tab { PACK, SORT, LOOK, GUIDE }
    /** Where the tab underline last rested, so it glides to the next page instead of jumping. */
    private static float underlineX = -1, underlineWidth;
    /** Switching tabs replaces the screen; only a window that was closed rises into place. */
    private static long lastDrawn;

    /** What the lower band shows when the pointer rests on nothing in particular. */
    protected record Focus(@Nullable Component heading, @Nullable Component body) {}

    protected final Screen parent;
    protected CompoundTag snapshot;
    protected @Nullable UUID selected;
    protected float scale = 1;
    protected final CompanionPreview preview = new CompanionPreview();
    protected boolean previewActive;
    private final Map<Tab, UiButton> tabs = new EnumMap<>(Tab.class);
    private final long opened = Util.getMillis() - lastDrawn < 250 ? 0 : Util.getMillis();
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
    protected abstract void body(MoonGraphics graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY);
    /** Drawn above the controls, for text that sits beside a row. */
    protected void overlay(MoonGraphics graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY) {}
    protected void refreshControls() {}
    protected void onSnapshot(CompoundTag previous) {}
    /** A hint for something drawn directly rather than a button. */
    protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) { return null; }
    protected abstract Focus focus();
    /** How much of the lower band's right side this page's actions take. */
    protected abstract int actionsWidth();

    /** Packets from the server land here for whichever page is open. */
    final void receive(CompoundTag next, boolean open) {
        CompoundTag previous = snapshot;
        snapshot = next.copy();
        if (open) {
            String target = next.getString("OpenTarget");
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
        ListTag entries = snapshot.getList("Companions", 10);
        if (recordFor(selected) != null) return;
        selected = null;
        if (!entries.isEmpty()) {
            try { selected = UUID.fromString(entries.getCompound(0).getString("Id")); } catch (IllegalArgumentException ignored) {}
        }
    }
    private static String rosterIds(CompoundTag data) {
        StringBuilder ids = new StringBuilder();
        ListTag entries = data.getList("Companions", 10);
        for (int i = 0; i < entries.size(); i++) ids.append(entries.getCompound(i).getString("Id")).append(';');
        return ids.toString();
    }
    protected final @Nullable CompoundTag selectedRecord() { return recordFor(selected); }
    private @Nullable CompoundTag recordFor(@Nullable UUID id) {
        if (id == null) return null;
        ListTag entries = snapshot.getList("Companions", 10);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (id.toString().equals(entry.getString("Id"))) return entry;
        }
        return null;
    }
    protected static CompanionMode mode(CompoundTag entry) {
        return CompanionMode.values()[Mth.clamp(CompanionUi.integer(entry, "Mode", CompanionMode.DEAD.ordinal()), 0, CompanionMode.values().length - 1)];
    }
    protected final boolean systemEnabled() { return CompanionUi.flag(snapshot, "SystemEnabled", true); }
    protected final void send(CompanionActionPacket.Action action, UUID target, String value) {
        if (minecraft.getConnection() == null) return;
        PacketDistributor.sendToServer(new CompanionActionPacket(action, target, value));
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
        float fit = Math.min((width - 8F) / WIDTH, (height - 8F) / HEIGHT);
        scale = fit < 1 ? Math.max(0.1F, fit) : Math.max(1, (int) Math.min((float) width / COMFORT_WIDTH, (float) height / COMFORT_HEIGHT));
        int left = left(), top = top();
        int x = left + TITLE_ROOM;
        for (Tab each : Tab.values()) {
            Component label = Component.translatable("screen.howlingwerewolf.companion.tab." + each.name().toLowerCase(java.util.Locale.ROOT));
            int tabWidth = Mth.clamp(font.width(label) + 16, 40, 76);
            UiButton button = new UiButton(x, top + 3, tabWidth, 20, label, Look.TAB, () -> openTab(each));
            button.selected(each == tab());
            tabs.put(each, addRenderableWidget(button));
            x += tabWidth;
        }
        addRenderableWidget(new MoonUi.ThemeToggle(left + WIDTH - 40, top + 6, this::rebuildWidgets));
        addRenderableWidget(new UiButton(left + WIDTH - 22, top + 5, 14, 16, Component.literal("×"), Look.TAB, this::onClose)
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
                case SORT -> present && snapshot.getBoolean("SortingEnabled");
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

    @Override public void extractRenderState(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        lastDrawn = now;
        float rise = MoonUi.smooth((now - opened) / 120F);
        graphics.fillGradient(0, 0, width, height, MoonUi.alpha(0x66060509, rise), MoonUi.alpha(0x99060509, rise));
        float logicalX = mouseX / scale, logicalY = mouseY / scale;
        int left = left(), top = top();
        MoonUi.Theme theme = MoonUi.theme();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(scale, scale);
            graphics.pose().translate(0, (1 - rise) * 6);
            MoonUi.pool(graphics, left + WIDTH / 2F, top + HEIGHT / 2F + 5, WIDTH * 0.66F, HEIGHT * 0.70F, 0x60000000);
            MoonUi.round(graphics, left, top, WIDTH, HEIGHT, 8, MoonUi.BAND);
            int canvasBottom = top + HEIGHT - LOWER_BAND;
            graphics.fillGradient(left, top + TOP_BAND, left + WIDTH, canvasBottom, theme.top(), theme.bottom());
            body(graphics, theme, left, top, logicalX, logicalY);
            Component heading = Component.translatable("screen.howlingwerewolf.companions");
            if (font.width(heading) * 2 <= TITLE_ROOM - 18) MoonUi.large(graphics, font, heading, left + 12, top + 5, 2, MoonUi.BAND_TEXT);
            else CompanionUi.label(graphics, font, heading, left + 12, top + 9, TITLE_ROOM - 18, MoonUi.BAND_TEXT);
            drawUnderline(graphics, top);
            drawFocus(graphics, left, top, logicalX, logicalY);
            super.extractRenderState(graphics, Math.round(logicalX), Math.round(logicalY), partialTick);
            overlay(graphics, theme, left, top, logicalX, logicalY);
        } finally { graphics.pose().popMatrix(); }
    }
    private void drawUnderline(MoonGraphics graphics, int top) {
        UiButton active = tabs.get(tab());
        if (active == null) return;
        long now = Util.getMillis();
        float blend = 1 - (float) Math.exp(-Math.min(now - lastFrame, 100) / 60F);
        lastFrame = now;
        float goalX = active.getX() + 8, goalWidth = active.getWidth() - 16;
        underlineX = underlineX < 0 ? goalX : Mth.lerp(blend, underlineX, goalX);
        underlineWidth = underlineWidth <= 0 ? goalWidth : Mth.lerp(blend, underlineWidth, goalWidth);
        graphics.fill(Math.round(underlineX), top + 21, Math.round(underlineX + underlineWidth), top + 23, MoonUi.BLOOD);
    }
    /** The pointer's subject first, then the page's own resting text. */
    private void drawFocus(MoonGraphics graphics, int left, int top, float mouseX, float mouseY) {
        Component heading = null, body = null;
        for (var child : children()) {
            if (child instanceof UiButton button && button.visible && button.isMouseOver(mouseX, mouseY)) {
                Component hint = button.hintText();
                if (hint != null) {
                    boolean named = !button.getMessage().getString().isEmpty() && button.look() != Look.TAB;
                    heading = named ? button.getMessage() : null;
                    body = hint;
                }
            } else if (child instanceof MoonUi.ThemeToggle toggle && toggle.isMouseOver(mouseX, mouseY)) {
                body = toggle.getMessage();
            }
        }
        if (body == null) body = areaHint(left, top, mouseX, mouseY);
        if (body == null) {
            Focus resting = focus();
            heading = resting.heading();
            body = resting.body();
        }
        int room = WIDTH - 24 - actionsWidth(), y = top + HEIGHT - LOWER_BAND + 6;
        if (heading != null) {
            CompanionUi.label(graphics, font, heading, left + 12, y, room, MoonUi.BAND_TEXT);
            y += 11;
        }
        if (body != null) CompanionUi.wrapped(graphics, font, body, left + 12, y, room, MoonUi.BAND_SUB, heading != null ? 2 : 3);
    }
    /** Draws lines that dissolve at whichever edge has more text beyond it. */
    protected final void reading(MoonGraphics graphics, List<FormattedCharSequence> lines, int x, int width, int top, int bottom, float offset, int maximum, int color) {
        graphics.enableScissor(x, top, x + width, bottom);
        try {
            for (int i = 0; i < lines.size(); i++) {
                int y = top + i * 11 - Math.round(offset);
                if (y + 10 <= top || y >= bottom) continue;
                float fade = 1;
                if (offset > 1) fade = Math.min(fade, (y - top + 2) / 12F);
                if (offset < maximum - 1) fade = Math.min(fade, (bottom - y - 8) / 12F);
                graphics.text(font, lines.get(i), x, y, MoonUi.alpha(color, Mth.clamp(fade, 0, 1)), false);
            }
        } finally { graphics.disableScissor(); }
    }
    @Override public void extractBackground(MoonGraphics graphics, int x, int y, float partialTick) {}

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
    /** The key that opens this window also closes it, unless a name is being typed. */
    @Override public boolean keyPressed(KeyEvent event) {
        if (!(getFocused() instanceof EditBox) && event.matches(ClientModEvents.OPEN_COMPANIONS)) { onClose(); return true; }
        return super.keyPressed(event);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
