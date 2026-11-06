/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.HWClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttributes;

import java.util.function.Supplier;

/**
 * The drawing kit of the moonlit progression screens: two palettes, soft textured shapes that stay
 * smooth at any GUI scale, and flat pill controls. Everything takes float positions through the
 * pose, so shapes can drift and ease without snapping to the pixel grid.
 */
final class MoonUi {
    /** The letterbox bands stay near-black in both palettes; only the canvas between them changes. */
    static final int BAND = 0xFF0E0D12, BAND_TEXT = 0xFFF5F2EC, BAND_SUB = 0xFFB4B0B9, BAND_FAINT = 0xFF6C6872, BLOOD = 0xFFC8324A;

    record Theme(int top, int bottom, int bloom, int ink, int sub, int faint, int line, int accent, int node, int moon, int shade, int shadow, int mote) {}
    static final Theme MOONLIGHT = new Theme(0xFFF4F1EA, 0xFFD9D5CD, 0xE6FFFFFF, 0xFF17151C, 0xFF5D5963, 0xFF8F8B94, 0xFFBDB9B2, 0xFFB3263A,
            0xFFFBFAF7, 0xFFF3EFE6, 0x705A5462, 0x46302A33, 0x70686270);
    static final Theme NIGHTFALL = new Theme(0xFF16131F, 0xFF08070D, 0x3CA18CEB, 0xFFF3F0FA, 0xFFB5B0C7, 0xFF7C7694, 0xFF3F3A57, 0xFFE4566C,
            0xFF1E1B2C, 0xFFEFE6D2, 0xE00F0D18, 0x8C000000, 0xB0F3F0FA);

    private static final Identifier MOON = texture("moon"), PHASE = texture("moon_phase"), SOFT = texture("soft"), DISC = texture("disc"), RING = texture("ring");
    private static Identifier texture(String name) { return Identifier.fromNamespaceAndPath("howlingwerewolf", "textures/gui/moon/" + name + ".png"); }

    static Theme theme() { return HWClientConfig.nightfall() ? NIGHTFALL : MOONLIGHT; }
    /** A colour that needs a different strength on each canvas. */
    static int tint(int onMoonlight, int onNightfall) { return HWClientConfig.nightfall() ? onNightfall : onMoonlight; }

    static int alpha(int color, float amount) {
        return Math.round((color >>> 24) * Mth.clamp(amount, 0, 1)) << 24 | color & 0xFFFFFF;
    }
    static int mix(int from, int to, float amount) { return CompanionUi.mix(from, to, Mth.clamp(amount, 0, 1)); }
    static float smooth(float value) {
        float clamped = Mth.clamp(value, 0, 1);
        return clamped * clamped * (3 - 2 * clamped);
    }
    /** Settles with a slight overshoot, as if arriving under its own weight. */
    static float settle(float value) {
        float t = Mth.clamp(value, 0, 1) - 1;
        return 1 + t * t * (2.2F * t + 1.2F);
    }

