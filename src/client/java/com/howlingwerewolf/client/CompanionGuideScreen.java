/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A reading view: chapters down the side and one scrollable page, with paging in the lower band. */
public final class CompanionGuideScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.guide.";
    static final List<String> TOPICS = List.of("start", "ritual", "merchant", "recruit", "commands", "inventory", "appearance", "hunt", "upgrade", "revive", "sort");
    private static final int NAV_X = 12, NAV_W = 126, NAV_TOP = 28, NAV_STEP = 21, PAGE_X = 150, PAGE_W = 356, PAGE_TOP = 52;
    private static int chapter;
    private final List<UiButton> chapters = new ArrayList<>();
    private UiButton previous, next;
    private int scroll, maximumScroll;
    private float shown;

    CompanionGuideScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable("screen.howlingwerewolf.companion.guide"), parent, snapshot, selected);
    }
    @Override protected Tab tab() { return Tab.GUIDE; }
    @Override protected int actionsWidth() { return WIDTH - 24; }
    @Override protected Focus focus() { return new Focus(null, null); }
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
        chapter = Mth.clamp(index, 0, TOPICS.size() - 1);
        scroll = maximumScroll = 0;
        shown = 0;
        refreshControls();
    }
    @Override protected void build(int left, int top) {
        chapters.clear();
        for (int i = 0; i < TOPICS.size(); i++) {
            int index = i;
            chapters.add(addRenderableWidget(new UiButton(left + NAV_X, top + NAV_TOP + i * NAV_STEP, NAV_W, NAV_STEP, topicTitle(i), Look.GHOST, () -> open(index)).icon(() -> icon(TOPICS.get(index)))));
        }
        int y = top + HEIGHT - LOWER_BAND + 12;
        previous = addRenderableWidget(new UiButton(left + 12, y, 150, 22, Component.empty(), Look.SOFT, () -> open(chapter - 1)));
        next = addRenderableWidget(new UiButton(left + WIDTH - 12 - 150, y, 150, 22, Component.empty(), Look.SOFT, () -> open(chapter + 1)));
    }
    @Override protected void refreshControls() {
        if (next == null) return;
        for (int i = 0; i < chapters.size(); i++) chapters.get(i).selected(i == chapter);
        previous.visible = chapter > 0;
        next.visible = chapter < TOPICS.size() - 1;
        if (previous.visible) previous.setMessage(Component.literal("‹ ").append(topicTitle(chapter - 1)));
        if (next.visible) next.setMessage(topicTitle(chapter + 1).copy().append(" ›"));
    }
    @Override protected void body(MoonGraphics graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY) {
        int x = left + PAGE_X, bottom = top + HEIGHT - LOWER_BAND - 6;
        // A faint moon sits behind the page, so the guide stays part of the same window.
        MoonUi.moon(graphics, x + PAGE_W - 60, top + 128, 92, 2, theme, 0.28F);
        graphics.fill(x - 7, top + TOP_BAND + 6, x - 6, bottom, theme.line());
        graphics.item(new ItemStack(icon(TOPICS.get(chapter))), x, top + TOP_BAND + 4);
        graphics.text(font, topicTitle(chapter), x + 21, top + TOP_BAND + 8, theme.accent(), false);
        var lines = font.split(Component.translatable(KEY + TOPICS.get(chapter) + ".body"), PAGE_W - 14);
        int height = bottom - (top + PAGE_TOP);
        maximumScroll = Math.max(0, lines.size() * 11 - height);
        scroll = Mth.clamp(scroll, 0, maximumScroll);
        shown = Mth.lerp(0.28F, shown, scroll);
        reading(graphics, lines, x, PAGE_W - 8, top + PAGE_TOP, bottom, shown, maximumScroll, theme.ink());
        if (maximumScroll > 0) {
            int trackX = x + PAGE_W - 3;
            graphics.fill(trackX, top + PAGE_TOP, trackX + 1, bottom, theme.line());
            int thumb = Math.max(18, height * height / (lines.size() * 11));
            int thumbY = top + PAGE_TOP + Math.round((height - thumb) * shown / maximumScroll);
            MoonUi.round(graphics, trackX - 1, thumbY, 3, thumb, 1, theme.accent());
        }
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        double logicalX = x / scale, logicalY = y / scale;
        if (logicalX >= left() + PAGE_X && logicalX < left() + PAGE_X + PAGE_W && logicalY >= top() + TOP_BAND && logicalY < top() + HEIGHT - LOWER_BAND) {
            scroll = Mth.clamp(scroll - (int) Math.signum(dy) * 33, 0, maximumScroll);
            return true;
        }
        return super.mouseScrolled(x, y, dx, dy);
    }
}
