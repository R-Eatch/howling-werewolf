/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/** Text helpers and the softly animated controls of the companion window. */
final class CompanionUi {
    static final int ROW_RADIUS = 12;

    static int mix(int from, int to, float amount) {
        int a = Math.round(Mth.lerp(amount, from >>> 24, to >>> 24));
        int r = Math.round(Mth.lerp(amount, from >> 16 & 255, to >> 16 & 255));
        int g = Math.round(Mth.lerp(amount, from >> 8 & 255, to >> 8 & 255));
        int b = Math.round(Mth.lerp(amount, from & 255, to & 255));
        return a << 24 | r << 16 | g << 8 | b;
    }
    static String fit(Font font, String text, int width) {
        return font.width(text) <= width ? text : font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }
    static void label(MoonGraphics graphics, Font font, Component text, int x, int y, int width, int color) {
        graphics.text(font, fit(font, text.getString(), width), x, y, color, false);
    }
    static int wrapped(MoonGraphics graphics, Font font, Component text, int x, int y, int width, int color, int maxLines) {
        int lines = 0;
        for (var line : font.split(text, width)) {
            if (lines++ >= maxLines) break;
            graphics.text(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }
    /** A small heading with a hairline running to the column edge. */
    static void section(MoonGraphics graphics, Font font, Component text, int x, int y, int width, MoonUi.Theme theme) {
        graphics.text(font, text, x, y, theme.faint(), false);
        int start = x + font.width(text) + 6;
        if (start < x + width) graphics.fill(start, y + 4, x + width, y + 5, theme.line());
    }
    /** A slim meter on the canvas. */
    static void meter(MoonGraphics graphics, int x, int y, int width, int height, float ratio, int color, MoonUi.Theme theme) {
        MoonUi.round(graphics, x, y, width, height, height / 2, theme.line());
        int filled = Math.round(width * Mth.clamp(ratio, 0, 1));
        if (filled >= height) MoonUi.round(graphics, x, y, filled, height, height / 2, color);
    }
    /** A small rounded tag for a state; returns its width. */
    static int tag(MoonGraphics graphics, Font font, Component text, int x, int y, int color) {
        int width = font.width(text) + 10;
        MoonUi.round(graphics, x, y, width, 13, 6, MoonUi.alpha(color, 0.16F));
        graphics.text(font, text, x + 5, y + 3, color, false);
        return width;
    }

    enum Look {
        /** Pills: solid blood for a page's main action, dark red for stopping one, an outline for the rest. */
        PRIMARY, DANGER, SOFT,
        /** One word in the upper band. */
        TAB,
        /** A plain row in a list on the canvas. */
        GHOST,
        /** A small choice on the canvas. */
        CHIP,
        /** A row led by a disc holding its icon. */
        ROW
    }

    /** Flat button with an eased hover, an optional item icon, and a hint for the lower band to read out. */
    static final class UiButton extends MoonInput.Button {
        private final Runnable action;
        private final Look look;
        private Supplier<? extends ItemLike> icon;
        private ItemStack iconStack = ItemStack.EMPTY;
        private Supplier<Component> hint = () -> null;
        private boolean selected;
        private float glow;
        private long lastFrame = Util.getMillis();

        UiButton(int x, int y, int width, int height, Component message, Look look, Runnable action) {
            super(x, y, width, height, message);
            this.look = look;
            this.action = action;
        }
        UiButton icon(Supplier<? extends ItemLike> item) { icon = item; return this; }
        UiButton hint(Supplier<Component> text) { hint = text; return this; }
        void selected(boolean value) { selected = value; }
        boolean selected() { return selected; }
        Component hintText() { return hint.get(); }
        Look look() { return look; }
        float glow() { return glow; }

        @Override void press() { if (active) action.run(); }

        @Override protected void extractContents(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
            long now = Util.getMillis();
            float step = Math.min(now - lastFrame, 80) / 90F;
            lastFrame = now;
            glow = Mth.clamp(glow + (active && isHovered() ? step : -step), 0, 1);
            Font font = Minecraft.getInstance().font;
            MoonUi.Theme theme = MoonUi.theme();
            int x = getX(), y = getY(), width = getWidth(), height = getHeight();
            if (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) graphics.outline(x - 1, y - 1, width + 2, height + 2, theme.accent());
            if (look == Look.ROW) { drawRow(graphics, font, theme); return; }
            int ink;
            switch (look) {
                case PRIMARY -> {
                    MoonUi.round(graphics, x, y, width, height, height / 2, active ? MoonUi.mix(MoonUi.BLOOD, 0xFFE9506A, glow) : 0xFF2B2930);
                    ink = active ? 0xFFFFFFFF : MoonUi.BAND_FAINT;
                }
                case DANGER -> {
                    MoonUi.round(graphics, x, y, width, height, height / 2, active ? MoonUi.mix(0xFF4A2030, 0xFF6A2B40, glow) : 0xFF2B2930);
                    ink = active ? 0xFFFFD9DF : MoonUi.BAND_FAINT;
                }
                case SOFT -> {
                    MoonUi.round(graphics, x, y, width, height, height / 2, active ? MoonUi.mix(0xFF4B4852, MoonUi.BAND_TEXT, glow) : 0xFF2B2930);
                    MoonUi.round(graphics, x + 1, y + 1, width - 2, height - 2, height / 2 - 1, active ? MoonUi.mix(MoonUi.BAND, 0xFF2B2930, glow) : MoonUi.BAND);
                    ink = active ? MoonUi.BAND_TEXT : MoonUi.BAND_FAINT;
                }
                case CHIP -> {
                    int edge = selected ? theme.accent() : MoonUi.mix(theme.line(), theme.ink(), glow);
                    MoonUi.round(graphics, x, y, width, height, 5, active || selected ? edge : MoonUi.alpha(theme.line(), 0.6F));
                    MoonUi.round(graphics, x + 1, y + 1, width - 2, height - 2, 4, theme.node());
                    ink = selected ? theme.accent() : active ? theme.ink() : theme.faint();
                }
                case GHOST -> {
                    if (selected) graphics.fill(x, y + 3, x + 2, y + height - 3, theme.accent());
                    ink = selected ? theme.accent() : MoonUi.mix(theme.sub(), theme.ink(), glow);
                }
                default -> ink = !active ? MoonUi.BAND_FAINT : selected ? MoonUi.BAND_TEXT : MoonUi.mix(MoonUi.BAND_SUB, MoonUi.BAND_TEXT, glow);
            }
            boolean hasIcon = icon != null;
            if (hasIcon) refreshIcon();
            if (getMessage().getString().isEmpty()) {
                if (hasIcon) graphics.item(iconStack, x + (width - 16) / 2, y + (height - 16) / 2);
                return;
            }
            int room = width - 8 - (hasIcon ? 18 : 0);
            String text = fit(font, getMessage().getString(), room);
            int contentWidth = font.width(text) + (hasIcon ? 18 : 0);
            int cursor = look == Look.GHOST ? x + 6 : x + (width - contentWidth) / 2;
            if (hasIcon) { graphics.item(iconStack, cursor, y + (height - 16) / 2); cursor += 18; }
            graphics.text(font, text, cursor, y + (height - 8) / 2, ink, false);
        }
        private void refreshIcon() {
            ItemLike item = icon.get();
            if (iconStack.isEmpty() || !iconStack.is(item.asItem())) iconStack = new ItemStack(item);
        }
        private void drawRow(MoonGraphics graphics, Font font, MoonUi.Theme theme) {
            float x = getX() + ROW_RADIUS + 1, y = getY() + getHeight() / 2F;
            float radius = ROW_RADIUS + (selected ? 0.8F : 0) + glow * 1.2F;
            MoonUi.pool(graphics, x, y + 3, radius * 1.5F, radius * 1.5F, theme.shadow());
            MoonUi.disc(graphics, x, y, radius, theme.node());
            int edge = selected ? theme.accent() : active ? MoonUi.mix(theme.sub(), theme.ink(), glow) : theme.line();
            MoonUi.ring(graphics, x, y, radius, edge);
            if (selected) MoonUi.ring(graphics, x, y, radius - 0.8F, edge);
            if (icon != null) {
                refreshIcon();
                graphics.item(iconStack, Math.round(x) - 8, Math.round(y) - 8);
            }
            if (!active && !selected) MoonUi.disc(graphics, x, y, radius - 1, MoonUi.alpha(theme.node(), 0.62F));
            if (!getMessage().getString().isEmpty()) {
                int ink = selected ? theme.accent() : active ? MoonUi.mix(theme.ink(), theme.accent(), glow * 0.6F) : theme.faint();
                label(graphics, font, getMessage(), getX() + ROW_RADIUS * 2 + 7, Math.round(y) - 4, getWidth() - ROW_RADIUS * 2 - 7, ink);
            }
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }

    /** NBT reads with a fallback for absent keys. */
    static String str(CompoundTag tag, String key, String fallback) { return tag.contains(key) ? tag.getString(key) : fallback; }
    static boolean flag(CompoundTag tag, String key, boolean fallback) { return tag.contains(key) ? tag.getBoolean(key) : fallback; }
    static int integer(CompoundTag tag, String key, int fallback) { return tag.contains(key) ? tag.getInt(key) : fallback; }
    static long lng(CompoundTag tag, String key, long fallback) { return tag.contains(key) ? tag.getLong(key) : fallback; }
    static float num(CompoundTag tag, String key, float fallback) { return tag.contains(key) ? tag.getFloat(key) : fallback; }

    private CompanionUi() {}
}
