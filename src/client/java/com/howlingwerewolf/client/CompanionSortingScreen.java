/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.companion.CompanionSortSettings;
import com.howlingwerewolf.companion.CompanionSorting;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The moon becomes a live map of the work area, beside the job's status, method and one start control. */
public final class CompanionSortingScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.sort.";
    private static final int RADAR_RADIUS = 100, RADAR_X = 130, SIDE_X = 262, SIDE_W = 246, RING_TICKS = 72, ACTION_W = 130;
    private static final int DOT = 0xFF17151C, DOT_SKIPPED = 0xFF9B968F, DOT_FRAMED = 0xFF2F8F5B;
    private UiButton consolidate, merge, start, stop;
    private int delay, surveyTicks;

    CompanionSortingScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable("screen.howlingwerewolf.companion.sort"), parent, snapshot, selected);
    }
    @Override protected Tab tab() { return Tab.SORT; }
    @Override protected int actionsWidth() { return ACTION_W + 10; }
    private static int radarY(int top) { return top + (TOP_BAND + HEIGHT - LOWER_BAND) / 2; }
    @Override protected void build(int left, int top) {
        int half = (SIDE_W - 4) / 2;
        consolidate = addRenderableWidget(new UiButton(left + SIDE_X, top + 112, half, 22, Component.translatable(KEY + "consolidate"), Look.CHIP, () -> setMethod(true))
                .icon(() -> Items.CHEST).hint(() -> Component.translatable(KEY + "consolidate_desc")));
        merge = addRenderableWidget(new UiButton(left + SIDE_X + half + 4, top + 112, half, 22, Component.translatable(KEY + "merge"), Look.CHIP, () -> setMethod(false))
                .icon(() -> Items.BUNDLE).hint(() -> Component.translatable(KEY + "merge_desc")));
        int actionX = left + WIDTH - 12 - ACTION_W, actionY = top + HEIGHT - LOWER_BAND + 10;
        start = addRenderableWidget(new UiButton(actionX, actionY, ACTION_W, 26, Component.translatable(KEY + "start"), Look.PRIMARY, () -> request(CompanionActionPacket.Action.SORT, "start"))
                .hint(this::startHint));
        stop = addRenderableWidget(new UiButton(actionX, actionY, ACTION_W, 26, Component.translatable(KEY + "stop"), Look.DANGER, () -> request(CompanionActionPacket.Action.REST, "stop")));
        survey();
    }
    private void survey() {
        CompoundTag record = selectedRecord();
        if (selected != null && record != null && mode(record) != CompanionMode.SORT) send(CompanionActionPacket.Action.SORT, selected, "preview");
    }
    private void setMethod(boolean wanted) {
        CompoundTag record = selectedRecord();
        if (record != null && CompanionUi.flag(record, "SortConsolidate", true) != wanted) request(CompanionActionPacket.Action.SORT, "toggle");
    }
    private void request(CompanionActionPacket.Action action, String value) {
        if (selected == null) return;
        send(action, selected, value);
        delay = 5; refreshControls();
    }
    private @Nullable Component startHint() {
        CompoundTag record = selectedRecord();
        if (record == null) return null;
        if (!record.getBoolean("SortNearby")) return Component.translatable("screen.howlingwerewolf.companion.reason.sort_far");
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
        boolean ready = record != null && systemEnabled() && snapshot.getBoolean("SortingEnabled") && record.getBoolean("Enabled") && mode(record).hasEntity() && delay == 0;
        boolean sorting = record != null && mode(record) == CompanionMode.SORT;
        start.visible = !sorting; stop.visible = sorting;
        start.active = ready && record.getBoolean("SortNearby");
        stop.active = ready;
        boolean together = record != null && CompanionUi.flag(record, "SortConsolidate", true);
        consolidate.selected(together); merge.selected(!together);
        consolidate.active = merge.active = ready;
    }
    private static int[] center(CompoundTag record) {
        int[] center = record.getIntArray("SortCenter"); if (center.length < 3) center = new int[3];
        return center.length == 3 ? center : new int[3];
    }
    private static int mapX(int left, int offset) { return left + RADAR_X + Math.round(offset * (RADAR_RADIUS - 8F) / CompanionSortSettings.RADIUS); }
    private static int mapY(int top, int offset) { return radarY(top) + Math.round(offset * (RADAR_RADIUS - 8F) / CompanionSortSettings.RADIUS); }

    @Override protected Focus focus() {
        CompoundTag record = selectedRecord();
        return new Focus(null, record == null ? null : Component.translatable(KEY + "note"));
    }

    @Override protected void body(MoonGraphics graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY) {
        CompoundTag record = selectedRecord();
        if (record == null) return;
        drawRadar(graphics, theme, left, top, record);
        int x = left + SIDE_X, y = top + TOP_BAND + 8;
        CompanionUi.label(graphics, font, Component.literal(record.getString("Name")), x, y + 2, SIDE_W - 90, theme.ink());
        String phase = CompanionUi.str(record, "SortPhase", "stopped");
        Component phaseText = Component.translatable(KEY + "phase." + phase);
        CompanionUi.tag(graphics, font, phaseText, x + SIDE_W - font.width(phaseText) - 10, y, phaseColor(phase, theme));
        y += 18;
        int moved = record.getInt("SortMoved"), remaining = record.getInt("SortRemaining");
        int usable = 0, skipped = 0;
        ListTag containers = record.getList("SortContainers", 10);
        for (int i = 0; i < containers.size(); i++) { if (containers.getCompound(i).getBoolean("Usable")) usable++; else skipped++; }
        CompanionUi.label(graphics, font, Component.translatable(KEY + "found", usable, skipped), x, y, SIDE_W, theme.ink());
        CompanionUi.label(graphics, font, Component.translatable(KEY + "moved", moved, remaining), x, y + 11, SIDE_W, theme.sub());
        CompanionUi.label(graphics, font, Component.translatable(KEY + "limit", CompanionSorting.SCAN_LIMIT), x, y + 22, SIDE_W, theme.faint());
        CompanionUi.section(graphics, font, Component.translatable("screen.howlingwerewolf.companion.section.method"), x, top + 100, SIDE_W, theme);
        CompanionUi.wrapped(graphics, font, Component.translatable(KEY + (CompanionUi.flag(record, "SortConsolidate", true) ? "consolidate_desc" : "merge_desc")), x, top + 140, SIDE_W, theme.sub(), 2);
    }
    private static int phaseColor(String phase, MoonUi.Theme theme) {
        return switch (phase) {
            case "walking", "opening", "moving", "closing" -> theme.accent();
            case "done" -> MoonUi.tint(0xFF2F8F5B, 0xFF74D19E);
            case "paused" -> MoonUi.tint(0xFFB9791F, 0xFFE8AC62);
            default -> theme.sub();
        };
    }
    private void drawRadar(MoonGraphics graphics, MoonUi.Theme theme, int left, int top, CompoundTag record) {
        int cx = left + RADAR_X, cy = radarY(top), radius = RADAR_RADIUS;
        MoonUi.glow(graphics, cx, cy, radius * 1.7F, theme.bloom());
        MoonUi.moon(graphics, cx, cy, radius, 0, theme);
        // The map rings stay dark in both palettes because the moon behind them is pale in both.
        MoonUi.circle(graphics, cx, cy, radius / 2F, 0.7F, 0x40000000);
        MoonUi.line(graphics, cx - radius + 6, cy, cx + radius - 6, cy, 0.7F, 0x2C000000);
        MoonUi.line(graphics, cx, cy - radius + 6, cx, cy + radius - 6, 0.7F, 0x2C000000);
        int moved = record.getInt("SortMoved"), remaining = record.getInt("SortRemaining");
        String phase = CompanionUi.str(record, "SortPhase", "stopped");
        float progress = moved + remaining == 0 ? phase.equals("done") ? 1 : 0 : moved / (float) (moved + remaining);
        int lit = Math.round(progress * RING_TICKS);
        float ring = radius + 5;
        for (int i = 0; i < RING_TICKS; i++) {
            float angle = i * Mth.TWO_PI / RING_TICKS - Mth.HALF_PI, cos = Mth.cos(angle), sin = Mth.sin(angle);
            boolean on = i < lit;
            float outer = ring + (on ? 5 : 3);
            MoonUi.line(graphics, cx + cos * ring, cy + sin * ring, cx + cos * outer, cy + sin * outer, on ? 1.4F : 0.8F, on ? theme.accent() : theme.line());
        }
        if (mode(record) == CompanionMode.SORT) {
            // A slow pulse expands from the anchor while the job runs.
            float pulse = (Util.getMillis() % 2200) / 2200F;
            MoonUi.circle(graphics, cx, cy, Math.max(1, radius * pulse), 0.9F, MoonUi.alpha(MoonUi.BLOOD, (1 - pulse) * 0.6F));
        }
        graphics.text(font, "N", cx - 2, cy - radius + 5, 0x80000000, false);
        int[] center = center(record);
        int[] at = record.getIntArray("SortAt");
        ListTag containers = record.getList("SortContainers", 10);
        int framed = 0;
        for (int i = 0; i < containers.size(); i++) {
            CompoundTag entry = containers.getCompound(i);
            int px = mapX(left, entry.getInt("X") - center[0]), py = mapY(top, entry.getInt("Z") - center[2]);
            boolean usable = entry.getBoolean("Usable");
            boolean holdsOneKind = !entry.getList("Frames", 8).isEmpty();
            if (holdsOneKind) framed++;
            boolean current = at.length == 3 && entry.getInt("X") == at[0] && entry.getInt("Y") == at[1] && entry.getInt("Z") == at[2];
            int color = current ? MoonUi.BLOOD : !usable ? DOT_SKIPPED : holdsOneKind ? DOT_FRAMED : DOT;
            MoonUi.disc(graphics, px, py, current ? 3.4F : 2.6F, color);
            if (current) MoonUi.ring(graphics, px, py, 6, MoonUi.alpha(MoonUi.BLOOD, 1 - (Util.getMillis() % 800) / 800F));
        }
        MoonUi.line(graphics, cx - 4, cy, cx + 4, cy, 1.4F, MoonUi.BLOOD);
        MoonUi.line(graphics, cx, cy - 4, cx, cy + 4, 1.4F, MoonUi.BLOOD);
        if (containers.isEmpty()) {
            Component empty = Component.translatable(KEY + "radar_empty");
            graphics.text(font, empty, cx - font.width(empty) / 2, cy + 12, 0xB0000000, false);
        }
        int legendY = top + HEIGHT - LOWER_BAND - 13;
        String range = CompanionSortSettings.RADIUS + "m";
        graphics.text(font, range, left + RADAR_X + radius - font.width(range) + 10, legendY, theme.faint(), false);
        if (framed > 0) {
            MoonUi.disc(graphics, left + 14, legendY + 4, 2.6F, DOT_FRAMED);
            graphics.text(font, Component.translatable(KEY + "framed", framed), left + 21, legendY, theme.faint(), false);
        }
    }
    @Override protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) {
        CompoundTag record = selectedRecord();
        if (record == null) return null;
        int[] center = center(record);
        ListTag containers = record.getList("SortContainers", 10);
        for (int i = 0; i < containers.size(); i++) {
            CompoundTag entry = containers.getCompound(i);
            int px = mapX(left, entry.getInt("X") - center[0]), py = mapY(top, entry.getInt("Z") - center[2]);
            if (Mth.abs(mouseX - px) <= 3 && Mth.abs(mouseY - py) <= 3) {
                ListTag frames = entry.getList("Frames", 8);
                if (!frames.isEmpty() && entry.getBoolean("Usable")) {
                    return Component.translatable(KEY + "dot_framed", framedItems(frames), entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
                }
                return Component.translatable(KEY + (entry.getBoolean("Usable") ? "dot_ready" : "dot_skipped"),
                        entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
            }
        }
        return null;
    }
    private static Component framedItems(ListTag frames) {
        List<Component> names = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(frames.getString(i));
            if (id != null) names.add(new ItemStack(BuiltInRegistries.ITEM.get(id)).getHoverName());
        }
        return ComponentUtils.formatList(names, Component.literal(" / "));
    }
}
