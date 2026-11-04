/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A reading view: chapters down the side and one scrollable page, with paging at the foot. */
public final class CompanionGuideScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.guide.";
    static final List<String> TOPICS = List.of("start", "ritual", "merchant", "recruit", "commands", "inventory", "appearance", "hunt", "upgrade", "revive", "sort");
    private static final int NAV_X = 10, NAV_W = 116, PAGE_X = 134, PAGE_W = 326, PAGE_TOP = 58, VISIBLE_LINES = 17;
    private static int chapter;
    private final List<UiButton> chapters = new ArrayList<>();
    private UiButton previous, next;
    private int scroll, maximumScroll;

    CompanionGuideScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable("screen.howlingwerewolf.companion.guide"), parent, snapshot, selected);
    }
    @Override protected Tab tab() { return Tab.GUIDE; }
    private static ItemLike icon(String topic) {
        return switch (topic) {
            case "start" -> Items.COMPASS;
            case "ritual" -> ModItems.MOONBANE_PEARL.get();
            case "merchant" -> Items.EMERALD;
            case "recruit" -> ModItems.ALPHA_WEREWOLF_BADGE.get();
            case "commands" -> Items.LEAD;
            case "inventory" -> Items.BUNDLE;
            case "appearance" -> Items.NAME_TAG;
            case "hunt" -> Items.BOW;
            case "upgrade" -> Items.EXPERIENCE_BOTTLE;
            case "revive" -> Items.TOTEM_OF_UNDYING;
            default -> Items.CHEST;
        };
    }
    private static Component topicTitle(int index) { return Component.translatable(KEY + TOPICS.get(index) + ".title"); }
    private void open(int index) {
        chapter = Math.clamp(index, 0, TOPICS.size() - 1);
        scroll = maximumScroll = 0;
        refreshControls();
    }
    @Override protected void build(int left, int top) {
        chapters.clear();
        for (int i = 0; i < TOPICS.size(); i++) {
            int index = i;
            chapters.add(addRenderableWidget(new UiButton(left + NAV_X, top + BODY_TOP + 4 + i * 22, NAV_W, 21, topicTitle(i), Look.GHOST, () -> open(index)).icon(() -> icon(TOPICS.get(index)))));
        }
        previous = addRenderableWidget(new UiButton(left + PAGE_X, top + 258, 120, 22, Component.empty(), Look.SOFT, () -> open(chapter - 1)));
        next = addRenderableWidget(new UiButton(left + PAGE_X + PAGE_W - 120, top + 258, 120, 22, Component.empty(), Look.SOFT, () -> open(chapter + 1)));
    }
    @Override protected void refreshControls() {
        if (next == null) return;
        for (int i = 0; i < chapters.size(); i++) chapters.get(i).selected(i == chapter);
        previous.visible = chapter > 0;
        next.visible = chapter < TOPICS.size() - 1;
        if (previous.visible) previous.setMessage(Component.literal("‹ ").append(topicTitle(chapter - 1)));
        if (next.visible) next.setMessage(topicTitle(chapter + 1).copy().append(" ›"));
    }
    @Override protected void body(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY) {
        int x = left + PAGE_X;
        graphics.fill(left + PAGE_X - 5, top + BODY_TOP + 4, left + PAGE_X - 4, top + HEIGHT - 10, CompanionUi.LINE);
        graphics.item(new ItemStack(icon(TOPICS.get(chapter))), x, top + BODY_TOP + 2);
        graphics.text(font, topicTitle(chapter), x + 22, top + BODY_TOP + 7, CompanionUi.MOON, false);
        var lines = font.split(Component.translatable(KEY + TOPICS.get(chapter) + ".body"), PAGE_W - 12);
        maximumScroll = Math.max(0, lines.size() - VISIBLE_LINES);
        scroll = Math.clamp(scroll, 0, maximumScroll);
        graphics.enableScissor(x, top + PAGE_TOP, x + PAGE_W, top + PAGE_TOP + VISIBLE_LINES * 11 + 2);
        try {
            for (int line = scroll; line < Math.min(lines.size(), scroll + VISIBLE_LINES); line++) {
                graphics.text(font, lines.get(line), x, top + PAGE_TOP + (line - scroll) * 11, CompanionUi.TEXT, false);
            }
        } finally { graphics.disableScissor(); }
        if (maximumScroll > 0) {
            int track = VISIBLE_LINES * 11, barX = x + PAGE_W - 3;
            int thumb = Math.max(18, track * VISIBLE_LINES / lines.size());
            graphics.fill(barX, top + PAGE_TOP, barX + 2, top + PAGE_TOP + track, CompanionUi.LINE);
            int offset = (track - thumb) * scroll / maximumScroll;
            graphics.fill(barX, top + PAGE_TOP + offset, barX + 2, top + PAGE_TOP + offset + thumb, CompanionUi.VIOLET);
        }
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        double logicalX = x / scale, logicalY = y / scale;
        if (logicalX >= left() + PAGE_X && logicalX < left() + PAGE_X + PAGE_W && logicalY >= top() + PAGE_TOP - 8 && logicalY < top() + 256) {
            scroll = Math.clamp(scroll - (int) Math.signum(dy) * 3, 0, maximumScroll);
            return true;
        }
        return super.mouseScrolled(x, y, dx, dy);
    }
}
