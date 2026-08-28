package com.howlingwerewolf.client;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.network.ModNetwork;
import com.howlingwerewolf.network.ResetProgressionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WerewolfResetConfirmScreen extends Screen {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 184;
    private static final int BACKGROUND = 0xF0181015;
    private static final int BORDER = 0xFF9A344B;
    private static final int TEXT = 0xFFE8E2E4;
    private static final int WARNING = 0xFFFF6B6B;
    private static final int GOLD = 0xFFFFD75A;
    private final Screen parent;

    public WerewolfResetConfirmScreen(Screen parent) {
        super(Component.translatable("screen.howlingwerewolf.reset.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        WerewolfData data = getData();
        Button confirm = addRenderableWidget(Button.builder(
                        Component.translatable("screen.howlingwerewolf.reset.confirm"), button -> confirmReset())
                .bounds(left + 25, top + 145, 125, 20).build());
        confirm.active = data != null && data.canResetProgression();
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.reset.cancel"), button -> onClose())
                .bounds(left + 170, top + 145, 125, 20).build());
    }

    private void confirmReset() {
        ModNetwork.CHANNEL.sendToServer(new ResetProgressionPacket());
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BACKGROUND);
        graphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, BORDER);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 13, WARNING);
        WerewolfData data = getData();
        if (data == null || !data.canResetProgression()) {
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.reset.unavailable"),
                    left + PANEL_WIDTH / 2, top + 76, WARNING);
        } else {
            int newLevel = data.getLevel() - 5;
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.reset.warning"),
                    left + PANEL_WIDTH / 2, top + 38, WARNING);
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.reset.levels",
                    data.getLevel(), newLevel), left + PANEL_WIDTH / 2, top + 59, GOLD);
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.reset.clears"),
                    left + PANEL_WIDTH / 2, top + 78, TEXT);
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.reset.experience"),
                    left + PANEL_WIDTH / 2, top + 96, TEXT);
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.reset.points",
                    WerewolfData.skillPointsForLevel(newLevel, data.getEffectiveMaxLevel()),
                    WerewolfData.treePointsForLevel(newLevel, data.getEffectiveMaxLevel())),
                    left + PANEL_WIDTH / 2, top + 116, GOLD);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static WerewolfData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : WerewolfApi.get(minecraft.player).resolve().orElse(null);
    }

    @Override public boolean isPauseScreen() { return false; }
}
