package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.network.SetWerewolfSkinPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import com.howlingwerewolf.client.MoonInput.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.io.IOException;

/** A cosmetic fitting room. Only Apply sends a request; preview entities never enter the level. */
public final class WerewolfSkinScreen extends MoonInput.Page {
    private static final int MIN_WIDTH = 560, MIN_HEIGHT = 320, COMFORT_WIDTH = 640, COMFORT_HEIGHT = 360;
    private static final int TOP_BAND = 28, BOTTOM_BAND = 50, MARGIN = 12, LIST_WIDTH = 150, CARD_HEIGHT = 50, CARD_GAP = 4;
    private static final WerewolfForm[] PREVIEW_FORMS = {
            WerewolfForm.WEREWOLF, WerewolfForm.QUADRUPED, WerewolfForm.BEAST
    };
    private final Screen parent;
    private final Map<WerewolfForm, MoonUi.Pill> formButtons = new EnumMap<>(WerewolfForm.class);
    private final MoonUi.Backdrop backdrop = new MoonUi.Backdrop();
    private String selectedSkin = WerewolfSkinIds.DEFAULT_ID;
    private WerewolfModelSet modelSet = WerewolfModelSet.CLASSIC;
    private WerewolfModelSet pendingModelSet;
    private final Map<WerewolfModelSet, String> rememberedSkins = new EnumMap<>(WerewolfModelSet.class);
    private String pendingSkin;
    private int skinPage;
    private int catalogRevision = -1;
    private Component notice;
    private int noticeTicks;
    private WerewolfForm previewForm = WerewolfForm.WEREWOLF;
    private RemotePlayer previewPlayer;
    private MoonUi.Pill applyButton;
    private MoonUi.Pill equipmentButton;
    private MoonUi.Pill modelButton;
    private int wide, tall, stageX, stageWidth, moonX, moonY, moonRadius;
    private boolean initializedSelection;
    private boolean showEquipment = true;
    private boolean pendingEquipment;
    private boolean draggingPreview;
    private boolean confirmationTimedOut;
    private int pendingTicks;
    private float rotation;
    private float zoom = 1.0F;
    private float uiScale = 0.82F;

