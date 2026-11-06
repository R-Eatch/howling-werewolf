package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.network.CompanionActionPacket;
import com.howlingwerewolf.network.SetClawSlotPacket;
import com.howlingwerewolf.network.ToggleAbilityPacket;
import com.howlingwerewolf.network.ToggleBeastModePacket;
import com.howlingwerewolf.network.ToggleNightVisionPacket;
import com.howlingwerewolf.network.ToggleQuadrupedModePacket;
import com.howlingwerewolf.network.UnlockAbilityPacket;
import com.howlingwerewolf.network.UpgradeTreeSkillPacket;
import com.howlingwerewolf.network.UseAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import com.howlingwerewolf.client.MoonInput.KeyEvent;
import com.howlingwerewolf.client.MoonInput.MouseButtonEvent;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The world falls away to moonlight and the player's skills float around them. A letterbox holds
 * the navigation above and whatever is in focus below; between the bands stands tonight's moon,
 * ringed by experience, with the player's own form in front of it.
 */
public final class WerewolfProgressionScreen extends MoonInput.Page {
    private static final String KEY = "screen.howlingwerewolf.";
    private static final int MIN_WIDTH = 560, MIN_HEIGHT = 320, COMFORT_WIDTH = 640, COMFORT_HEIGHT = 360;
    private static final int TOP_BAND = 28, THIN_BAND = 16, MARGIN = 12, NODE_RADIUS = 12, HEADER = 30;
    private static final int XP_TICKS = 64;
    private static final int TREE_ACTIONS = 104, ABILITY_ACTIONS = 142, BULLET_INDENT = 9;

    private Page page = Page.TREE;
    private WerewolfTreeSkill selectedSkill = WerewolfTreeSkill.DAMAGE;
    private WerewolfAbility selectedAbility = WerewolfAbility.NIGHT_VISION;
    private final List<Node> nodes = new ArrayList<>();
    private final List<TabButton> tabs = new ArrayList<>();
    private final List<Line> article = new ArrayList<>();
    private final MoonUi.Backdrop backdrop = new MoonUi.Backdrop();

    private MoonUi.Pill upgrade, reset, unlock, summon, toggleForm, toggleVision, clawLeft, clawRight, bite, surge, beast, clawSwitch;
    private float uiScale = 1;
    private int wide, tall, band, stageBottom, moonY, moonRadius, orbitX, orbitY, rowGap;
    private int articleX, articleY, articleWidth, articleHeight, articleLength;
    private float frameBlend, moonX = -1, scroll, scrollGoal, underlineX = -1, underlineWidth, rotation = -24;
    private boolean dragging, draggingScroll;
    private long pageOpened = Util.getMillis(), lastFrame = pageOpened;
    private boolean builtForWerewolf;
    private @Nullable RemotePlayer figure;

    public WerewolfProgressionScreen() {
        super(Component.translatable("screen.howlingwerewolf.progression"));
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        clearWidgets();
        nodes.clear();
        tabs.clear();
        upgrade = reset = unlock = summon = toggleForm = toggleVision = clawLeft = clawRight = bite = surge = beast = clawSwitch = null;
        float fit = Math.min((float) width / MIN_WIDTH, (float) height / MIN_HEIGHT);
        uiScale = fit < 1 ? fit : Math.max(1, (int) Math.min((float) width / COMFORT_WIDTH, (float) height / COMFORT_HEIGHT));
        wide = Math.round(width / uiScale);
        tall = Math.round(height / uiScale);
        WerewolfData data = getData();
        builtForWerewolf = data != null && data.isWerewolf();
        layout(data);
        buildTabs();
        if (data != null && data.isWerewolf()) {
            if (page == Page.TREE) buildTree();
            else if (page == Page.ABILITIES) buildAbilities();
        }
        if (page == Page.INFO) buildInfo(data);
        if (page == Page.TRIAL) buildTrial(data);
        refresh();
    }

    private void layout(@Nullable WerewolfData data) {
        boolean stage = page == Page.TREE || page == Page.ABILITIES;
        if (stage && data != null && data.isWerewolf()) band = Math.max(60, 26 + longestDescription() * 10 + 6);
        else band = stage ? 30 : THIN_BAND;
        stageBottom = tall - band;
        int height = stageBottom - TOP_BAND;
        if (stage) {
            int room = height - HEADER - 12;
            moonRadius = Math.min(room / 2 - 14, Math.round(wide * 0.165F));
            moonY = TOP_BAND + HEADER + room / 2;
            int rows = page == Page.TREE ? WerewolfTreeSkill.values().length / 2 : WerewolfAbility.values().length / 2 + 1;
            rowGap = Math.min(56, (height - 8) / Math.max(rows, 1));
            orbitX = Math.max(moonRadius + 40, Math.round(wide * 0.25F));
            orbitY = rowGap * (rows - 1) / 2 + 34;
        } else {
            articleX = Math.round(wide * 0.42F);
            articleY = TOP_BAND + 10;
            articleWidth = wide - articleX - MARGIN - 8;
            articleHeight = stageBottom - articleY - 8;
            moonRadius = Math.min(height / 2 - 26, Math.round(wide * 0.17F));
            moonY = TOP_BAND + height / 2 - (page == Page.TRIAL ? 12 : 0);
        }
    }

    private int stageCenter() {
        return page == Page.TREE || page == Page.ABILITIES ? wide / 2 : Math.round(wide * 0.21F);
    }

    private int longestDescription() {
        int most = 0, room = wide - MARGIN * 2 - (page == Page.TREE ? TREE_ACTIONS : ABILITY_ACTIONS);
        if (page == Page.TREE) for (WerewolfTreeSkill skill : WerewolfTreeSkill.values())
            most = Math.max(most, font.split(Component.translatable(skill.translationKey() + ".desc"), room).size());
        else for (WerewolfAbility ability : WerewolfAbility.values())
            most = Math.max(most, font.split(Component.translatable(ability.translationKey() + ".desc"), room).size());
        return most;
    }

