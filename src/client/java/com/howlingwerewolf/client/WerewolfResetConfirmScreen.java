package com.howlingwerewolf.client;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.network.ResetProgressionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class WerewolfResetConfirmScreen extends Screen {
    private static final int TOP_BAND = 28, BOTTOM_BAND = 40;
    private final Screen parent;
    private final MoonUi.Backdrop backdrop = new MoonUi.Backdrop();

    public WerewolfResetConfirmScreen(Screen parent) {
        super(Component.translatable("screen.howlingwerewolf.reset.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = height - BOTTOM_BAND + 10;
        WerewolfData data = getData();
        MoonUi.Pill confirm = addRenderableWidget(new MoonUi.Pill(width / 2 - 134, y, 130, 20,
                Component.translatable("screen.howlingwerewolf.reset.confirm"), true, this::confirmReset));
        confirm.active = data != null && data.canResetProgression();
        addRenderableWidget(new MoonUi.Pill(width / 2 + 4, y, 130, 20,
                Component.translatable("screen.howlingwerewolf.reset.cancel"), false, this::onClose));
    }

    private void confirmReset() {
        ClientPlayNetworking.send(new ResetProgressionPacket());
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        MoonUi.Theme theme = MoonUi.theme();
        int center = width / 2, middle = (TOP_BAND + height - BOTTOM_BAND) / 2;
        backdrop.draw(graphics, theme, width, height, center, middle, mouseX, mouseY);
        MoonUi.moon(graphics, center, middle, Math.min(92, (height - TOP_BAND - BOTTOM_BAND) / 2 - 4), MoonUi.moonPhase(), theme, 0.35F);
        graphics.fill(0, 0, width, TOP_BAND, MoonUi.BAND);
        graphics.fill(0, height - BOTTOM_BAND, width, height, MoonUi.BAND);
        MoonUi.centered(graphics, font, title, center, 10, MoonUi.BAND_TEXT);
        WerewolfData data = getData();
        if (data == null || !data.canResetProgression()) {
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.reset.unavailable"), center, middle - 4, theme.accent());
        } else {
            int newLevel = data.getLevel() - 5, y = middle - 42;
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.reset.warning"), center, y, theme.accent());
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.reset.levels", data.getLevel(), newLevel), center, y + 20, theme.ink());
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.reset.clears"), center, y + 38, theme.sub());
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.reset.experience"), center, y + 50, theme.sub());
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.reset.points",
                    WerewolfData.skillPointsForLevel(newLevel, data.getEffectiveMaxLevel()),
                    WerewolfData.treePointsForLevel(newLevel, data.getEffectiveMaxLevel())), center, y + 70, theme.ink());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The canvas is drawn with the dialog itself.
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static WerewolfData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : WerewolfApi.get(minecraft.player).orElse(null);
    }

    @Override public boolean isPauseScreen() { return false; }
}