    public WerewolfSkinScreen(Screen parent) {
        super(Component.translatable("screen.howlingwerewolf.skins.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        formButtons.clear();
        draggingPreview = false;
        float fit = Math.min((float) width / MIN_WIDTH, (float) height / MIN_HEIGHT);
        uiScale = fit < 1 ? fit : Math.max(1, (int) Math.min((float) width / COMFORT_WIDTH, (float) height / COMFORT_HEIGHT));
        wide = Math.round(width / uiScale);
        tall = Math.round(height / uiScale);
        stageX = MARGIN + LIST_WIDTH + 8;
        stageWidth = wide - stageX - MARGIN;
        int stageTop = TOP_BAND + 6, stageBottom = tall - BOTTOM_BAND - 6;
        moonX = stageX + stageWidth / 2;
        moonRadius = Math.min((stageBottom - stageTop - 54) / 2, stageWidth / 2 - 8);
        moonY = stageTop + 22 + (stageBottom - stageTop - 44) / 2;
        initializeSelection(getData());
        catalogRevision = WerewolfSkinCatalog.revision();
        skinPage = Mth.clamp(skinPage, 0, pageCount() - 1);
        List<WerewolfSkinCatalog.Entry> skins = WerewolfSkinCatalog.entries(modelSet);
        modelButton = addRenderableWidget(new MoonUi.Pill(wide - MARGIN - 84, 6, 84, 16, Component.literal(modelSet.displayName()), false, this::switchModelSet));
        modelButton.active = pendingSkin == null;
        int cardY = TOP_BAND + 6;
        for (int row = 0; row < 4 && skinPage * 4 + row < skins.size(); row++) {
            addRenderableWidget(new SkinChoiceButton(MARGIN, cardY + row * (CARD_HEIGHT + CARD_GAP), skins.get(skinPage * 4 + row)));
        }
        int pagerY = tall - BOTTOM_BAND - 20;
        MoonUi.Pill previous = addRenderableWidget(new MoonUi.Pill(MARGIN, pagerY, 30, 16, Component.literal("<"), false, () -> changePage(-1)));
        previous.active = skinPage > 0;
        MoonUi.Pill next = addRenderableWidget(new MoonUi.Pill(MARGIN + LIST_WIDTH - 30, pagerY, 30, 16, Component.literal(">"), false, () -> changePage(1)));
        next.active = skinPage + 1 < pageCount();
        int formWidth = Math.min(84, (stageWidth - 8) / PREVIEW_FORMS.length - 4);
        int formX = moonX - (formWidth * PREVIEW_FORMS.length + 4 * (PREVIEW_FORMS.length - 1)) / 2;
        for (int index = 0; index < PREVIEW_FORMS.length; index++) {
            WerewolfForm form = PREVIEW_FORMS[index];
            formButtons.put(form, addRenderableWidget(new MoonUi.Pill(formX + index * (formWidth + 4), stageTop, formWidth, 16, formName(form), false, () -> {
                previewForm = form;
                resetView();
                updateButtons();
            })));
        }
        int rowY = tall - BOTTOM_BAND - 20;
        equipmentButton = addRenderableWidget(new MoonUi.Pill(moonX - 134, rowY, 130, 16, equipmentLabel(), false, () -> {
            showEquipment = !showEquipment;
            refreshPreviewPlayer();
            updateButtons();
        }));
        addRenderableWidget(new MoonUi.Pill(moonX + 4, rowY, 130, 16, Component.translatable("screen.howlingwerewolf.skins.reset_view"), false, this::resetView));
        int actionY = tall - 24;
        addRenderableWidget(new MoonUi.Pill(MARGIN, actionY, 64, 18, Component.translatable("gui.back"), false, this::onClose));
        addRenderableWidget(new MoonUi.Pill(wide - MARGIN - 110 - 4 - 122 - 4 - 92, actionY, 92, 18, Component.translatable("screen.howlingwerewolf.skins.packs"), false, this::openPacks));
        addRenderableWidget(new MoonUi.Pill(wide - MARGIN - 110 - 4 - 122, actionY, 122, 18, Component.translatable("screen.howlingwerewolf.skins.template"), false, this::createTemplate));
        applyButton = addRenderableWidget(new MoonUi.Pill(wide - MARGIN - 110, actionY, 110, 18, Component.translatable("screen.howlingwerewolf.skins.apply"), true, this::applySkin));
        refreshPreviewPlayer();
        updateButtons();
    }

    private void initializeSelection(WerewolfData data) {
        if (!initializedSelection && data != null) {
            selectedSkin = data.getSkinId();
            modelSet = data.getModelSet();
            showEquipment = data.showsEquipment();
            rememberedSkins.put(modelSet, selectedSkin);
            List<WerewolfSkinCatalog.Entry> skins = WerewolfSkinCatalog.entries(modelSet);
            for (int index = 0; index < skins.size(); index++) {
                if (skins.get(index).id().equals(selectedSkin)) skinPage = index / 4;
            }
            initializedSelection = true;
        }
    }

    private void chooseSkin(String skin) {
        selectedSkin = skin;
        initializedSelection = true;
        confirmationTimedOut = false;
        updateButtons();
    }

    private void applySkin() {
        WerewolfData data = getData();
        if (pendingSkin != null || data == null || !data.isWerewolf() || isCurrent(data)
                || WerewolfSkinCatalog.find(selectedSkin) == null) return;
        pendingSkin = selectedSkin;
        pendingModelSet = modelSet;
        pendingEquipment = showEquipment;
        pendingTicks = 0;
        confirmationTimedOut = false;
        PacketDistributor.sendToServer(new SetWerewolfSkinPacket(modelSet.id(), selectedSkin, showEquipment));
        updateButtons();
    }

    @Override
    public void tick() {
        WerewolfData data = getData();
        boolean hadSelection = initializedSelection;
        initializeSelection(data);
        if ((!hadSelection && initializedSelection) || catalogRevision != WerewolfSkinCatalog.revision()) rebuildWidgets();
        if (noticeTicks > 0 && --noticeTicks == 0) notice = null;
        if (pendingSkin != null) {
            if (data != null && pendingModelSet == data.getModelSet() && pendingSkin.equals(data.getSkinId()) && pendingEquipment == data.showsEquipment()) {
                pendingSkin = null;
                confirmationTimedOut = false;
            } else if (++pendingTicks >= 200) {
                pendingSkin = null;
                confirmationTimedOut = true;
            }
        }
        refreshPreviewPlayer();
        updateButtons();
    }

    private void updateButtons() {
        WerewolfData data = getData();
        if (applyButton != null) {
            boolean current = isCurrent(data);
            applyButton.active = data != null && data.isWerewolf() && !current && pendingSkin == null
                    && WerewolfSkinCatalog.find(selectedSkin) != null;
            applyButton.setMessage(Component.translatable(pendingSkin != null
                    ? "screen.howlingwerewolf.skins.applying"
                    : current ? "screen.howlingwerewolf.skins.applied" : "screen.howlingwerewolf.skins.apply"));
        }
        if (equipmentButton != null) equipmentButton.setMessage(equipmentLabel());
        if (modelButton != null) modelButton.active = pendingSkin == null;
        if (equipmentButton != null) equipmentButton.active = pendingSkin == null;
        formButtons.forEach((form, button) -> {
            button.active = form != previewForm;
            button.selected(form == previewForm);
        });
    }

    private void refreshPreviewPlayer() {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            previewPlayer = null;
            return;
        }
        if (previewPlayer == null || previewPlayer.level() != minecraft.level
                || !previewPlayer.getUUID().equals(minecraft.player.getUUID())) {
            previewPlayer = createPreviewPlayer(minecraft.level, minecraft.player.getGameProfile());
        }
        previewPlayer.setMainArm(minecraft.player.getMainArm());
        previewPlayer.tickCount = minecraft.player.tickCount;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            // Hidden equipment means worn armour and elytra; held items always show, as in the world.
            ItemStack stack = showEquipment || !isArmorSlot(slot) ? minecraft.player.getItemBySlot(slot) : ItemStack.EMPTY;
            if (!ItemStack.matches(previewPlayer.getItemBySlot(slot), stack)) {
                previewPlayer.setItemSlot(slot, stack.copy());
            }
        }
    }

