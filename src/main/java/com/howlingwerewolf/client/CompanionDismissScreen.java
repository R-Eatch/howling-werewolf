/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.UUID;

/** Dismissal cannot be undone, so it asks once more on a page of its own, as the progression reset does. */
final class CompanionDismissScreen extends Screen {
    private static final String KEY = "screen.howlingwerewolf.companion.dismiss.";
    private static final int TOP_BAND = 28, BOTTOM_BAND = 40, TEXT_WIDTH = 300;
    private final Screen parent;
    private final UUID companion;
    private final String name;
    private final int tier;
    private final CompanionMode mode;
    private final MoonUi.Backdrop backdrop = new MoonUi.Backdrop();

    CompanionDismissScreen(Screen parent, UUID companion, String name, int tier, CompanionMode mode) {
        super(Component.translatable(KEY + "title"));
        this.parent = parent;
        this.companion = companion;
        this.name = name;
        this.tier = tier;
        this.mode = mode;
    }

    @Override
    protected void init() {
        int y = height - BOTTOM_BAND + 10;
        addRenderableWidget(new MoonUi.Pill(width / 2 - 134, y, 130, 20,
                Component.translatable(KEY + "confirm"), true, this::confirm));
        addRenderableWidget(new MoonUi.Pill(width / 2 + 4, y, 130, 20,
                Component.translatable(KEY + "cancel"), false, this::onClose));
    }

    private void confirm() {
        if (minecraft.getConnection() != null) {
            ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.DISMISS, companion));
        }
        minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        MoonUi.Theme theme = MoonUi.theme();
        int center = width / 2, middle = (TOP_BAND + height - BOTTOM_BAND) / 2;
        backdrop.draw(graphics, theme, width, height, center, middle, mouseX, mouseY);
        MoonUi.moon(graphics, center, middle, Math.min(92, (height - TOP_BAND - BOTTOM_BAND) / 2 - 4), MoonUi.moonPhase(), theme, 0.35F);
        graphics.fill(0, 0, width, TOP_BAND, MoonUi.BAND);
        graphics.fill(0, height - BOTTOM_BAND, width, height, MoonUi.BAND);
        MoonUi.centered(graphics, font, title, center, 10, MoonUi.BAND_TEXT);
        int y = middle - 46;
        MoonUi.centered(graphics, font, Component.translatable(KEY + "warning"), center, y, theme.accent());
        MoonUi.centered(graphics, font, Component.translatable(KEY + "who", name, tier), center, y + 20, theme.ink());
        y = paragraph(graphics, Component.translatable(KEY + "clears"), center, y + 38, theme.sub());
        y = paragraph(graphics, Component.translatable(KEY + "items"), center, y + 2, theme.sub());
        if (mode == CompanionMode.HUNT) paragraph(graphics, Component.translatable(KEY + "hunt"), center, y + 8, theme.accent());
        else if (mode == CompanionMode.DEAD) paragraph(graphics, Component.translatable(KEY + "dead"), center, y + 8, theme.accent());
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    /** Centered lines that wrap on narrow windows; returns where the next line starts. */
    private int paragraph(GuiGraphicsExtractor graphics, Component text, int center, int y, int color) {
        for (FormattedCharSequence line : font.split(text, Math.min(TEXT_WIDTH, width - 32))) {
            graphics.text(font, line, center - font.width(line) / 2, y, color, false);
            y += 11;
        }
        return y;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // The canvas is drawn with the dialog itself.
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
