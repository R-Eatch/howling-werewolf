/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.companion.CompanionSortSettings;
import com.howlingwerewolf.companion.CompanionSorting;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** A live map of the work area beside the job's status, method and a single start control. */
public final class CompanionSortingScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.sort.";
    private static final int RADAR_X = 10, RADAR_SIZE = 214, SIDE_X = 234, SIDE_W = 226;
    private UiButton consolidate, merge, start, stop;
    private int delay, surveyTicks;

    CompanionSortingScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable("screen.howlingwerewolf.companion.sort"), parent, snapshot, selected);
    }
    @Override protected Tab tab() { return Tab.SORT; }
    @Override protected void build(int left, int top) {
        int half = (SIDE_W - 4) / 2;
        consolidate = addRenderableWidget(new UiButton(left + SIDE_X, top + 140, half, 24, Component.translatable(KEY + "consolidate"), Look.CHIP, () -> setMethod(true))
                .icon(() -> Items.CHEST).hint(() -> Component.translatable(KEY + "consolidate_desc")));
        merge = addRenderableWidget(new UiButton(left + SIDE_X + half + 4, top + 140, half, 24, Component.translatable(KEY + "merge"), Look.CHIP, () -> setMethod(false))
                .icon(() -> Items.BUNDLE).hint(() -> Component.translatable(KEY + "merge_desc")));
        start = addRenderableWidget(new UiButton(left + SIDE_X, top + 254, SIDE_W, 26, Component.translatable(KEY + "start"), Look.PRIMARY, () -> request(CompanionActionPacket.Action.SORT, "start"))
                .hint(this::startHint));
        stop = addRenderableWidget(new UiButton(left + SIDE_X, top + 254, SIDE_W, 26, Component.translatable(KEY + "stop"), Look.DANGER, () -> request(CompanionActionPacket.Action.REST, "stop")));
        survey();
    }
    private void survey() {
        CompoundTag record = selectedRecord();
        if (selected != null && record != null && mode(record) != CompanionMode.SORT) send(CompanionActionPacket.Action.SORT, selected, "preview");
    }
    private void setMethod(boolean wanted) {
        CompoundTag record = selectedRecord();
        if (record != null && record.getBooleanOr("SortConsolidate", true) != wanted) request(CompanionActionPacket.Action.SORT, "toggle");
    }
    private void request(CompanionActionPacket.Action action, String value) {
        if (selected == null) return;
        send(action, selected, value);
        delay = 5; refreshControls();
    }
    private @Nullable Component startHint() {
        CompoundTag record = selectedRecord();
        if (record == null) return null;
        if (!record.getBooleanOr("SortNearby", false)) return Component.translatable("screen.howlingwerewolf.companion.reason.sort_far");
        return Component.translatable(KEY + "start_hint", CompanionSortSettings.RADIUS);
    }
    @Override public void tick() {
        if (delay > 0) delay--;
        if (++surveyTicks >= 100) { surveyTicks = 0; survey(); }
        super.tick();
    }
    @Override protected void refreshControls() {
        if (start == null) return;
        CompoundTag record = selectedRecord();
        boolean ready = record != null && systemEnabled() && snapshot.getBooleanOr("SortingEnabled", false) && record.getBooleanOr("Enabled", false) && mode(record).hasEntity() && delay == 0;
        boolean sorting = record != null && mode(record) == CompanionMode.SORT;
        start.visible = !sorting; stop.visible = sorting;
        start.active = ready && record.getBooleanOr("SortNearby", false);
        stop.active = ready;
        boolean together = record != null && record.getBooleanOr("SortConsolidate", true);
        consolidate.selected(together); merge.selected(!together);
        consolidate.active = merge.active = ready;
    }
    private static int[] center(CompoundTag record) {
        int[] center = record.getIntArray("SortCenter").orElse(new int[3]);
        return center.length == 3 ? center : new int[3];
    }
    private int radarX(int left, int offset) { return left + RADAR_X + RADAR_SIZE / 2 + Math.round(offset * (RADAR_SIZE / 2F - 7) / CompanionSortSettings.RADIUS); }
    private int radarY(int top, int offset) { return top + BODY_TOP + 4 + RADAR_SIZE / 2 + Math.round(offset * (RADAR_SIZE / 2F - 7) / CompanionSortSettings.RADIUS); }

    @Override protected void body(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY) {
        CompoundTag record = selectedRecord();
        if (record == null) return;
        drawRadar(graphics, left, top, record);
        int x = left + SIDE_X, y = top + BODY_TOP + 4;
        CompanionUi.label(graphics, font, Component.literal(record.getStringOr("Name", "")), x, y + 2, SIDE_W - 90, CompanionUi.TEXT);
        String phase = record.getStringOr("SortPhase", "stopped");
        Component phaseText = Component.translatable(KEY + "phase." + phase);
        CompanionUi.pill(graphics, font, phaseText, x + SIDE_W - font.width(phaseText) - 10, y, phaseColor(phase));
        y += 20;
        CompanionUi.card(graphics, x, y, SIDE_W, 66, CompanionUi.SURFACE, CompanionUi.LINE);
        int moved = record.getIntOr("SortMoved", 0), remaining = record.getIntOr("SortRemaining", 0);
        int usable = 0, skipped = 0;
        ListTag containers = record.getListOrEmpty("SortContainers");
        for (int i = 0; i < containers.size(); i++) { if (containers.getCompoundOrEmpty(i).getBooleanOr("Usable", false)) usable++; else skipped++; }
        CompanionUi.label(graphics, font, Component.translatable(KEY + "found", usable, skipped), x + 8, y + 8, SIDE_W - 16, CompanionUi.TEXT);
        CompanionUi.bar(graphics, x + 8, y + 24, SIDE_W - 16, 8, moved + remaining == 0 ? (phase.equals("done") ? 1 : 0) : moved / (float) (moved + remaining), CompanionUi.MOON);
        CompanionUi.label(graphics, font, Component.translatable(KEY + "moved", moved, remaining), x + 8, y + 38, SIDE_W - 16, CompanionUi.MUTED);
        CompanionUi.label(graphics, font, Component.translatable(KEY + "limit", CompanionSorting.SCAN_LIMIT), x + 8, y + 51, SIDE_W - 16, CompanionUi.FAINT);
        CompanionUi.section(graphics, font, Component.translatable("screen.howlingwerewolf.companion.section.method"), x, top + 128, SIDE_W);
        CompanionUi.wrapped(graphics, font, Component.translatable(KEY + (record.getBooleanOr("SortConsolidate", true) ? "consolidate_desc" : "merge_desc")), x, top + 168, SIDE_W, CompanionUi.MUTED, 2);
        CompanionUi.section(graphics, font, Component.translatable("screen.howlingwerewolf.companion.section.how"), x, top + 195, SIDE_W);
        CompanionUi.wrapped(graphics, font, Component.translatable(KEY + "note"), x, top + 207, SIDE_W, CompanionUi.MUTED, 4);
    }
    private static int phaseColor(String phase) {
        return switch (phase) {
            case "walking", "opening", "moving", "closing" -> CompanionUi.MOON;
            case "done" -> CompanionUi.GOOD;
            case "paused" -> CompanionUi.WARN;
            default -> CompanionUi.MUTED;
        };
    }
    private void drawRadar(GuiGraphicsExtractor graphics, int left, int top, CompoundTag record) {
        int x = left + RADAR_X, y = top + BODY_TOP + 4, cx = x + RADAR_SIZE / 2, cy = y + RADAR_SIZE / 2, radius = RADAR_SIZE / 2 - 7;
        CompanionUi.card(graphics, x, y, RADAR_SIZE, RADAR_SIZE, 0xFF12101B, CompanionUi.LINE);
        CompanionUi.disc(graphics, cx, cy, radius, 0xFF191725);
        graphics.fill(cx - radius, cy, cx + radius + 1, cy + 1, 0xFF262338);
        graphics.fill(cx, cy - radius, cx + 1, cy + radius + 1, 0xFF262338);
        CompanionUi.ring(graphics, cx, cy, radius, CompanionUi.mix(CompanionUi.LINE, CompanionUi.MOON, 0.45F));
        CompanionUi.ring(graphics, cx, cy, radius / 2, CompanionUi.LINE);
        boolean sorting = mode(record) == CompanionMode.SORT;
        if (sorting) {
            // A slow pulse expands from the anchor while the job runs.
            float pulse = (Util.getMillis() % 2200) / 2200F;
            CompanionUi.ring(graphics, cx, cy, Math.round(radius * pulse), CompanionUi.mix(CompanionUi.MOON, 0xFF191725, pulse));
        }
        graphics.text(font, "N", cx - 2, y + 2, CompanionUi.FAINT, false);
        graphics.text(font, CompanionSortSettings.RADIUS + "m", cx + radius - font.width(CompanionSortSettings.RADIUS + "m") + 2, y + RADAR_SIZE - 11, CompanionUi.FAINT, false);
        int[] center = center(record);
        int[] at = record.getIntArray("SortAt").orElse(new int[0]);
        ListTag containers = record.getListOrEmpty("SortContainers");
        for (int i = 0; i < containers.size(); i++) {
            CompoundTag entry = containers.getCompoundOrEmpty(i);
            int px = radarX(left, entry.getIntOr("X", 0) - center[0]), py = radarY(top, entry.getIntOr("Z", 0) - center[2]);
            boolean usable = entry.getBooleanOr("Usable", false);
            boolean current = at.length == 3 && entry.getIntOr("X", 0) == at[0] && entry.getIntOr("Y", 0) == at[1] && entry.getIntOr("Z", 0) == at[2];
            int color = current ? CompanionUi.TEXT : usable ? CompanionUi.MOON : CompanionUi.FAINT;
            CompanionUi.box(graphics, px - 2, py - 2, 5, 5, color);
            if (current) graphics.outline(px - 4, py - 4, 9, 9, CompanionUi.mix(CompanionUi.MOON, 0x00000000, (Util.getMillis() % 800) / 800F));
        }
        graphics.fill(cx - 3, cy - 1, cx + 4, cy + 2, CompanionUi.VIOLET);
        graphics.fill(cx - 1, cy - 3, cx + 2, cy + 4, CompanionUi.VIOLET);
        if (containers.isEmpty()) graphics.centeredText(font, Component.translatable(KEY + "radar_empty"), cx, cy + 12, CompanionUi.FAINT);
    }
    @Override protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) {
        CompoundTag record = selectedRecord();
        if (record == null) return null;
        int[] center = center(record);
        ListTag containers = record.getListOrEmpty("SortContainers");
        for (int i = 0; i < containers.size(); i++) {
            CompoundTag entry = containers.getCompoundOrEmpty(i);
            int px = radarX(left, entry.getIntOr("X", 0) - center[0]), py = radarY(top, entry.getIntOr("Z", 0) - center[2]);
            if (Mth.abs(mouseX - px) <= 3 && Mth.abs(mouseY - py) <= 3) {
                return Component.translatable(KEY + (entry.getBooleanOr("Usable", false) ? "dot_ready" : "dot_skipped"),
                        entry.getIntOr("X", 0), entry.getIntOr("Y", 0), entry.getIntOr("Z", 0));
            }
        }
        return null;
    }
}