    static RemotePlayer createPreviewPlayer(ClientLevel level, GameProfile profile) {
        RemotePlayer player = new RemotePlayer(level, profile);
        // This GUI-only entity never receives a spawn packet, but item/avatar extraction
        // still requires an ID.
        // Keep it detached from the level and separate from the live player's positive ID.
        player.setId(-1);
        return player;
    }

    @Override
    public void extractRenderState(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int logicalMouseX = toLogical(mouseX);
        int logicalMouseY = toLogical(mouseY);
        MoonUi.Theme theme = MoonUi.theme();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(uiScale, uiScale);
            backdrop.draw(graphics, theme, wide, tall, moonX, moonY, logicalMouseX, logicalMouseY);
            MoonUi.moon(graphics, moonX, moonY, moonRadius, MoonUi.moonPhase(), theme);
            MoonUi.pool(graphics, moonX, moonY + moonRadius * 0.88F, moonRadius * 0.62F, moonRadius * 0.11F, theme.shadow());
            renderPreview(graphics, logicalMouseX, logicalMouseY);
            MoonUi.centered(graphics, font, Component.translatable("screen.howlingwerewolf.skins.preview_controls"), moonX, tall - BOTTOM_BAND - 33, theme.faint());
            MoonUi.centered(graphics, font, Component.literal((skinPage + 1) + " / " + pageCount()), MARGIN + LIST_WIDTH / 2, tall - BOTTOM_BAND - 16, theme.sub());
            graphics.fill(0, 0, wide, TOP_BAND, MoonUi.BAND);
            graphics.fill(0, tall - BOTTOM_BAND, wide, tall, MoonUi.BAND);
            Component subtitle = Component.translatable(WerewolfSkinCatalog.errorCount() > 0
                    ? "screen.howlingwerewolf.skins.invalid_packs" : "screen.howlingwerewolf.skins.subtitle", WerewolfSkinCatalog.errorCount());
            int titleRoom = wide - MARGIN * 2 - 92;
            if (font.width(title) * 2 <= titleRoom / 2) {
                MoonUi.large(graphics, font, title, MARGIN, 6, 2, MoonUi.BAND_TEXT);
                int subtitleX = MARGIN + font.width(title) * 2 + 12;
                CompanionUi.label(graphics, font, subtitle, subtitleX, 10, wide - MARGIN - 92 - subtitleX, MoonUi.BAND_FAINT);
            } else {
                CompanionUi.label(graphics, font, title, MARGIN, 10, titleRoom, MoonUi.BAND_TEXT);
            }
            WerewolfData data = getData();
            Component current = data == null ? Component.translatable("screen.howlingwerewolf.skins.loading")
                    : Component.translatable("screen.howlingwerewolf.skins.current", data.getModelSet().displayName() + " / " + skinName(data.getSkinId()).getString());
            int bandTop = tall - BOTTOM_BAND;
            CompanionUi.label(graphics, font, current, MARGIN, bandTop + 7, wide / 2 - MARGIN - 8, 0xFFE3B667);
            Component status;
            int statusColor = MoonUi.BAND_SUB;
            if (notice != null) {
                status = notice;
            } else if (WerewolfSkinCatalog.find(selectedSkin) == null) {
                status = Component.translatable("screen.howlingwerewolf.skins.missing");
                statusColor = 0xFFEE7A8C;
            } else if (data == null || !data.isWerewolf()) {
                status = Component.translatable("screen.howlingwerewolf.not_werewolf");
            } else if (pendingSkin != null) {
                status = Component.translatable("screen.howlingwerewolf.skins.waiting", skinName(pendingSkin));
            } else if (isCurrent(data)) {
                status = Component.translatable("screen.howlingwerewolf.skins.saved", skinName(selectedSkin));
                statusColor = 0xFFE3B667;
            } else if (confirmationTimedOut) {
                status = Component.translatable("screen.howlingwerewolf.skins.retry");
                statusColor = 0xFFEE7A8C;
            } else {
                status = Component.translatable("screen.howlingwerewolf.skins.previewing", skinName(selectedSkin));
            }
            int statusRoom = wide / 2 - MARGIN - 8, statusY = bandTop + 7;
            for (var line : font.split(status, statusRoom).stream().limit(1).toList()) {
                graphics.text(font, line, wide - MARGIN - font.width(line), statusY, statusColor, false);
            }
            super.extractRenderState(graphics, logicalMouseX, logicalMouseY, partialTick);
            if (modelButton != null && modelButton.isHovered()) {
                Component detail = Component.translatable("screen.howlingwerewolf.skins.model." + modelSet.id() + ".tooltip");
                graphics.setTooltipForNextFrame(font, font.split(detail, Math.round(260 * uiScale)), mouseX, mouseY);
            }
            for (var child : children()) {
                if (child instanceof SkinChoiceButton button && button.isHovered()) {
                    Component detail = skinDescription(button.skin);
                    if (!button.skin.author().isBlank()) detail = detail.copy().append("\n").append(
                            Component.translatable("screen.howlingwerewolf.skins.author", button.skin.author()));
                    graphics.setTooltipForNextFrame(font, font.split(detail, Math.round(230 * uiScale)), mouseX, mouseY);
                    break;
                }
            }
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @Override
    public void extractBackground(MoonGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The canvas is drawn with the screen itself.
    }

    private void renderPreview(MoonGraphics graphics, int mouseX, int mouseY) {
        if (previewPlayer == null) return;
        float blocks = switch (previewForm) {
            case BEAST -> 3.05F;
            case QUADRUPED -> 1.5F;
            default -> 2.5F;
        };
        float scale = moonRadius * 1.72F / blocks * zoom;
        float baseline = moonY + moonRadius * 0.88F;
        float horizontal = (float) Math.atan((moonX - mouseX) / 80.0F);
        float vertical = (float) Math.atan((moonY - mouseY) / 100.0F);
        // Inventory-style mouse tracking plus an independent orbit angle for inspecting the back.
        // All pose writes target the detached preview player, never Minecraft.player.
        previewPlayer.yBodyRot = previewPlayer.yBodyRotO = 180.0F + rotation + horizontal * 20.0F;
        previewPlayer.setYRot(180.0F + rotation + horizontal * 40.0F);
        previewPlayer.setXRot(-vertical * 20.0F);
        previewPlayer.yHeadRot = previewPlayer.yHeadRotO = previewPlayer.getYRot();
        previewPlayer.xRotO = previewPlayer.getXRot();
        Quaternionf camera = new Quaternionf().rotateX(vertical * 20.0F * Mth.DEG_TO_RAD);
        Quaternionf pose = new Quaternionf().rotateZ(Mth.PI).mul(camera);
        int x0 = stageX, x1 = stageX + stageWidth, y0 = TOP_BAND, y1 = tall - BOTTOM_BAND;
        // The scissor follows the GUI matrix; the deferred entity viewport uses screen coordinates.
        graphics.enableScissor(x0, y0, x1, y1);
        try {
            WerewolfSkinRenderContext.withPreview(previewPlayer, previewForm, modelSet, selectedSkin, () -> {
                graphics.entity(previewPlayer, scale, (x0 + x1) / 2.0F, baseline, pose, camera);
            });
        } finally {
            graphics.disableScissor();
        }
    }

    private void resetView() {
        rotation = 0.0F;
        zoom = 1.0F;
    }

    private boolean isOverPreview(double mouseX, double mouseY) {
        double dx = mouseX - moonX, dy = mouseY - moonY;
        return dx * dx + dy * dy <= moonRadius * moonRadius;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX / uiScale, mouseY / uiScale);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT && isOverPreview(mouseX / uiScale, mouseY / uiScale)) {
            draggingPreview = true;
            return true;
        }
        return super.mouseClicked(logicalEvent(event), doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int button = event.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT && draggingPreview) {
            rotation = Mth.wrapDegrees(rotation + (float) (dragX / uiScale) * 1.5F);
            return true;
        }
        return super.mouseDragged(logicalEvent(event), dragX / uiScale, dragY / uiScale);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT && draggingPreview) {
            draggingPreview = false;
            return true;
        }
        return super.mouseReleased(logicalEvent(event));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverPreview(mouseX / uiScale, mouseY / uiScale)) {
            zoom = Mth.clamp(zoom + (float) scrollY * 0.08F, 0.65F, 1.3F);
            return true;
        }
        return super.mouseScrolled(mouseX / uiScale, mouseY / uiScale, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private int toLogical(double coordinate) { return (int) Math.floor(coordinate / uiScale); }

    private Component equipmentLabel() {
        return Component.translatable(showEquipment ? "screen.howlingwerewolf.skins.equipment_on"
                : "screen.howlingwerewolf.skins.equipment_off");
    }

    private static Component skinName(String id) {
        WerewolfSkinCatalog.Entry entry = WerewolfSkinCatalog.find(id);
        return entry == null ? Component.literal(id) : Component.literal(entry.name());
    }

    private static Component skinDescription(WerewolfSkinCatalog.Entry entry) {
        if (entry.builtin() && entry.modelSet() == WerewolfModelSet.MODERN) {
            return Component.translatable("skin.howlingwerewolf.modern.desc", entry.name());
        }
        return entry.builtin() ? Component.translatable("skin.howlingwerewolf." + WerewolfSkin.byId(entry.id()).getId() + ".desc")
                : Component.literal(entry.description());
    }

    private int pageCount() { return Math.max(1, (WerewolfSkinCatalog.entries(modelSet).size() + 3) / 4); }

    private boolean isCurrent(WerewolfData data) {
        return data != null && modelSet == data.getModelSet() && selectedSkin.equals(data.getSkinId()) && showEquipment == data.showsEquipment();
    }

    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    private void switchModelSet() {
        if (pendingSkin != null) return;
        rememberedSkins.put(modelSet, selectedSkin);
        modelSet = modelSet == WerewolfModelSet.CLASSIC ? WerewolfModelSet.MODERN : WerewolfModelSet.CLASSIC;
        selectedSkin = rememberedSkins.getOrDefault(modelSet, modelSet.defaultSkinId());
        skinPage = 0;
        var skins = WerewolfSkinCatalog.entries(modelSet);
        for (int index = 0; index < skins.size(); index++) if (skins.get(index).id().equals(selectedSkin)) skinPage = index / 4;
        confirmationTimedOut = false;
        initializedSelection = true;
        resetView();
        rebuildWidgets();
    }

    private void changePage(int delta) {
        skinPage = Mth.clamp(skinPage + delta, 0, pageCount() - 1);
        rebuildWidgets();
    }

    private void openPacks() {
        minecraft.setScreen(new PackSelectionScreen(minecraft.getResourcePackRepository(), repository -> {
            minecraft.options.updateResourcePacks(repository);
            minecraft.setScreen(this);
        }, minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title")));
    }

    private void createTemplate() {
        try {
            var directory = WerewolfSkinTemplate.export(minecraft.getResourcePackDirectory(), modelSet, selectedSkin);
            net.minecraft.Util.getPlatform().openFile(directory.toFile());
            notice = Component.translatable("screen.howlingwerewolf.skins.template_created");
        } catch (IOException | RuntimeException error) {
            LogUtils.getLogger().warn("Unable to create werewolf skin template", error);
            notice = Component.translatable("screen.howlingwerewolf.skins.template_failed");
        }
        noticeTicks = 200;
    }

    private static Component formName(WerewolfForm form) {
        return Component.translatable("screen.howlingwerewolf.skins.form." + form.id());
    }

    private MouseButtonEvent logicalEvent(MouseButtonEvent event) {
        return new MouseButtonEvent(event.x() / uiScale, event.y() / uiScale, event.buttonInfo());
    }

    private static WerewolfData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : WerewolfApi.get(minecraft.player).orElse(null);
    }

    private final class SkinChoiceButton extends Button {
        private final WerewolfSkinCatalog.Entry skin;

        private SkinChoiceButton(int x, int y, WerewolfSkinCatalog.Entry skin) {
            super(x, y, LIST_WIDTH, CARD_HEIGHT, Component.literal(skin.name()), clicked -> chooseSkin(skin.id()), DEFAULT_NARRATION);
            this.skin = skin;
        }

        @Override
        protected void renderWidget(net.minecraft.client.gui.GuiGraphics raw, int mouseX, int mouseY, float partialTick) {
            MoonGraphics graphics = new MoonGraphics(raw);
            boolean selected = selectedSkin.equals(skin.id());
            MoonUi.Theme theme = MoonUi.theme();
            int edge = selected ? theme.accent() : isHoveredOrFocused() ? theme.ink() : theme.line();
            MoonUi.round(graphics, getX(), getY(), width, height, 5, edge);
            MoonUi.round(graphics, getX() + 1, getY() + 1, width - 2, height - 2, 4, theme.node());
            graphics.text(font, font.plainSubstrByWidth(getMessage().getString(), width - 20), getX() + 10, getY() + 6, selected ? theme.accent() : theme.ink(), false);
            int y = getY() + 18;
            for (var line : font.split(skinDescription(skin), width - 20).stream().limit(2).toList()) {
                graphics.text(font, line, getX() + 10, y, theme.sub(), false);
                y += 10;
            }
            int color = !skin.builtin() ? 0xFF917EB3 : skin.modelSet() == WerewolfModelSet.MODERN ? 0xFF728998 : switch (WerewolfSkin.byId(skin.id())) {
                case ADRIAN -> 0xFF967055;
                case ASHEN -> 0xFF9A9EA1;
                case ONYX -> 0xFF4B4C51;
                case IVORY -> 0xFFEEEFE5;
            };
            graphics.fill(getX() + 10, getY() + 42, getX() + width - 10, getY() + 45, color);
        }
    }
}