    private static void textured(GuiGraphicsExtractor graphics, Identifier texture, float x, float y, float width, float height,
                                 int u, int v, int size, int textureWidth, int textureHeight, int color) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(width / size, height / size);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, u, v, size, size, size, size, textureWidth, textureHeight, color);
        pose.popMatrix();
    }
    static void disc(GuiGraphicsExtractor graphics, float x, float y, float radius, int color) {
        textured(graphics, DISC, x - radius, y - radius, radius * 2, radius * 2, 0, 0, 64, 64, 64, color);
    }
    static void ring(GuiGraphicsExtractor graphics, float x, float y, float radius, int color) {
        textured(graphics, RING, x - radius, y - radius, radius * 2, radius * 2, 0, 0, 128, 128, 128, color);
    }
    static void glow(GuiGraphicsExtractor graphics, float x, float y, float radius, int color) {
        textured(graphics, SOFT, x - radius, y - radius, radius * 2, radius * 2, 0, 0, 128, 128, 128, color);
    }
    /** A glow squashed into an ellipse, for the pool of light under a figure or a contact shadow. */
    static void pool(GuiGraphicsExtractor graphics, float x, float y, float radiusX, float radiusY, int color) {
        textured(graphics, SOFT, x - radiusX, y - radiusY, radiusX * 2, radiusY * 2, 0, 0, 128, 128, 128, color);
    }
    /** Tonight's moon: {@code phase} follows the vanilla order, zero being full. */
    static void moon(GuiGraphicsExtractor graphics, float x, float y, float radius, int phase, Theme theme) { moon(graphics, x, y, radius, phase, theme, 1); }
    static void moon(GuiGraphicsExtractor graphics, float x, float y, float radius, int phase, Theme theme, float strength) {
        textured(graphics, MOON, x - radius, y - radius, radius * 2, radius * 2, 0, 0, 256, 256, 256, alpha(theme.moon(), strength));
        if (phase != 0) textured(graphics, PHASE, x - radius, y - radius, radius * 2, radius * 2, phase % 4 * 128, phase / 4 * 128, 128, 512, 256, alpha(theme.shade(), strength));
    }
    static int moonPhase() {
        var level = Minecraft.getInstance().level;
        return level == null ? 0 : level.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE).index();
    }
    /** A straight stroke of any angle and sub-pixel weight. */
    static void line(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1, float weight, int color) {
        float dx = x1 - x0, dy = y1 - y0;
        float length = Mth.sqrt(dx * dx + dy * dy);
        if (length < 0.01F) return;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x0, y0);
        pose.rotate((float) Math.atan2(dy, dx));
        pose.scale(length, weight);
        pose.translate(0, -0.5F);
        graphics.fill(0, 0, 1, 1, color);
        pose.popMatrix();
    }
    /** A fine circle for large radii, where the ring texture would grow too heavy. */
    static void circle(GuiGraphicsExtractor graphics, float x, float y, float radius, float weight, int color) {
        int steps = Math.max(24, Math.round(radius * 0.8F));
        float lastX = x + radius, lastY = y;
        for (int i = 1; i <= steps; i++) {
            float angle = i * Mth.TWO_PI / steps, nextX = x + Mth.cos(angle) * radius, nextY = y + Mth.sin(angle) * radius;
            line(graphics, lastX, lastY, nextX, nextY, weight, color);
            lastX = nextX; lastY = nextY;
        }
    }
    /** Rounded rectangle; the corners are quadrants of the anti-aliased disc. */
    static void round(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
        int r = Math.min(radius, Math.min(width, height) / 2);
        if (r <= 0) { graphics.fill(x, y, x + width, y + height, color); return; }
        graphics.blit(RenderPipelines.GUI_TEXTURED, DISC, x, y, 0, 0, r, r, 32, 32, 64, 64, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, DISC, x + width - r, y, 32, 0, r, r, 32, 32, 64, 64, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, DISC, x, y + height - r, 0, 32, r, r, 32, 32, 64, 64, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, DISC, x + width - r, y + height - r, 32, 32, r, r, 32, 32, 64, 64, color);
        graphics.fill(x + r, y, x + width - r, y + height, color);
        graphics.fill(x, y + r, x + r, y + height - r, color);
        graphics.fill(x + width - r, y + r, x + width, y + height - r, color);
    }
    /** Text at twice the size or more, for the few words that should carry a page. */
    static void large(GuiGraphicsExtractor graphics, Font font, Component text, float x, float y, float scale, int color) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);
        graphics.text(font, text, 0, 0, color, false);
        pose.popMatrix();
    }

    /** Text without the drop shadow, which smears on the white canvas. */
    static void centered(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        graphics.text(font, text, x - font.width(text) / 2, y, color, false);
    }

    /** The canvas itself: it washes in over the world on opening, then motes drift across it. */
    static final class Backdrop {
        /** Stepping from one full-screen page to another must not flash the world through. */
        private static long lastDrawn;
        private final float[][] motes = new float[46][4];
        private final long opened = Util.getMillis() - lastDrawn < 250 ? 0 : Util.getMillis();

        Backdrop() {
            java.util.Random random = new java.util.Random(1307);
            for (float[] mote : motes) {
                mote[0] = random.nextFloat();
                mote[1] = random.nextFloat();
                mote[2] = 0.35F + random.nextFloat() * 0.9F;
                mote[3] = random.nextFloat() * Mth.TWO_PI;
            }
        }
        void draw(GuiGraphicsExtractor graphics, Theme theme, int wide, int tall, float lightX, float lightY, float mouseX, float mouseY) {
            long now = Util.getMillis();
            lastDrawn = now;
            float wash = smooth((now - opened) / 260F);
            graphics.fillGradient(0, 0, wide, tall, alpha(theme.top(), wash), alpha(theme.bottom(), wash));
            glow(graphics, lightX, lightY, wide * 0.62F, alpha(theme.bloom(), wash));
            float time = now / 1000F;
            float shiftX = (mouseX - wide / 2F) / wide, shiftY = (mouseY - tall / 2F) / tall;
            for (float[] mote : motes) {
                float depth = mote[2];
                float x = (mote[0] * wide + Mth.sin(time * 0.11F * depth + mote[3]) * 14 * depth - shiftX * 16 * depth + wide) % wide;
                float y = ((mote[1] * tall - time * 2.2F * depth - shiftY * 12 * depth) % tall + tall) % tall;
                float twinkle = 0.45F + 0.55F * Mth.sin(time * 0.9F * depth + mote[3]) * Mth.sin(time * 0.37F + mote[3] * 2);
                disc(graphics, x, y, 0.45F + depth * 0.55F, alpha(theme.mote(), wash * Math.max(0.15F, twinkle)));
            }
        }
    }

    /** A crescent that swaps the canvas between white moonlight and dark nightfall, for every moonlit screen. */
    static final class ThemeToggle extends AbstractButton {
        private final Runnable after;

        ThemeToggle(int x, int y, Runnable after) {
            super(x, y, 14, 14, Component.translatable("screen.howlingwerewolf.theme"));
            this.after = after;
        }
        @Override public void onPress(InputWithModifiers input) {
            HWClientConfig.setNightfall(!HWClientConfig.nightfall());
            after.run();
        }
        @Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            float x = getX() + getWidth() / 2F, y = getY() + getHeight() / 2F;
            disc(graphics, x, y, 5.5F, isHovered() ? 0xFFFFFFFF : BAND_SUB);
            disc(graphics, x + 2.6F, y - 1.6F, 4.6F, BAND);
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }

    /** Flat pill for the letterbox: solid blood for the page's main action, an outline for the rest. */
    static final class Pill extends AbstractButton {
        private final boolean solid;
        private final Runnable action;
        private Supplier<Component> hint = () -> null;
        private boolean selected;
        private float glow;
        private long lastFrame = Util.getMillis();

        Pill(int x, int y, int width, int height, Component message, boolean solid, Runnable action) {
            super(x, y, width, height, message);
            this.solid = solid;
            this.action = action;
        }
        Pill hint(Supplier<Component> text) { hint = text; return this; }
        /** Marks the current choice of a group; it stays lit even while it cannot be pressed again. */
        void selected(boolean value) { selected = value; }
        Component hintText() { return hint.get(); }

        @Override public void onPress(InputWithModifiers input) { if (active) action.run(); }

        @Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            long now = Util.getMillis();
            float step = Math.min(now - lastFrame, 80) / 90F;
            lastFrame = now;
            glow = Mth.clamp(glow + (active && isHovered() ? step : -step), 0, 1);
            Font font = Minecraft.getInstance().font;
            int x = getX(), y = getY(), width = getWidth(), height = getHeight();
            int ink;
            if (solid || selected) {
                round(graphics, x, y, width, height, height / 2, active || selected ? mix(BLOOD, 0xFFE9506A, glow) : 0xFF2B2930);
                ink = active || selected ? 0xFFFFFFFF : BAND_FAINT;
            } else {
                round(graphics, x, y, width, height, height / 2, active ? mix(0xFF4B4852, BAND_TEXT, glow) : 0xFF2B2930);
                round(graphics, x + 1, y + 1, width - 2, height - 2, height / 2 - 1, active ? mix(BAND, 0xFF2B2930, glow) : BAND);
                ink = active ? BAND_TEXT : BAND_FAINT;
            }
            if (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) graphics.outline(x - 1, y - 1, width + 2, height + 2, BAND_TEXT);
            String text = CompanionUi.fit(font, getMessage().getString(), width - 6);
            graphics.text(font, text, x + (width - font.width(text)) / 2, y + (height - 8) / 2, ink, false);
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }

    private MoonUi() {}
}