    private void buildTabs() {
        record Entry(Component label, Runnable action, @Nullable Page page) {}
        List<Entry> entries = List.of(
                new Entry(Component.translatable(KEY + "tab.tree"), () -> switchPage(Page.TREE), Page.TREE),
                new Entry(Component.translatable(KEY + "tab.abilities"), () -> switchPage(Page.ABILITIES), Page.ABILITIES),
                new Entry(Component.translatable(KEY + "tab.info"), () -> switchPage(Page.INFO), Page.INFO),
                new Entry(Component.translatable(KEY + "tab.trial"), () -> switchPage(Page.TRIAL), Page.TRIAL),
                new Entry(Component.translatable(KEY + "tab.skins"), () -> minecraft.setScreen(new WerewolfSkinScreen(this)), null),
                new Entry(Component.translatable(KEY + "companion.shortcut"), () -> PacketDistributor.sendToServer(
                        new CompanionActionPacket(CompanionActionPacket.Action.OPEN, CompanionActionPacket.NO_TARGET)), null));
        int total = 0;
        for (Entry entry : entries) total += font.width(entry.label()) + 14;
        // A long translation shortens the words rather than pushing the row off the screen.
        int room = wide - MARGIN * 2 - 36 - 60;
        float squeeze = Math.min(1, (float) room / total);
        int x = wide - MARGIN - 36 - Math.round(total * squeeze);
        for (Entry entry : entries) {
            int tabWidth = Math.round((font.width(entry.label()) + 14) * squeeze);
            tabs.add(addRenderableWidget(new TabButton(x, 4, tabWidth, 20, entry.label(), entry.page(), entry.action())));
            x += tabWidth;
        }
        addRenderableWidget(new MoonUi.ThemeToggle(wide - MARGIN - 32, 7, this::rebuildWidgets));
        addRenderableWidget(new TabButton(wide - MARGIN - 12, 4, 14, 20, Component.literal("×"), null, this::onClose));
    }

    private void buildTree() {
        WerewolfTreeSkill[] skills = WerewolfTreeSkill.values();
        for (int i = 0; i < skills.length; i++) nodes.add(addRenderableWidget(new Node(skills[i], null, i, skills.length, treeIcon(skills[i]))));
        int top = stageBottom + 8, right = wide - MARGIN;
        upgrade = addRenderableWidget(new MoonUi.Pill(right - 40, top, 40, 20, Component.literal("+"), true, () -> {
            PacketDistributor.sendToServer(new UpgradeTreeSkillPacket(selectedSkill));
            upgrade.active = false;
        }));
        reset = addRenderableWidget(new MoonUi.Pill(right - 72, top + 25, 72, 16, Component.translatable(KEY + "reset"), false,
                () -> minecraft.setScreen(new WerewolfResetConfirmScreen(this)))
                .hint(() -> Component.translatable(KEY + "reset.summary")));
    }

