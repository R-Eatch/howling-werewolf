/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The moonlit screens handle input through these small types, which wrap the 1.21.1 screen and
 * widget callbacks. Each version keeps its own copy, so the screens themselves stay unchanged.
 */
final class MoonInput {
    /** A mouse button press at a position. */
    record MouseButtonEvent(double x, double y, int buttonInfo) {
        int button() { return buttonInfo; }
    }

    /** A key press. */
    record KeyEvent(int key, int scancode, int modifiers) {
        boolean isUp() { return key == GLFW.GLFW_KEY_UP; }
        boolean isDown() { return key == GLFW.GLFW_KEY_DOWN; }
        boolean matches(KeyMapping mapping) { return mapping.matches(key, scancode); }
    }

    /** A button drawn through {@link MoonGraphics}. */
    abstract static class Button extends AbstractButton {
        Button(int x, int y, int width, int height, Component message) { super(x, y, width, height, message); }
        @Override public final void onPress() { press(); }
        abstract void press();
        @Override protected final void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            extractContents(new MoonGraphics(graphics), mouseX, mouseY, partialTick);
        }
        protected abstract void extractContents(MoonGraphics graphics, int mouseX, int mouseY, float partialTick);
    }

    /** A screen drawn through {@link MoonGraphics}, receiving the wrapped input types. */
    abstract static class Page extends Screen {
        Page(Component title) { super(title); }
        @Override public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            extractRenderState(new MoonGraphics(graphics), mouseX, mouseY, partialTick);
        }
        /** Draws the page; the default draws the widgets. */
        public void extractRenderState(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.render(graphics.raw(), mouseX, mouseY, partialTick);
        }
        /** Each page paints its own canvas. */
        public void extractBackground(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {}
        /** Screen#render calls this first; the page's canvas replaces 1.21.1's world blur and dimming. */
        @Override public final void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            extractBackground(new MoonGraphics(graphics), mouseX, mouseY, partialTick);
        }

        @Override public final boolean mouseClicked(double x, double y, int button) { return mouseClicked(new MouseButtonEvent(x, y, button), false); }
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return super.mouseClicked(event.x(), event.y(), event.button()); }
        @Override public final boolean mouseReleased(double x, double y, int button) { return mouseReleased(new MouseButtonEvent(x, y, button)); }
        public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(event.x(), event.y(), event.button()); }
        @Override public final boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
            return mouseDragged(new MouseButtonEvent(x, y, button), dragX, dragY);
        }
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            return super.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
        }
        @Override public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) { return super.mouseScrolled(x, y, scrollX, scrollY); }
        @Override public final boolean keyPressed(int key, int scancode, int modifiers) { return keyPressed(new KeyEvent(key, scancode, modifiers)); }
        public boolean keyPressed(KeyEvent event) { return super.keyPressed(event.key(), event.scancode(), event.modifiers()); }
    }

    private MoonInput() {}
}
