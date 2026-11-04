/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/** Moonlit palette and flat, softly animated controls shared by every companion page. */
final class CompanionUi {
    static final int PANEL = 0xF2141220, SURFACE = 0xFF1E1B2C, RAISED = 0xFF2A2640, LINE = 0xFF39344F;
    static final int MOON = 0xFFEBC97F, VIOLET = 0xFFA18CEB, TEXT = 0xFFF3F0FA, MUTED = 0xFFADA8C1, FAINT = 0xFF76708E;
    static final int GOOD = 0xFF74D19E, WARN = 0xFFE8AC62, DANGER = 0xFFE36C80, INK = 0xFF16131F;

    /** A rectangle with chamfered corners. The rows never overlap, so translucent fills stay even. */
    static void box(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 1, y, x + width - 1, y + 1, color);
        graphics.fill(x, y + 1, x + width, y + height - 1, color);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
    }
    static void card(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int fill, int line) {
        box(graphics, x, y, width, height, line);
        box(graphics, x + 1, y + 1, width - 2, height - 2, fill);
    }
    static void bar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float ratio, int color) {
        box(graphics, x, y, width, height, 0xFF0F0D17);
        int filled = Math.round((width - 2) * Mth.clamp(ratio, 0, 1));
        if (filled > 0) box(graphics, x + 1, y + 1, filled, height - 2, color);
    }
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
    static void label(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color) {
        graphics.text(font, fit(font, text.getString(), width), x, y, color, false);
    }
    static int wrapped(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int color, int maxLines) {
        int lines = 0;
        for (var line : font.split(text, width)) {
            if (lines++ >= maxLines) break;
            graphics.text(font, line, x, y, color, false);
            y += 11;
        }
        return y;
    }
    /** Returns the width used, so pills can flow in a row. */
    static int pill(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        int width = font.width(text) + 10;
        box(graphics, x, y, width, 13, mix(0xFF000000, color, 0.22F));
        graphics.text(font, text, x + 5, y + 3, color, false);
        return width;
    }
    /** Small uppercase-style heading with a hairline to the column edge. */
    static void section(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width) {
        graphics.text(font, text, x, y, FAINT, false);
        int start = x + font.width(text) + 6;
        if (start < x + width) graphics.fill(start, y + 4, x + width, y + 5, LINE);
    }
    static void disc(GuiGraphicsExtractor graphics, int centerX, int centerY, int radius, int color) {
        for (int row = -radius; row <= radius; row++) {
            int half = (int) Math.sqrt(radius * radius - row * row);
            graphics.fill(centerX - half, centerY + row, centerX + half + 1, centerY + row + 1, color);
        }
    }
    static void ring(GuiGraphicsExtractor graphics, int centerX, int centerY, int radius, int color) {
        int steps = Math.max(24, radius * 6);
        for (int step = 0; step < steps; step++) {
            double angle = step * Mth.TWO_PI / steps;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius), y = centerY + (int) Math.round(Math.sin(angle) * radius);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    enum Look { PRIMARY, SOFT, GHOST, DANGER, TAB, TILE, CARD, CHIP }

    /** Flat button with an eased hover, an optional item icon, and a hint explaining what it does or what it needs. */
    static final class UiButton extends AbstractButton {
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

        @Override public void onPress(InputWithModifiers input) { if (active) action.run(); }

        @Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            long now = Util.getMillis();
            float step = Math.min(now - lastFrame, 80) / 90F;
            lastFrame = now;
            glow = Mth.clamp(glow + (active && isHovered() ? step : -step), 0, 1);
            Font font = Minecraft.getInstance().font;
            int x = getX(), y = getY(), width = getWidth(), height = getHeight();
            int ink = active ? TEXT : FAINT;
            switch (look) {
                case PRIMARY -> {
                    int base = active ? mix(MOON, 0xFFFFE6AE, glow) : RAISED;
                    box(graphics, x, y, width, height, base);
                    ink = active ? INK : FAINT;
                }
                case DANGER -> {
                    box(graphics, x, y, width, height, active ? mix(0xFF4A2030, 0xFF6A2B40, glow) : RAISED);
                    ink = active ? 0xFFFFD9DF : FAINT;
                }
                case SOFT -> card(graphics, x, y, width, height, mix(SURFACE, RAISED, glow), mix(LINE, VIOLET, glow));
                case CHIP, TILE, CARD -> {
                    int fill = selected ? mix(0xFF000000, MOON, 0.16F) : mix(SURFACE, RAISED, glow);
                    int edge = selected ? MOON : mix(LINE, VIOLET, glow);
                    card(graphics, x, y, width, height, fill, active || selected ? edge : mix(LINE, 0xFF000000, 0.3F));
                }
                case GHOST -> { if (selected || glow > 0) box(graphics, x, y, width, height, mix(0x00000000, selected ? RAISED : SURFACE, selected ? 1 : glow)); }
                case TAB -> { ink = selected ? MOON : active ? mix(MUTED, TEXT, glow) : FAINT; }
            }
            if (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) graphics.outline(x, y, width, height, VIOLET);
            if (look == Look.CARD) return;
            boolean hasIcon = icon != null;
            if (hasIcon && iconStack.isEmpty()) iconStack = new ItemStack(icon.get());
            if (getMessage().getString().isEmpty()) {
                if (hasIcon) graphics.item(iconStack, x + (width - 16) / 2, y + (height - 16) / 2);
                return;
            }
            if (look == Look.TILE) {
                if (hasIcon) graphics.item(iconStack, x + (width - 16) / 2, y + 3);
                String text = fit(font, getMessage().getString(), width - 2);
                graphics.text(font, text, x + (width - font.width(text)) / 2, y + height - 12, selected ? MOON : ink, false);
                return;
            }
            int room = width - 8 - (hasIcon ? 18 : 0);
            String text = fit(font, getMessage().getString(), room);
            int contentWidth = font.width(text) + (hasIcon ? 18 : 0);
            int cursor = look == Look.GHOST ? x + 4 : x + (width - contentWidth) / 2;
            if (hasIcon) { graphics.item(iconStack, cursor, y + (height - 16) / 2); cursor += 18; }
            graphics.text(font, text, cursor, y + (height - 8) / 2, look == Look.GHOST && selected ? MOON : ink, false);
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }

    private CompanionUi() {}
}