    private void buildAbilities() {
        WerewolfAbility[] abilities = WerewolfAbility.values();
        for (int i = 0; i < abilities.length; i++) nodes.add(addRenderableWidget(new Node(null, abilities[i], i, abilities.length, abilityIcon(abilities[i]))));
        int top = stageBottom + 8, right = wide - MARGIN, row = top + 25;
        unlock = addRenderableWidget(new MoonUi.Pill(right - 80, top, 80, 20, Component.translatable(KEY + "unlock"), true, () -> {
            PacketDistributor.sendToServer(new UnlockAbilityPacket(selectedAbility));
            unlock.active = false;
        }));
        summon = addRenderableWidget(new MoonUi.Pill(right - 80, row, 80, 16, Component.translatable(KEY + "use"), false,
                () -> PacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.SUMMON_WOLF_SPIRIT))));
        toggleForm = addRenderableWidget(new MoonUi.Pill(right - 30, row, 30, 16, Component.literal("G"), false,
                () -> PacketDistributor.sendToServer(new ToggleQuadrupedModePacket())));
        toggleVision = addRenderableWidget(new MoonUi.Pill(right - 30, row, 30, 16, Component.literal("V"), false,
                () -> PacketDistributor.sendToServer(new ToggleNightVisionPacket())));
        clawLeft = addRenderableWidget(new MoonUi.Pill(right - 62, row, 30, 16, Component.literal("<"), false, () -> changeClawSlot(-1)));
        clawRight = addRenderableWidget(new MoonUi.Pill(right - 30, row, 30, 16, Component.literal(">"), false, () -> changeClawSlot(1)));
        bite = addRenderableWidget(new MoonUi.Pill(right - 30, row, 30, 16, Component.literal("B"), false, ClientForgeEvents::useBloodyBite));
        surge = addRenderableWidget(new MoonUi.Pill(right - 30, row, 30, 16, Component.literal("R"), false,
                () -> PacketDistributor.sendToServer(new UseAbilityPacket(WerewolfAbility.MOONBLOOD_SURGE))));
        // Long, Tool and Fire Claws can be switched off once learned, and on again at no cost.
        clawSwitch = addRenderableWidget(new MoonUi.Pill(right - 80, row, 80, 16, Component.translatable(KEY + "ability_disable"), false,
                () -> PacketDistributor.sendToServer(new ToggleAbilityPacket(selectedAbility))));
    }

    private void buildInfo(@Nullable WerewolfData data) {
        String statusKey;
        int statusColor;
        MoonUi.Theme theme = MoonUi.theme();
        if (data != null && data.isBeastMode()) { statusKey = "beast"; statusColor = theme.accent(); }
        else if (data != null && data.isQuadrupedMode()) { statusKey = "quadruped"; statusColor = theme.accent(); }
        else if (data != null && data.isWerewolf()) { statusKey = "werewolf"; statusColor = theme.accent(); }
        else if (data != null && data.isInfected()) { statusKey = "infected"; statusColor = theme.accent(); }
        else { statusKey = "uninfected"; statusColor = theme.sub(); }
        article.clear();
        write(Component.translatable(KEY + "info.status", Component.translatable(KEY + "info.status." + statusKey)), statusColor, 4);
        write(Component.translatable(KEY + "kill_xp_rule"), theme.sub(), 10);
        write(Component.translatable(KEY + "info.features"), theme.ink(), 5);
        for (String key : List.of("full_moon", "armor", "fire", "night_vision", "diet", "silver", "progression", "level_scaling", "quadruped", "beast")) {
            writeBullet(Component.translatable(KEY + "info." + key), theme.sub(), 5);
        }
        writeBullet(Component.translatable(KEY + "info.beast_warning"), theme.accent(), 0);
        finishArticle();
    }

    private void buildTrial(@Nullable WerewolfData data) {
        MoonUi.Theme theme = MoonUi.theme();
        boolean won = data != null && data.hasDefeatedAlpha();
        article.clear();
        write(Component.translatable(KEY + "trial.title"), theme.ink(), 3);
        write(Component.translatable(won ? KEY + "trial.unlocked" : KEY + "trial.locked"), won ? theme.accent() : theme.faint(), 9);
        write(Component.translatable(KEY + "trial.intro"), theme.sub(), 7);
        write(Component.translatable(KEY + "trial.layout"), theme.sub(), 7);
        write(Component.translatable(KEY + "trial.items"), theme.sub(), 7);
        write(Component.translatable(KEY + "trial.requirements"), theme.sub(), 7);
        write(Component.translatable(KEY + "trial.warning"), theme.accent(), 0);
        finishArticle();
        beast = addRenderableWidget(new MoonUi.Pill(stageCenter() - 22, moonY + moonRadius + 14, 44, 18, Component.literal("H"), true,
                () -> PacketDistributor.sendToServer(new ToggleBeastModePacket())));
    }

    private void write(Component text, int color, int gapAfter) {
        List<FormattedCharSequence> lines = font.split(text, articleWidth);
        for (int i = 0; i < lines.size(); i++) article.add(new Line(lines.get(i), color, i == lines.size() - 1 ? gapAfter : 0, 0, false));
    }

    /** A list entry: the mark hangs in the margin so wrapped lines align under the text. */
    private void writeBullet(Component text, int color, int gapAfter) {
        List<FormattedCharSequence> lines = font.split(text, articleWidth - BULLET_INDENT);
        for (int i = 0; i < lines.size(); i++) article.add(new Line(lines.get(i), color, i == lines.size() - 1 ? gapAfter : 0, BULLET_INDENT, i == 0));
    }

    private void finishArticle() {
        int length = 0;
        for (Line line : article) length += 10 + line.gap();
        articleLength = length;
        scrollGoal = Mth.clamp(scrollGoal, 0, Math.max(0, articleLength - articleHeight));
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
        pageOpened = Util.getMillis();
        scroll = scrollGoal = 0;
        rebuildWidgets();
    }

    private void select(Node node) {
        if (node.skill != null) selectedSkill = node.skill;
        else selectedAbility = node.ability;
        refresh();
    }

    private @Nullable Node selectedNode() {
        for (Node node : nodes) if (node.selected()) return node;
        return null;
    }

    @Override
    public void tick() {
        WerewolfData data = getData();
        if ((data != null && data.isWerewolf()) != builtForWerewolf) { rebuildWidgets(); return; }
        refresh();
    }

    private void refresh() {
        WerewolfData data = getData();
        if (upgrade != null) upgrade.active = canUpgrade(data, selectedSkill);
        if (reset != null) reset.active = canReset(data);
        if (unlock != null) {
            boolean owned = data != null && data.hasAbility(selectedAbility);
            unlock.active = canUnlock(data, selectedAbility);
            unlock.setMessage(Component.translatable(owned ? KEY + "unlocked" : KEY + "unlock"));
            control(summon, data, WerewolfAbility.SUMMON_WOLF_SPIRIT);
            control(toggleForm, data, WerewolfAbility.QUADRUPED_FORM);
            control(toggleVision, data, WerewolfAbility.NIGHT_VISION);
            control(clawLeft, data, WerewolfAbility.EMPTY_CLAW_SLOT);
            control(clawRight, data, WerewolfAbility.EMPTY_CLAW_SLOT);
            control(bite, data, WerewolfAbility.BLOODY_BITE);
            control(surge, data, WerewolfAbility.MOONBLOOD_SURGE);
            clawSwitch.visible = selectedAbility.switchable();
            clawSwitch.active = data != null && data.hasAbility(selectedAbility);
            clawSwitch.setMessage(Component.translatable(data != null && data.isAbilityDisabled(selectedAbility)
                    ? KEY + "ability_enable" : KEY + "ability_disable"));
        }
        if (beast != null) beast.active = data != null && data.isTransformed() && data.hasDefeatedAlpha();
    }

    private void control(MoonUi.Pill pill, @Nullable WerewolfData data, WerewolfAbility owner) {
        pill.visible = selectedAbility == owner;
        pill.active = data != null && data.hasAbility(owner);
    }

    // ------------------------------------------------------------------ frame

    @Override
    public void extractRenderState(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float logicalX = mouseX / uiScale, logicalY = mouseY / uiScale;
        long now = Util.getMillis();
        frameBlend = 1 - (float) Math.exp(-Math.min(now - lastFrame, 100) / 90F);
        lastFrame = now;
        moonX = moonX < 0 ? stageCenter() : Mth.lerp(frameBlend, moonX, stageCenter());
        scroll = Mth.lerp(frameBlend, scroll, scrollGoal);
        MoonUi.Theme theme = MoonUi.theme();
        WerewolfData data = getData();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(uiScale, uiScale);
            backdrop.draw(graphics, theme, wide, tall, moonX, moonY, logicalX, logicalY);
            boolean stage = page == Page.TREE || page == Page.ABILITIES;
            if (page == Page.TRIAL) drawAltar(graphics, theme, data);
            else drawMoonAndFigure(graphics, theme, data, stage, logicalX, logicalY);
            if (stage) drawStage(graphics, theme, data);
            else drawArticle(graphics, theme);
            drawBands(graphics, data, logicalX, logicalY);
            super.extractRenderState(graphics, Math.round(logicalX), Math.round(logicalY), partialTick);
            for (var child : children()) {
                if (child instanceof MoonUi.ThemeToggle button && button.isHovered()) {
                    graphics.setTooltipForNextFrame(font, font.split(Component.translatable(KEY + "theme"), 160), mouseX, mouseY);
                }
            }
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @Override
    public void extractBackground(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The canvas below is the background; the world only shows while it washes in.
    }

    private void drawMoonAndFigure(MoonGraphics graphics, MoonUi.Theme theme, @Nullable WerewolfData data, boolean stage, float mouseX, float mouseY) {
        float shiftX = (mouseX - moonX) * -0.006F, shiftY = (mouseY - moonY) * -0.006F;
        MoonUi.moon(graphics, moonX + shiftX, moonY + shiftY, moonRadius, MoonUi.moonPhase(), theme);
        float ringRadius = moonRadius + 8;
        float progress = 0;
        if (data != null && data.isWerewolf()) {
            int needed = data.getExperienceForNextLevel();
            progress = needed == 0 ? 1 : Math.min(1, (float) data.getExperience() / needed);
        }
        int lit = Math.round(progress * XP_TICKS);
        for (int i = 0; i < XP_TICKS; i++) {
            float angle = i * Mth.TWO_PI / XP_TICKS - Mth.HALF_PI;
            float cos = Mth.cos(angle), sin = Mth.sin(angle);
            boolean on = i < lit;
            float inner = ringRadius, outer = ringRadius + (on ? 5 : 3);
            MoonUi.line(graphics, moonX + cos * inner, moonY + sin * inner, moonX + cos * outer, moonY + sin * outer, on ? 1.4F : 0.8F, on ? theme.accent() : theme.line());
        }
        float feet = moonY + moonRadius * 0.88F;
        MoonUi.pool(graphics, moonX, feet, moonRadius * 0.62F, moonRadius * 0.11F, theme.shadow());
        drawFigure(graphics, data, moonX, feet, moonRadius, mouseX, mouseY, stage);
    }

    private void drawFigure(MoonGraphics graphics, @Nullable WerewolfData data, float x, float feet, float radius, float mouseX, float mouseY, boolean stage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { figure = null; return; }
        if (figure == null || figure.level() != mc.level) figure = WerewolfSkinScreen.createPreviewPlayer(mc.level, mc.player.getGameProfile());
        RemotePlayer player = figure;
        boolean werewolf = data != null && data.isWerewolf();
        WerewolfForm form = !werewolf ? WerewolfForm.HUMAN : data.isTransformed() ? data.getForm() : WerewolfForm.WEREWOLF;
        WerewolfModelSet modelSet = data == null ? WerewolfModelSet.CLASSIC : data.getModelSet();
        String skin = data == null ? WerewolfSkinIds.DEFAULT_ID : data.getSkinId();
        float blocks = switch (form) { case BEAST -> 3.05F; case QUADRUPED -> 1.5F; case HUMAN -> 2.35F; default -> 2.5F; };
        float size = radius * 1.72F / blocks;
        int x0 = Math.round(x - radius - 60), x1 = Math.round(x + radius + 60), y0 = TOP_BAND, y1 = stageBottom;
        player.tickCount = mc.player.tickCount;
        float horizontal = (float) Math.atan((x - mouseX) / 90.0), vertical = (float) Math.atan((feet - size - mouseY) / 120.0);
        player.yBodyRot = player.yBodyRotO = 180 + rotation + horizontal * 12;
        player.setYRot(180 + rotation + horizontal * 30);
        player.setXRot(-vertical * 14);
        player.yHeadRot = player.yHeadRotO = player.getYRot();
        player.xRotO = player.getXRot();
        Quaternionf camera = new Quaternionf().rotateX(4 * Mth.DEG_TO_RAD);
        Quaternionf pose = new Quaternionf().rotateZ(Mth.PI).mul(camera);
        graphics.enableScissor(x0, y0, x1, y1);
        try {
            WerewolfSkinRenderContext.withPreview(player, form, modelSet, skin, () -> {
                graphics.entity(player, size, (x0 + x1) / 2F, feet, pose, camera);
            });
        } finally {
            graphics.disableScissor();
        }
    }

    /** Dashed arcs through the rows of nodes, and a hairline from each node to the moon. */
    private void drawStage(MoonGraphics graphics, MoonUi.Theme theme, @Nullable WerewolfData data) {
        if (data == null || !data.isWerewolf()) return;
        int center = wide / 2;
        MoonUi.centered(graphics, font, Component.translatable(KEY + "level", data.getLevel(), data.getEffectiveMaxLevel()), center, TOP_BAND + 6, theme.ink());
        int points = page == Page.TREE ? data.getAvailableTreePoints() : data.getAvailableSkillPoints();
        MoonUi.centered(graphics, font, Component.translatable(page == Page.TREE ? KEY + "tree_points" : KEY + "skill_points", points), center, TOP_BAND + 17,
                points > 0 ? theme.accent() : theme.faint());
        int needed = data.getExperienceForNextLevel();
        Component xp = needed == 0 ? Component.translatable(KEY + "max_level") : Component.translatable(KEY + "xp", data.getExperience(), needed);
        MoonUi.centered(graphics, font, xp, center, moonY + moonRadius + 15, theme.sub());
        float span = Math.min(0.98F, (rowGap * ((nodes.size() + 1) / 2 - 1) / 2F + 8) / orbitY);
        float limit = (float) Math.asin(span);
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 44; i += 2) {
                float a0 = -limit + limit * 2 * i / 44, a1 = -limit + limit * 2 * (i + 1) / 44;
                MoonUi.line(graphics, center + side * orbitX * Mth.cos(a0), moonY + orbitY * Mth.sin(a0),
                        center + side * orbitX * Mth.cos(a1), moonY + orbitY * Mth.sin(a1), 0.7F, theme.line());
            }
        }
        float ringEdge = moonRadius + 15;
        for (Node node : nodes) {
            float amount = node.reveal();
            if (amount <= 0.01F) continue;
            float x = node.x(amount), y = node.y(amount);
            float dx = center - x, dy = moonY - y, length = Mth.sqrt(dx * dx + dy * dy);
            if (length <= ringEdge + NODE_RADIUS) continue;
            float nx = dx / length, ny = dy / length;
            boolean lit = node.selected() || node.isHovered();
            MoonUi.line(graphics, x + nx * (NODE_RADIUS + 1), y + ny * (NODE_RADIUS + 1), center - nx * ringEdge, moonY - ny * ringEdge,
                    lit ? 1.1F : 0.6F, lit ? theme.accent() : MoonUi.alpha(theme.line(), 0.7F));
        }
    }

    private void drawArticle(MoonGraphics graphics, MoonUi.Theme theme) {
        float reveal = MoonUi.smooth((Util.getMillis() - pageOpened) / 220F);
        graphics.enableScissor(articleX, articleY, articleX + articleWidth + 8, articleY + articleHeight);
        int y = articleY - Math.round(scroll);
        for (Line line : article) {
            if (y + 10 > articleY && y < articleY + articleHeight) {
                // Lines dissolve at whichever edge has more text beyond it.
                float fade = 1;
                if (articleLength > articleHeight) {
                    if (scroll > 1) fade = Math.min(fade, (y - articleY + 2) / 12F);
                    if (scroll < articleLength - articleHeight - 1) fade = Math.min(fade, (articleY + articleHeight - y - 8) / 12F);
                }
                int ink = MoonUi.alpha(line.color(), reveal * Mth.clamp(fade, 0, 1));
                if (line.marked()) MoonUi.disc(graphics, articleX + 2.5F, y + 3.5F, 1.6F, ink);
                graphics.text(font, line.text(), articleX + line.indent(), y, ink, false);
            }
            y += 10 + line.gap();
        }
        graphics.disableScissor();
        int travel = articleLength - articleHeight;
        if (travel > 0) {
            int trackX = articleX + articleWidth + 6;
            graphics.fill(trackX, articleY, trackX + 1, articleY + articleHeight, MoonUi.alpha(theme.line(), 0.8F));
            int thumb = Math.max(18, articleHeight * articleHeight / articleLength);
            int thumbY = articleY + Math.round((articleHeight - thumb) * scroll / travel);
            MoonUi.round(graphics, trackX - 1, thumbY, 3, thumb, 1, theme.accent());
        }
    }

    /** The rite seen from above, laid out on the moon it is sworn under. */
    private void drawAltar(MoonGraphics graphics, MoonUi.Theme theme, @Nullable WerewolfData data) {
        MoonUi.moon(graphics, moonX, moonY, moonRadius, MoonUi.moonPhase(), theme);
        float ring = moonRadius + 6;
        for (int i = 0; i < XP_TICKS; i += 2) {
            float a0 = i * Mth.TWO_PI / XP_TICKS, a1 = (i + 1) * Mth.TWO_PI / XP_TICKS;
            MoonUi.line(graphics, moonX + Mth.cos(a0) * ring, moonY + Mth.sin(a0) * ring, moonX + Mth.cos(a1) * ring, moonY + Mth.sin(a1) * ring, 0.7F, theme.line());
        }
        float reach = moonRadius * 0.66F;
        int ink = 0xFF17151C;
        for (int side = 0; side < 4; side++) {
            float dirX = side == 1 ? 1 : side == 3 ? -1 : 0, dirY = side == 0 ? -1 : side == 2 ? 1 : 0;
            for (float step : new float[]{0.42F, 0.68F}) {
                float px = moonX + dirX * reach * step, py = moonY + dirY * reach * step;
                graphics.fill(Math.round(px) - 3, Math.round(py) - 3, Math.round(px) + 3, Math.round(py) + 3, 0xFF6F6A74);
            }
            float px = moonX + dirX * reach, py = moonY + dirY * reach;
            altarNode(graphics, px, py, Math.max(8, moonRadius * 0.14F), String.valueOf(side + 2), ink, MoonUi.BLOOD);
        }
        altarNode(graphics, moonX, moonY, Math.max(9, moonRadius * 0.17F), "1", MoonUi.BLOOD, 0xFFFFFFFF);
    }

    private void altarNode(MoonGraphics graphics, float x, float y, float radius, String number, int fill, int text) {
        MoonUi.disc(graphics, x, y, radius, fill);
        MoonUi.centered(graphics, font, Component.literal(number), Math.round(x), Math.round(y) - 4, text);
    }

    private void drawBands(MoonGraphics graphics, @Nullable WerewolfData data, float mouseX, float mouseY) {
        graphics.fill(0, 0, wide, TOP_BAND, MoonUi.BAND);
        graphics.fill(0, stageBottom, wide, tall, MoonUi.BAND);
        int titleRoom = tabs.isEmpty() ? wide : tabs.get(0).getX() - MARGIN - 8;
        if (font.width(title) * 2 <= titleRoom) MoonUi.large(graphics, font, title, MARGIN, 6, 2, MoonUi.BAND_TEXT);
        else graphics.text(font, CompanionUi.fit(font, title.getString(), titleRoom), MARGIN, 10, MoonUi.BAND_TEXT, false);
        TabButton current = null;
        for (TabButton tab : tabs) if (tab.page == page) current = tab;
        if (current != null) {
            float goalX = current.getX() + 7, goalWidth = current.getWidth() - 14;
            underlineX = underlineX < 0 ? goalX : Mth.lerp(frameBlend, underlineX, goalX);
            underlineWidth = underlineWidth <= 0 ? goalWidth : Mth.lerp(frameBlend, underlineWidth, goalWidth);
            graphics.fill(Math.round(underlineX), 23, Math.round(underlineX + underlineWidth), 25, MoonUi.BLOOD);
        }
        if (page != Page.TREE && page != Page.ABILITIES) return;
        if (data == null || !data.isWerewolf()) {
            MoonUi.centered(graphics, font, Component.translatable(KEY + "not_werewolf"), wide / 2, stageBottom + 11, MoonUi.BAND_TEXT);
            return;
        }
        drawFocus(graphics, data, mouseX, mouseY);
    }

    /** The lower band reads out whatever the pointer rests on, falling back to the selected node. */
    private void drawFocus(MoonGraphics graphics, WerewolfData data, float mouseX, float mouseY) {
        int top = stageBottom + 8, room = wide - MARGIN * 2 - (page == Page.TREE ? TREE_ACTIONS : ABILITY_ACTIONS);
        Node hovered = null;
        for (Node node : nodes) if (node.isMouseOver(mouseX, mouseY)) hovered = node;
        Node shown = hovered != null ? hovered : selectedNode();
        boolean previewing = hovered != null && !hovered.selected();
        setActionsVisible(!previewing);
        Component heading = null, body = null;
        if (reset != null && reset.visible && reset.isMouseOver(mouseX, mouseY)) {
            heading = reset.getMessage(); body = reset.hintText();
        } else if (Mth.abs(mouseX - wide / 2F) < 70 && mouseY > moonY + moonRadius + 12 && mouseY < moonY + moonRadius + 26) {
            int needed = data.getExperienceForNextLevel();
            heading = needed == 0 ? Component.translatable(KEY + "max_level") : Component.translatable(KEY + "xp", data.getExperience(), needed);
            body = Component.translatable(KEY + "kill_xp_rule");
        } else if (shown != null) {
            heading = shown.name();
            body = shown.description();
        }
        if (heading == null) return;
        graphics.text(font, heading, MARGIN, top, MoonUi.BAND_TEXT, false);
        int x = MARGIN + font.width(heading) + 8;
        if (shown != null && heading == shown.name) {
            if (shown.skill != null) {
                int rank = data.getTreeSkillRank(shown.skill);
                for (int pip = 0; pip < shown.skill.maxRank(); pip++) {
                    graphics.fill(x, top + 2, x + 8, top + 6, pip < rank ? MoonUi.BLOOD : 0xFF3A3840);
                    x += 11;
                }
                String count = rank + " / " + shown.skill.maxRank();
                graphics.text(font, count, x + 2, top, MoonUi.BAND_SUB, false);
            } else {
                Component cost = Component.translatable(KEY + "cost", shown.ability.cost());
                graphics.text(font, cost, x, top, 0xFFE3B667, false);
                x += font.width(cost) + 8;
                Component state = null;
                if (data.hasAbility(shown.ability)) {
                    if (shown.ability == WerewolfAbility.QUADRUPED_FORM) state = Component.translatable(data.isQuadrupedMode() ? KEY + "active" : KEY + "inactive");
                    else if (shown.ability == WerewolfAbility.EMPTY_CLAW_SLOT) state = Component.translatable(KEY + "claw_slot", data.getClawHotbarSlot() + 1);
                    else if (shown.ability.switchable()) state = Component.translatable(data.isAbilityDisabled(shown.ability) ? KEY + "inactive" : KEY + "active");
                }
                if (state != null) graphics.text(font, state, x, top, 0xFFEE7A8C, false);
            }
        }
        int y = top + 13;
        for (FormattedCharSequence line : font.split(body, room)) {
            graphics.text(font, line, MARGIN, y, MoonUi.BAND_SUB, false);
            y += 10;
        }
    }

    private void setActionsVisible(boolean visible) {
        if (upgrade != null) upgrade.visible = visible;
        if (unlock != null) {
            unlock.visible = visible;
            refresh();
            if (!visible) for (MoonUi.Pill pill : List.of(summon, toggleForm, toggleVision, clawLeft, clawRight, bite, surge, clawSwitch)) pill.visible = false;
        }
    }

    // ------------------------------------------------------------------ rules

    private static boolean canUpgrade(@Nullable WerewolfData data, WerewolfTreeSkill skill) {
        return data != null && data.isWerewolf() && data.getAvailableTreePoints() > 0 && data.getTreeSkillRank(skill) < skill.maxRank();
    }

    private static boolean canUnlock(@Nullable WerewolfData data, WerewolfAbility ability) {
        return data != null && data.isWerewolf() && !data.hasAbility(ability) && data.getAvailableSkillPoints() >= ability.cost();
    }

    private static boolean canReset(@Nullable WerewolfData data) {
        return data != null && data.canResetProgression();
    }

    private static @Nullable WerewolfData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : WerewolfApi.get(minecraft.player).orElse(null);
    }

    private static ItemLike treeIcon(WerewolfTreeSkill skill) {
        return switch (skill) {
            case DAMAGE -> Items.IRON_SWORD;
            case MOON_REND -> ModItems.MOONBANE_PEARL.get();
            case DEFENSE -> Items.SHIELD;
            case SPEED -> Items.SUGAR;
            case RESISTANCE -> Items.MILK_BUCKET;
            case REGENERATION -> Items.GHAST_TEAR;
            case JUMP -> Items.RABBIT_FOOT;
            case KNOCKBACK_RESISTANCE -> Items.ANVIL;
            case FALL_RESISTANCE -> Items.FEATHER;
            case LIFESTEAL -> Items.REDSTONE;
            case SATIETY -> Items.COOKED_BEEF;
            case CLAW_EFFICIENCY -> Items.IRON_PICKAXE;
            case HUNTING_MASTERY -> Items.BOW;
            case PACK_OATH -> Items.BONE;
        };
    }

    private static ItemLike abilityIcon(WerewolfAbility ability) {
        return switch (ability) {
            case NIGHT_VISION -> Items.ENDER_EYE;
            case SUMMON_WOLF_SPIRIT -> Items.SOUL_LANTERN;
            case QUADRUPED_FORM -> Items.LEATHER_BOOTS;
            case MOONBLOOD_SURGE -> Items.NETHER_WART;
            case ARMORED_INSTINCT -> Items.IRON_CHESTPLATE;
            case HARD_LIFE -> Items.BREAD;
            case EMPTY_CLAW_SLOT -> Items.ITEM_FRAME;
            case LONG_CLAWS -> Items.SHEARS;
            case TOOL_CLAWS -> Items.IRON_AXE;
            case FIRE_CLAWS -> Items.BLAZE_POWDER;
            case BLOODY_BITE -> Items.BEEF;
        };
    }

    // ------------------------------------------------------------------ input

    @Override public void mouseMoved(double mouseX, double mouseY) { super.mouseMoved(mouseX / uiScale, mouseY / uiScale); }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        MouseButtonEvent logical = logicalEvent(event);
        if (super.mouseClicked(logical, doubleClick)) return true;
        if (event.button() != 0) return false;
        float dx = (float) logical.x() - moonX, dy = (float) logical.y() - moonY;
        if (page != Page.TRIAL && dx * dx + dy * dy <= (moonRadius + 8) * (moonRadius + 8)) { dragging = true; return true; }
        if (articleLength > articleHeight && page != Page.TREE && page != Page.ABILITIES
                && Math.abs(logical.x() - (articleX + articleWidth + 6)) <= 4 && logical.y() >= articleY && logical.y() <= articleY + articleHeight) {
            draggingScroll = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = draggingScroll = false;
        return super.mouseReleased(logicalEvent(event));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) { rotation = Mth.wrapDegrees(rotation + (float) dragX / uiScale * 1.5F); return true; }
        if (draggingScroll) {
            float travel = articleLength - articleHeight;
            scrollGoal = Mth.clamp(scrollGoal + (float) (dragY / uiScale) * articleLength / Math.max(1, articleHeight), 0, travel);
            return true;
        }
        return super.mouseDragged(logicalEvent(event), dragX / uiScale, dragY / uiScale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (page == Page.INFO || page == Page.TRIAL) {
            scrollGoal = Mth.clamp(scrollGoal - (float) scrollY * 24, 0, Math.max(0, articleLength - articleHeight));
            return true;
        }
        return super.mouseScrolled(mouseX / uiScale, mouseY / uiScale, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.matches(ClientModEvents.OPEN_PROGRESSION)) { onClose(); return true; }
        return super.keyPressed(event);
    }

    private MouseButtonEvent logicalEvent(MouseButtonEvent event) {
        return new MouseButtonEvent(event.x() / uiScale, event.y() / uiScale, event.buttonInfo());
    }

    @Override public boolean isPauseScreen() { return false; }

    // ------------------------------------------------------------------ parts

    private record Line(FormattedCharSequence text, int color, int gap, int indent, boolean marked) {}
    private enum Page { TREE, ABILITIES, INFO, TRIAL }

    /** One word in the upper band; the screen draws the sliding underline. */
    private static final class TabButton extends MoonInput.Button {
        final @Nullable Page page;
        private final Runnable action;
        private float glow;
        private long lastFrame = Util.getMillis();

        TabButton(int x, int y, int width, int height, Component label, @Nullable Page page, Runnable action) {
            super(x, y, width, height, label);
            this.page = page;
            this.action = action;
        }
        @Override void press() { action.run(); }
        @Override protected void extractContents(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
            long now = Util.getMillis();
            glow = Mth.clamp(glow + (isHovered() ? 1 : -1) * Math.min(now - lastFrame, 80) / 90F, 0, 1);
            lastFrame = now;
            Font font = Minecraft.getInstance().font;
            int ink = MoonUi.mix(MoonUi.BAND_SUB, MoonUi.BAND_TEXT, glow);
            graphics.text(font, CompanionUi.fit(font, getMessage().getString(), Math.max(8, getWidth() - 10)), getX() + (getWidth() < 20 ? 4 : 7), getY() + 6, ink, false);
            if (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) graphics.outline(getX(), getY(), getWidth(), getHeight(), MoonUi.BAND_TEXT);
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }

    /** A skill or ability floating at its place on the orbit, with its name running outward. */
    private final class Node extends MoonInput.Button {
        final @Nullable WerewolfTreeSkill skill;
        final @Nullable WerewolfAbility ability;
        final Component name;
        private final ItemStack icon;
        private final int order;
        private final boolean left;
        private final float targetX, targetY;
        private final int labelWidth;
        private float glow;
        private long lastFrame = Util.getMillis();

        Node(@Nullable WerewolfTreeSkill skill, @Nullable WerewolfAbility ability, int index, int count, ItemLike item) {
            super(0, 0, 0, 0, Component.translatable(skill != null ? skill.translationKey() : ability.translationKey()));
            this.skill = skill;
            this.ability = ability;
            this.name = getMessage();
            this.icon = new ItemStack(item);
            this.order = index;
            int rows = (count + 1) / 2;
            this.left = index < rows;
            int row = left ? index : index - rows;
            int inColumn = left ? rows : count - rows;
            float dy = (row - (inColumn - 1) / 2F) * rowGap;
            float ratio = dy / orbitY;
            float reach = orbitX * Mth.sqrt(Math.max(0, 1 - ratio * ratio));
            this.targetX = wide / 2F + (left ? -reach : reach);
            this.targetY = moonY + dy;
            int room = Math.round(left ? targetX - NODE_RADIUS - 6 - 6 : wide - (targetX + NODE_RADIUS + 6) - 6);
            this.labelWidth = Math.min(room, font.width(name));
            int nodeLeft = Math.round(targetX) - NODE_RADIUS - 1, nodeRight = Math.round(targetX) + NODE_RADIUS + 1;
            int boxLeft = left ? nodeLeft - 6 - labelWidth : nodeLeft;
            int boxRight = left ? nodeRight : nodeRight + 6 + labelWidth;
            setRectangle(boxRight - boxLeft, NODE_RADIUS * 2 + 2, boxLeft, Math.round(targetY) - NODE_RADIUS - 1);
        }
        boolean selected() { return skill != null ? selectedSkill == skill : selectedAbility == ability; }
        Component name() { return name; }
        Component description() { return Component.translatable((skill != null ? skill.translationKey() : ability.translationKey()) + ".desc"); }
        float reveal() { return (Util.getMillis() - pageOpened - order * 22F) / 300F; }
        float x(float amount) { return Mth.lerp(MoonUi.settle(amount), wide / 2F, targetX); }
        float y(float amount) { return Mth.lerp(MoonUi.settle(amount), (float) moonY, targetY); }

        @Override void press() { select(this); }

        @Override protected void extractContents(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float amount = reveal();
            if (amount <= 0) return;
            long now = Util.getMillis();
            glow = Mth.clamp(glow + (isHovered() ? 1 : -1) * Math.min(now - lastFrame, 80) / 90F, 0, 1);
            lastFrame = now;
            MoonUi.Theme theme = MoonUi.theme();
            WerewolfData data = getData();
            int rank = data == null ? 0 : skill != null ? data.getTreeSkillRank(skill) : data.hasAbility(ability) ? 1 : 0;
            int top = skill != null ? skill.maxRank() : 1;
            boolean ready = skill != null ? canUpgrade(data, skill) : canUnlock(data, ability);
            boolean owned = rank > 0, full = rank >= top;
            float time = now / 1000F;
            float x = x(amount), y = y(amount) + Mth.sin(time * 1.1F + order * 1.9F) * 1.1F;
            float settled = Mth.clamp(amount, 0, 1);
            float radius = NODE_RADIUS * (1 + 0.1F * glow + (selected() ? 0.06F : 0)) * Math.min(1, settled * 1.6F);
            MoonUi.pool(graphics, x, y + 3, radius * 1.5F, radius * 1.5F, MoonUi.alpha(theme.shadow(), settled));
            MoonUi.disc(graphics, x, y, radius, theme.node());
            if (ready) {
                float pulse = 0.5F + 0.5F * Mth.sin(time * 3.2F + order);
                MoonUi.ring(graphics, x, y, radius + 2 + pulse * 2, MoonUi.alpha(theme.accent(), 0.8F - pulse * 0.55F));
            }
            int edge = selected() ? theme.accent() : full ? theme.ink() : owned ? theme.sub() : theme.line();
            MoonUi.ring(graphics, x, y, radius, edge);
            if (selected()) MoonUi.ring(graphics, x, y, radius - 0.8F, edge);
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            float iconScale = radius / NODE_RADIUS;
            pose.scale(iconScale, iconScale);
            graphics.item(icon, -8, -8);
            pose.popMatrix();
            if (!owned && !ready) MoonUi.disc(graphics, x, y, radius - 1, MoonUi.alpha(theme.node(), 0.62F));
            float textAlpha = MoonUi.smooth(settled * 1.3F - 0.3F);
            if (textAlpha <= 0.02F) return;
            int nameColor = selected() ? theme.accent() : owned || ready ? theme.ink() : theme.sub();
            String label = CompanionUi.fit(font, name.getString(), labelWidth);
            int textWidth = font.width(label);
            int textX = left ? Math.round(targetX) - NODE_RADIUS - 6 - textWidth : Math.round(targetX) + NODE_RADIUS + 7;
            int textY = Math.round(targetY) - 8;
            graphics.text(font, label, textX, textY, MoonUi.alpha(nameColor, textAlpha), false);
            int pipWidth = top > 4 ? 5 : 7, gap = 2;
            int pipsWidth = skill != null ? top * pipWidth + (top - 1) * gap : 0;
            int pipX = left ? Math.round(targetX) - NODE_RADIUS - 6 - pipsWidth : textX;
            if (skill != null) {
                for (int pip = 0; pip < top; pip++) {
                    int color = pip < rank ? theme.accent() : theme.line();
                    graphics.fill(pipX, textY + 11, pipX + pipWidth, textY + 14, MoonUi.alpha(color, textAlpha));
                    pipX += pipWidth + gap;
                }
            } else {
                int color = owned ? data.isAbilityDisabled(ability) ? theme.sub() : theme.accent() : theme.line();
                int barX = left ? Math.round(targetX) - NODE_RADIUS - 6 - 12 : textX;
                graphics.fill(barX, textY + 11, barX + 12, textY + 14, MoonUi.alpha(color, textAlpha));
            }
            if (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) graphics.outline(getX(), getY(), getWidth(), getHeight(), theme.accent());
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }
}
