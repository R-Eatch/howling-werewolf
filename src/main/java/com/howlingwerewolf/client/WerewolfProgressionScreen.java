package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.network.SetClawSlotPacket;
import com.howlingwerewolf.network.ToggleBeastModePacket;
import com.howlingwerewolf.network.ToggleNightVisionPacket;
import com.howlingwerewolf.network.UnlockAbilityPacket;
import com.howlingwerewolf.network.UpgradeTreeSkillPacket;
import com.howlingwerewolf.network.UseAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class WerewolfProgressionScreen extends Screen {
    private static final int PANEL_WIDTH = 326;
    private static final int PANEL_HEIGHT = 450;
    private static final int TREE_ROW_HEIGHT = 18;
    private static final int ABILITY_ROW_HEIGHT = 29;
    private static final int BACKGROUND = 0xF0110D13;
    private static final int INNER = 0xFF211820;
    private static final int BORDER = 0xFF743547;
    private static final int ACCENT = 0xFFD94A5E;
    private static final int GOLD = 0xFFFFD75A;
    private static final int TEXT = 0xFFE8E2E4;
    private static final int DIM = 0xFF776B71;
    private Page page = Page.TREE;
    private final Map<WerewolfTreeSkill, Button> treeButtons = new EnumMap<>(WerewolfTreeSkill.class);
    private final Map<WerewolfAbility, Button> abilityButtons = new EnumMap<>(WerewolfAbility.class);
    private Button resetButton;
    private Button beastButton;
    private float uiScale = 0.82F;

    public WerewolfProgressionScreen() {
        super(Component.translatable("screen.howlingwerewolf.progression"));
    }

    @Override
    protected void init() {
        clearWidgets();
        treeButtons.clear();
        abilityButtons.clear();
        beastButton = null;
        uiScale = Math.min(0.82F, Math.min((width - 12.0F) / PANEL_WIDTH, (height - 12.0F) / PANEL_HEIGHT));
        uiScale = Math.max(0.1F, uiScale);
        int left = (logicalWidth() - PANEL_WIDTH) / 2;
        int top = (logicalHeight() - PANEL_HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.tab.tree"), button -> switchPage(Page.TREE))
                .bounds(left + 8, top + PANEL_HEIGHT - 29, 54, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.tab.abilities"), button -> switchPage(Page.ABILITIES))
                .bounds(left + 66, top + PANEL_HEIGHT - 29, 58, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.tab.info"), button -> switchPage(Page.INFO))
                .bounds(left + 128, top + PANEL_HEIGHT - 29, 52, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.tab.trial"), button -> switchPage(Page.TRIAL))
                .bounds(left + 184, top + PANEL_HEIGHT - 29, 66, 20).build());
        resetButton = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.reset"), button ->
                        minecraft.setScreen(new WerewolfResetConfirmScreen(this)))
                .bounds(left + 254, top + PANEL_HEIGHT - 29, 64, 20).build());
        resetButton.active = canReset(getData());
        if (page == Page.TREE) initTree(left, top);
        else if (page == Page.ABILITIES) initAbilities(left, top);
        else if (page == Page.TRIAL) initTrial(left, top);
    }

    private void initTree(int left, int top) {
        WerewolfData data = getData();
        int row = 0;
        for (WerewolfTreeSkill skill : WerewolfTreeSkill.values()) {
            Button button = Button.builder(Component.literal("+"), clicked -> {
                        PacketDistributor.sendToServer(new UpgradeTreeSkillPacket(skill));
                        clicked.active = false;
                    }).bounds(left + PANEL_WIDTH - 35, top + 78 + row * TREE_ROW_HEIGHT, 20, 15).build();
            button.active = canUpgrade(data, skill);
            treeButtons.put(skill, addRenderableWidget(button));
            row++;
        }
    }

    private void initAbilities(int left, int top) {
        WerewolfData data = getData();
        int row = 0;
        for (WerewolfAbility ability : WerewolfAbility.values()) {
            Button button = Button.builder(Component.translatable("screen.howlingwerewolf.unlock"), clicked -> {
                        PacketDistributor.sendToServer(new UnlockAbilityPacket(ability));
                        clicked.active = false;
                    }).bounds(left + PANEL_WIDTH - 84, abilityControlY(top, ability), 69, 18).build();
            button.active = canUnlock(data, ability);
            abilityButtons.put(ability, addRenderableWidget(button));
            row++;
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.use"), button ->
                        PacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.SUMMON_WOLF_SPIRIT)))
                .bounds(left + 160, abilityControlY(top, WerewolfAbility.SUMMON_WOLF_SPIRIT), 58, 18).build());
        addRenderableWidget(Button.builder(Component.literal("G"), button ->
                        PacketDistributor.sendToServer(new com.howlingwerewolf.network.ToggleQuadrupedModePacket()))
                .bounds(left + 194, abilityControlY(top, WerewolfAbility.QUADRUPED_FORM), 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal("V"), button ->
                        PacketDistributor.sendToServer(new ToggleNightVisionPacket()))
                .bounds(left + 194, abilityControlY(top, WerewolfAbility.NIGHT_VISION), 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeClawSlot(-1))
                .bounds(left + 160, abilityControlY(top, WerewolfAbility.EMPTY_CLAW_SLOT), 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeClawSlot(1))
                .bounds(left + 194, abilityControlY(top, WerewolfAbility.EMPTY_CLAW_SLOT), 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal("B"), button -> ClientForgeEvents.useBloodyBite())
                .bounds(left + 194, abilityControlY(top, WerewolfAbility.BLOODY_BITE), 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal("R"), button ->
                        PacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.MOONBLOOD_SURGE)))
                .bounds(left + 194, abilityControlY(top, WerewolfAbility.MOONBLOOD_SURGE), 24, 18).build());
    }

    private void initTrial(int left, int top) {
        beastButton = addRenderableWidget(Button.builder(Component.literal("H"), button ->
                        PacketDistributor.sendToServer(new ToggleBeastModePacket()))
                .bounds(left + PANEL_WIDTH - 54, top + 43, 36, 18).build());
        WerewolfData data = getData();
        beastButton.active = data != null && data.isTransformed() && data.hasDefeatedAlpha();
    }

    private void changeClawSlot(int delta) {
        WerewolfData data = getData();
        if (data == null || !data.hasAbility(WerewolfAbility.EMPTY_CLAW_SLOT)) return;
        int slot = Math.floorMod(data.getClawHotbarSlot() + delta, 9);
        data.setClawHotbarSlot(slot);
        PacketDistributor.sendToServer(new SetClawSlotPacket(slot));
    }

    private void switchPage(Page target) {
        if (page == target) return;
        page = target;
        rebuildWidgets();
    }

    @Override
    public void tick() {
        WerewolfData data = getData();
        treeButtons.forEach((skill, button) -> button.active = canUpgrade(data, skill));
        abilityButtons.forEach((ability, button) -> {
            button.active = canUnlock(data, ability);
            button.setMessage(data != null && data.hasAbility(ability)
                    ? Component.translatable("screen.howlingwerewolf.unlocked")
                    : Component.translatable("screen.howlingwerewolf.unlock"));
        });
        if (resetButton != null) resetButton.active = canReset(data);
        if (beastButton != null) {
            beastButton.active = data != null && data.isTransformed() && data.hasDefeatedAlpha();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        int logicalMouseX = toLogical(mouseX);
        int logicalMouseY = toLogical(mouseY);
        int left = (logicalWidth() - PANEL_WIDTH) / 2;
        int top = (logicalHeight() - PANEL_HEIGHT) / 2;
        graphics.pose().pushPose();
        graphics.pose().scale(uiScale, uiScale, 1.0F);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BACKGROUND);
        graphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, BORDER);
        graphics.fill(left + 7, top + 34, left + PANEL_WIDTH - 7, top + PANEL_HEIGHT - 36, INNER);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 9, ACCENT);
        WerewolfData data = getData();
        if (page == Page.INFO) {
            renderInfo(graphics, data, left, top);
        } else if (page == Page.TRIAL) {
            renderTrial(graphics, data, left, top);
        } else if (data == null || !data.isWerewolf()) {
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.not_werewolf"), left + PANEL_WIDTH / 2, top + 130, TEXT);
        } else {
            renderHeader(graphics, data, left, top);
            if (page == Page.TREE) renderTree(graphics, data, left, top, logicalMouseX, logicalMouseY);
            else if (page == Page.ABILITIES) renderAbilities(graphics, data, left, top, logicalMouseX, logicalMouseY);
        }
        super.render(graphics, logicalMouseX, logicalMouseY, partialTick);
        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen#render invokes this again from super.render; the explicit transparent pass above
        // preserves the 1.20.1 dimming without 1.21.1's world blur or a second overlay.
    }

    private void renderHeader(GuiGraphics graphics, WerewolfData data, int left, int top) {
        graphics.drawString(font, Component.translatable("screen.howlingwerewolf.level", data.getLevel(), data.getEffectiveMaxLevel()), left + 15, top + 42, TEXT, false);
        int points = page == Page.TREE ? data.getAvailableTreePoints() : data.getAvailableSkillPoints();
        String key = page == Page.TREE ? "screen.howlingwerewolf.tree_points" : "screen.howlingwerewolf.skill_points";
        graphics.drawString(font, Component.translatable(key, points), left + 190, top + 42, GOLD, false);
        int needed = data.getExperienceForNextLevel();
        float ratio = needed == 0 ? 1.0F : Math.min(1.0F, (float) data.getExperience() / needed);
        graphics.fill(left + 15, top + 52, left + PANEL_WIDTH - 15, top + 62, 0xFF3A3035);
        graphics.fill(left + 15, top + 52, left + 15 + Math.round((PANEL_WIDTH - 30) * ratio), top + 62, ACCENT);
        Component xpText = needed == 0
                ? Component.translatable("screen.howlingwerewolf.max_level")
                : Component.translatable("screen.howlingwerewolf.xp", data.getExperience(), needed);
        graphics.drawCenteredString(font, xpText, left + PANEL_WIDTH / 2, top + 53, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.kill_xp_rule"),
                left + PANEL_WIDTH / 2, top + 64, DIM);
    }

    private void renderTree(GuiGraphics graphics, WerewolfData data, int left, int top, int mouseX, int mouseY) {
        Component tooltip = null;
        int row = 0;
        for (WerewolfTreeSkill skill : WerewolfTreeSkill.values()) {
            int y = top + 82 + row * TREE_ROW_HEIGHT;
            if (mouseX >= left + 12 && mouseX < left + PANEL_WIDTH - 12 && mouseY >= y - 4 && mouseY < y + 13) {
                graphics.fill(left + 11, y - 4, left + PANEL_WIDTH - 11, y + 13, 0x553D2630);
                tooltip = Component.translatable(skill.translationKey() + ".desc");
            }
            graphics.drawString(font, Component.translatable(skill.translationKey()), left + 16, y, TEXT, false);
            int rank = data.getTreeSkillRank(skill);
            for (int pip = 0; pip < skill.maxRank(); pip++) {
                int x = left + 203 + pip * 13;
                graphics.fill(x, y, x + 9, y + 7, pip < rank ? ACCENT : DIM);
            }
            row++;
        }
        if (tooltip != null) renderWrappedTooltip(graphics, tooltip, mouseX, mouseY);
    }

    private void renderAbilities(GuiGraphics graphics, WerewolfData data, int left, int top, int mouseX, int mouseY) {
        int row = 0;
        for (WerewolfAbility ability : WerewolfAbility.values()) {
            int y = top + 82 + row * ABILITY_ROW_HEIGHT;
            boolean unlocked = data.hasAbility(ability);
            graphics.drawString(font, Component.translatable(ability.translationKey()), left + 16, y, unlocked ? TEXT : DIM, false);
            graphics.drawString(font, Component.translatable("screen.howlingwerewolf.cost", ability.cost()), left + 16, y + 11, GOLD, false);
            if (ability == WerewolfAbility.NIGHT_VISION && unlocked)
                graphics.drawString(font, Component.translatable(data.isNightVisionEnabled() ? "screen.howlingwerewolf.auto_on" : "screen.howlingwerewolf.auto_off"), left + 99, y + 11, ACCENT, false);
            if (ability == WerewolfAbility.QUADRUPED_FORM && unlocked)
                graphics.drawString(font, Component.translatable(data.isQuadrupedMode() ? "screen.howlingwerewolf.active" : "screen.howlingwerewolf.inactive"), left + 99, y + 11, ACCENT, false);
            if (ability == WerewolfAbility.EMPTY_CLAW_SLOT && unlocked)
                graphics.drawString(font, Component.translatable("screen.howlingwerewolf.claw_slot", data.getClawHotbarSlot() + 1), left + 99, y + 11, ACCENT, false);
            row++;
        }
        for (WerewolfAbility ability : WerewolfAbility.values()) {
            int y = top + 82 + ability.ordinal() * ABILITY_ROW_HEIGHT;
            if (mouseX >= left + 12 && mouseX < left + PANEL_WIDTH - 12 && mouseY >= y - 3 && mouseY < y + 27) {
                renderWrappedTooltip(graphics,
                        Component.translatable(ability.translationKey() + ".desc"), mouseX, mouseY);
                break;
            }
        }
    }

    private void renderInfo(GuiGraphics graphics, WerewolfData data, int left, int top) {
        String statusKey;
        int statusColor;
        if (data != null && data.isBeastMode()) {
            statusKey = "screen.howlingwerewolf.info.status.beast";
            statusColor = 0xFFFF5555;
        } else if (data != null && data.isQuadrupedMode()) {
            statusKey = "screen.howlingwerewolf.info.status.quadruped";
            statusColor = GOLD;
        } else if (data != null && data.isWerewolf()) {
            statusKey = "screen.howlingwerewolf.info.status.werewolf";
            statusColor = ACCENT;
        } else if (data != null && data.isInfected()) {
            statusKey = "screen.howlingwerewolf.info.status.infected";
            statusColor = GOLD;
        } else {
            statusKey = "screen.howlingwerewolf.info.status.uninfected";
            statusColor = DIM;
        }

        graphics.fill(left + 13, top + 43, left + PANEL_WIDTH - 13, top + 66, 0xFF30242B);
        graphics.drawString(font, Component.translatable("screen.howlingwerewolf.info.status",
                Component.translatable(statusKey)), left + 20, top + 50, statusColor, false);
        graphics.drawString(font, Component.translatable("screen.howlingwerewolf.info.features"),
                left + 16, top + 76, GOLD, false);

        List<InfoFeature> features = List.of(
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.full_moon"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.armor"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.fire"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.night_vision"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.diet"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.silver"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.progression"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.level_scaling"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.quadruped"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.beast"), TEXT),
                new InfoFeature(Component.translatable("screen.howlingwerewolf.info.beast_warning"), 0xFFFF5555)
        );
        int y = top + 91;
        for (InfoFeature feature : features) {
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(
                    Component.literal("• ").append(feature.text()), PANEL_WIDTH - 38);
            for (net.minecraft.util.FormattedCharSequence line : lines) {
                graphics.drawString(font, line, left + 19, y, feature.color(), false);
                y += 10;
            }
            y += 4;
        }
    }

    private void renderTrial(GuiGraphics graphics, WerewolfData data, int left, int top) {
        graphics.drawString(font, Component.translatable("screen.howlingwerewolf.trial.title"),
                left + 16, top + 45, GOLD, false);
        Component state = Component.translatable(data != null && data.hasDefeatedAlpha()
                ? "screen.howlingwerewolf.trial.unlocked"
                : "screen.howlingwerewolf.trial.locked");
        graphics.drawString(font, state, left + 16, top + 57,
                data != null && data.hasDefeatedAlpha() ? ACCENT : DIM, false);

        int introBottom = drawWrapped(graphics, Component.translatable("screen.howlingwerewolf.trial.intro"),
                left + 16, top + 76, PANEL_WIDTH - 32, TEXT);

        int centerX = left + PANEL_WIDTH / 2;
        int centerY = Math.max(top + 164, introBottom + 62);
        drawAltarNode(graphics, centerX, centerY, "1", GOLD);
        drawAltarNode(graphics, centerX, centerY - 48, "2", ACCENT);
        drawAltarNode(graphics, centerX + 48, centerY, "3", ACCENT);
        drawAltarNode(graphics, centerX, centerY + 48, "4", ACCENT);
        drawAltarNode(graphics, centerX - 48, centerY, "5", ACCENT);
        drawSpacingBlock(graphics, centerX, centerY - 20);
        drawSpacingBlock(graphics, centerX, centerY - 32);
        drawSpacingBlock(graphics, centerX + 20, centerY);
        drawSpacingBlock(graphics, centerX + 32, centerY);
        drawSpacingBlock(graphics, centerX, centerY + 20);
        drawSpacingBlock(graphics, centerX, centerY + 32);
        drawSpacingBlock(graphics, centerX - 20, centerY);
        drawSpacingBlock(graphics, centerX - 32, centerY);

        int y = centerY + 62;
        y = drawWrapped(graphics, Component.translatable("screen.howlingwerewolf.trial.layout"),
                left + 16, y, PANEL_WIDTH - 32, TEXT) + 5;
        y = drawWrapped(graphics, Component.translatable("screen.howlingwerewolf.trial.items"),
                left + 16, y, PANEL_WIDTH - 32, TEXT) + 5;
        y = drawWrapped(graphics, Component.translatable("screen.howlingwerewolf.trial.requirements"),
                left + 16, y, PANEL_WIDTH - 32, TEXT) + 5;
        drawWrapped(graphics, Component.translatable("screen.howlingwerewolf.trial.warning"),
                left + 16, y, PANEL_WIDTH - 32, 0xFFFF7777);
    }

    private void drawAltarNode(GuiGraphics graphics, int x, int y, String number, int color) {
        graphics.fill(x - 11, y - 11, x + 11, y + 11, 0xFF30242B);
        graphics.renderOutline(x - 11, y - 11, 22, 22, color);
        graphics.drawCenteredString(font, number, x, y - 4, color);
    }

    private void drawSpacingBlock(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 4, y - 4, x + 4, y + 4, 0xFF51494D);
        graphics.renderOutline(x - 4, y - 4, 8, 8, DIM);
    }

    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        for (net.minecraft.util.FormattedCharSequence line : font.split(text, width)) {
            graphics.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    private void renderWrappedTooltip(GuiGraphics graphics, Component text, int mouseX, int mouseY) {
        int maxWidth = Math.max(120, Math.min(320, logicalWidth() - 32));
        graphics.renderTooltip(font, font.split(text, maxWidth), mouseX, mouseY);
    }

    private static boolean canUpgrade(WerewolfData data, WerewolfTreeSkill skill) {
        return data != null && data.isWerewolf() && data.getAvailableTreePoints() > 0 && data.getTreeSkillRank(skill) < skill.maxRank();
    }

    private static boolean canUnlock(WerewolfData data, WerewolfAbility ability) {
        return data != null && data.isWerewolf() && !data.hasAbility(ability) && data.getAvailableSkillPoints() >= ability.cost();
    }

    private static boolean canReset(WerewolfData data) {
        return data != null && data.canResetProgression();
    }

    private static int abilityControlY(int top, WerewolfAbility ability) {
        return top + 81 + ability.ordinal() * ABILITY_ROW_HEIGHT;
    }

    private int logicalWidth() { return Math.round(width / uiScale); }
    private int logicalHeight() { return Math.round(height / uiScale); }
    private int toLogical(double coordinate) { return (int)Math.floor(coordinate / uiScale); }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX / uiScale, mouseY / uiScale);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX / uiScale, mouseY / uiScale, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX / uiScale, mouseY / uiScale, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX / uiScale, mouseY / uiScale, button, dragX / uiScale, dragY / uiScale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX / uiScale, mouseY / uiScale, scrollX, scrollY);
    }

    private static WerewolfData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : WerewolfApi.get(minecraft.player).orElse(null);
    }

    @Override public boolean isPauseScreen() { return false; }
    private record InfoFeature(Component text, int color) {}
    private enum Page { TREE, ABILITIES, INFO, TRIAL }
}
