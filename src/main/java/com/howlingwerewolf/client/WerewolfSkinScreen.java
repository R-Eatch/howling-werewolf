package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfSkinIds;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.network.SetWerewolfSkinPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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
public final class WerewolfSkinScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 390;
    private static final int BACKGROUND = 0xF0110D13;
    private static final int INNER = 0xFF211820;
    private static final int BORDER = 0xFF743547;
    private static final int ACCENT = 0xFFD94A5E;
    private static final int GOLD = 0xFFFFD75A;
    private static final int TEXT = 0xFFE8E2E4;
    private static final int DIM = 0xFFAC9EA5;
    private static final WerewolfForm[] PREVIEW_FORMS = {
            WerewolfForm.WEREWOLF, WerewolfForm.QUADRUPED, WerewolfForm.BEAST
    };
    private final Screen parent;
    private final Map<WerewolfForm, Button> formButtons = new EnumMap<>(WerewolfForm.class);
    private String selectedSkin = WerewolfSkinIds.DEFAULT_ID;
    private String pendingSkin;
    private int skinPage;
    private int catalogRevision = -1;
    private Component notice;
    private int noticeTicks;
    private WerewolfForm previewForm = WerewolfForm.WEREWOLF;
    private RemotePlayer previewPlayer;
    private Button applyButton;
    private Button equipmentButton;
    private boolean initializedSelection;
    private boolean showEquipment = true;
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
        uiScale = Math.max(0.1F, Math.min(0.82F,
                Math.min((width - 12.0F) / PANEL_WIDTH, (height - 12.0F) / PANEL_HEIGHT)));
        int left = panelLeft();
        int top = panelTop();
        initializeSelection(getData());
        catalogRevision = WerewolfSkinCatalog.revision();
        skinPage = Mth.clamp(skinPage, 0, pageCount() - 1);
        List<WerewolfSkinCatalog.Entry> skins = WerewolfSkinCatalog.entries();
        for (int row = 0; row < 4 && skinPage * 4 + row < skins.size(); row++) {
            addRenderableWidget(new SkinChoiceButton(left + 12, top + 76 + row * 57, skins.get(skinPage * 4 + row)));
        }
        Button previous = addRenderableWidget(Button.builder(Component.literal("<"), clicked -> changePage(-1))
                .bounds(left + 12, top + 307, 24, 20).build());
        previous.active = skinPage > 0;
        Button next = addRenderableWidget(Button.builder(Component.literal(">"), clicked -> changePage(1))
                .bounds(left + 110, top + 307, 24, 20).build());
        next.active = skinPage + 1 < pageCount();
        for (int index = 0; index < PREVIEW_FORMS.length; index++) {
            WerewolfForm form = PREVIEW_FORMS[index];
            Button button = Button.builder(formName(form), clicked -> {
                previewForm = form;
                resetView();
                updateButtons();
            }).bounds(left + 144 + index * 88, top + 76, 84, 20).build();
            formButtons.put(form, addRenderableWidget(button));
        }
        equipmentButton = addRenderableWidget(Button.builder(equipmentLabel(), clicked -> {
            showEquipment = !showEquipment;
            refreshPreviewPlayer();
            updateButtons();
        }).bounds(left + 144, top + 307, 152, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.skins.reset_view"), clicked -> resetView())
                .bounds(left + 302, top + 307, 106, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), clicked -> onClose())
                .bounds(left + 12, top + 358, 64, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.skins.packs"), clicked -> openPacks())
                .bounds(left + 80, top + 358, 86, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.skins.template"), clicked -> createTemplate())
                .bounds(left + 170, top + 358, 122, 20).build());
        applyButton = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.skins.apply"), clicked -> applySkin())
                .bounds(left + 296, top + 358, 112, 20).build());
        refreshPreviewPlayer();
        updateButtons();
    }

    private void initializeSelection(WerewolfData data) {
        if (!initializedSelection && data != null) {
            selectedSkin = data.getSkinId();
            List<WerewolfSkinCatalog.Entry> skins = WerewolfSkinCatalog.entries();
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
        if (pendingSkin != null || data == null || !data.isWerewolf() || selectedSkin.equals(data.getSkinId())
                || WerewolfSkinCatalog.find(selectedSkin) == null) return;
        pendingSkin = selectedSkin;
        pendingTicks = 0;
        confirmationTimedOut = false;
        PacketDistributor.sendToServer(new SetWerewolfSkinPacket(selectedSkin));
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
            if (data != null && pendingSkin.equals(data.getSkinId())) {
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
            boolean current = data != null && selectedSkin.equals(data.getSkinId());
            applyButton.active = data != null && data.isWerewolf() && !current && pendingSkin == null
                    && WerewolfSkinCatalog.find(selectedSkin) != null;
            applyButton.setMessage(Component.translatable(pendingSkin != null
                    ? "screen.howlingwerewolf.skins.applying"
                    : current ? "screen.howlingwerewolf.skins.applied" : "screen.howlingwerewolf.skins.apply"));
        }
        if (equipmentButton != null) equipmentButton.setMessage(equipmentLabel());
        formButtons.forEach((form, button) -> button.active = form != previewForm);
    }

    private void refreshPreviewPlayer() {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            previewPlayer = null;
            return;
        }
        if (previewPlayer == null || previewPlayer.level() != minecraft.level
                || !previewPlayer.getUUID().equals(minecraft.player.getUUID())) {
            previewPlayer = new RemotePlayer(minecraft.level, minecraft.player.getGameProfile());
        }
        previewPlayer.setMainArm(minecraft.player.getMainArm());
        previewPlayer.tickCount = minecraft.player.tickCount;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = showEquipment ? minecraft.player.getItemBySlot(slot) : ItemStack.EMPTY;
            if (!ItemStack.matches(previewPlayer.getItemBySlot(slot), stack)) {
                previewPlayer.setItemSlot(slot, stack.copy());
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        int left = panelLeft();
        int top = panelTop();
        int logicalMouseX = toLogical(mouseX);
        int logicalMouseY = toLogical(mouseY);
        graphics.pose().pushPose();
        try {
            graphics.pose().scale(uiScale, uiScale, 1.0F);
            graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BACKGROUND);
            graphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, BORDER);
            graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 12, ACCENT);
            graphics.drawCenteredString(font, Component.translatable(WerewolfSkinCatalog.errorCount() > 0
                            ? "screen.howlingwerewolf.skins.invalid_packs" : "screen.howlingwerewolf.skins.subtitle",
                            WerewolfSkinCatalog.errorCount()),
                    left + PANEL_WIDTH / 2, top + 31, DIM);
            WerewolfData data = getData();
            Component current = data == null ? Component.translatable("screen.howlingwerewolf.skins.loading")
                    : Component.translatable("screen.howlingwerewolf.skins.current", skinName(data.getSkinId()));
            graphics.drawString(font, font.plainSubstrByWidth(current.getString(), PANEL_WIDTH - 32), left + 16, top + 56, GOLD, false);
            graphics.drawCenteredString(font, Component.literal((skinPage + 1) + " / " + pageCount()), left + 73, top + 313, TEXT);
            graphics.fill(left + 144, top + 101, left + 408, top + 301, INNER);
            graphics.renderOutline(left + 144, top + 101, 264, 200, BORDER);
            renderPreview(graphics, left, top, logicalMouseX, logicalMouseY);
            graphics.drawCenteredString(font, Component.translatable("screen.howlingwerewolf.skins.preview_controls"),
                    left + 276, top + 287, DIM);
            Component status;
            int statusColor = DIM;
            if (notice != null) {
                status = notice;
            } else if (WerewolfSkinCatalog.find(selectedSkin) == null) {
                status = Component.translatable("screen.howlingwerewolf.skins.missing");
                statusColor = ACCENT;
            } else if (data == null || !data.isWerewolf()) {
                status = Component.translatable("screen.howlingwerewolf.not_werewolf");
            } else if (pendingSkin != null) {
                status = Component.translatable("screen.howlingwerewolf.skins.waiting", skinName(pendingSkin));
            } else if (selectedSkin.equals(data.getSkinId())) {
                status = Component.translatable("screen.howlingwerewolf.skins.saved", skinName(selectedSkin));
                statusColor = GOLD;
            } else if (confirmationTimedOut) {
                status = Component.translatable("screen.howlingwerewolf.skins.retry");
                statusColor = ACCENT;
            } else {
                status = Component.translatable("screen.howlingwerewolf.skins.previewing", skinName(selectedSkin));
            }
            int statusY = top + 332;
            for (var line : font.split(status, PANEL_WIDTH - 32).stream().limit(2).toList()) {
                graphics.drawString(font, line, left + (PANEL_WIDTH - font.width(line)) / 2, statusY, statusColor, false);
                statusY += 10;
            }
            super.render(graphics, logicalMouseX, logicalMouseY, partialTick);
            for (var child : children()) {
                if (child instanceof SkinChoiceButton button && button.isHovered()) {
                    Component detail = skinDescription(button.skin);
                    if (!button.skin.author().isBlank()) detail = detail.copy().append("\n").append(
                            Component.translatable("screen.howlingwerewolf.skins.author", button.skin.author()));
                    graphics.renderTooltip(font, font.split(detail, 230), logicalMouseX, logicalMouseY);
                    break;
                }
            }
        } finally {
            graphics.pose().popPose();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The explicit transparent pass in render keeps the existing menus' dimming without blur.
    }

    private void renderPreview(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        if (previewPlayer == null) return;
        int centerX = left + 276;
        int baseline = top + 270;
        int baseScale = switch (previewForm) {
            case BEAST -> 55;
            case QUADRUPED -> 83;
            default -> 72;
        };
        int scale = Math.round(baseScale * zoom);
        float horizontal = (float) Math.atan((centerX - mouseX) / 80.0F);
        float vertical = (float) Math.atan((top + 185 - mouseY) / 100.0F);
        // Inventory-style mouse tracking plus an independent orbit angle for inspecting the back.
        // All pose writes target the detached preview player, never Minecraft.player.
        previewPlayer.yBodyRot = previewPlayer.yBodyRotO = 180.0F + rotation + horizontal * 20.0F;
        previewPlayer.setYRot(180.0F + rotation + horizontal * 40.0F);
        previewPlayer.setXRot(-vertical * 20.0F);
        previewPlayer.yHeadRot = previewPlayer.yHeadRotO = previewPlayer.getYRot();
        previewPlayer.xRotO = previewPlayer.getXRot();
        Quaternionf camera = new Quaternionf().rotateX(vertical * 20.0F * Mth.DEG_TO_RAD);
        Quaternionf pose = new Quaternionf().rotateZ(Mth.PI).mul(camera);
        // GuiGraphics scissor coordinates are in screen space, independent of PoseStack scaling.
        graphics.enableScissor((int) Math.floor((left + 145) * uiScale),
                (int) Math.floor((top + 102) * uiScale),
                (int) Math.ceil((left + 407) * uiScale), (int) Math.ceil((top + 282) * uiScale));
        try {
            WerewolfSkinRenderContext.withPreview(previewPlayer, previewForm, selectedSkin, () ->
                    InventoryScreen.renderEntityInInventory(graphics, centerX, baseline, scale,
                            new Vector3f(), pose, camera, previewPlayer));
        } finally {
            graphics.disableScissor();
        }
    }

    private void resetView() {
        rotation = 0.0F;
        zoom = 1.0F;
    }

    private boolean isOverPreview(double mouseX, double mouseY) {
        int left = panelLeft();
        int top = panelTop();
        return mouseX >= left + 144 && mouseX < left + 408 && mouseY >= top + 101 && mouseY < top + 301;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX / uiScale, mouseY / uiScale);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverPreview(mouseX / uiScale, mouseY / uiScale)) {
            draggingPreview = true;
            return true;
        }
        return super.mouseClicked(mouseX / uiScale, mouseY / uiScale, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingPreview) {
            rotation = Mth.wrapDegrees(rotation + (float) (dragX / uiScale) * 1.5F);
            return true;
        }
        return super.mouseDragged(mouseX / uiScale, mouseY / uiScale, button, dragX / uiScale, dragY / uiScale);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingPreview) {
            draggingPreview = false;
            return true;
        }
        return super.mouseReleased(mouseX / uiScale, mouseY / uiScale, button);
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

    private int panelLeft() { return (Math.round(width / uiScale) - PANEL_WIDTH) / 2; }
    private int panelTop() { return (Math.round(height / uiScale) - PANEL_HEIGHT) / 2; }
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
        return entry.builtin() ? Component.translatable("skin.howlingwerewolf." + WerewolfSkin.byId(entry.id()).getId() + ".desc")
                : Component.literal(entry.description());
    }

    private int pageCount() { return Math.max(1, (WerewolfSkinCatalog.entries().size() + 3) / 4); }

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
            var directory = WerewolfSkinTemplate.export(minecraft.getResourcePackDirectory());
            Util.getPlatform().openFile(directory.toFile());
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

    private static WerewolfData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : WerewolfApi.get(minecraft.player).orElse(null);
    }

    private final class SkinChoiceButton extends Button {
        private final WerewolfSkinCatalog.Entry skin;

        private SkinChoiceButton(int x, int y, WerewolfSkinCatalog.Entry skin) {
            super(x, y, 122, 50, Component.literal(skin.name()), clicked -> chooseSkin(skin.id()), DEFAULT_NARRATION);
            this.skin = skin;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = selectedSkin.equals(skin.id());
            graphics.fill(getX(), getY(), getX() + width, getY() + height,
                    selected ? 0xFF3D2630 : isHoveredOrFocused() ? 0xFF30242B : INNER);
            graphics.renderOutline(getX(), getY(), width, height, selected ? ACCENT : isHoveredOrFocused() ? GOLD : BORDER);
            graphics.drawString(font, font.plainSubstrByWidth(getMessage().getString(), width - 20), getX() + 10, getY() + 7, selected ? GOLD : TEXT, false);
            int y = getY() + 21;
            for (var line : font.split(skinDescription(skin), width - 20).stream().limit(2).toList()) {
                graphics.drawString(font, line, getX() + 10, y, DIM, false);
                y += 10;
            }
            int color = !skin.builtin() ? 0xFF917EB3 : switch (WerewolfSkin.byId(skin.id())) {
                case ADRIAN -> 0xFF967055;
                case ASHEN -> 0xFF9A9EA1;
                case ONYX -> 0xFF4B4C51;
                case IVORY -> 0xFFEEEFE5;
            };
            graphics.fill(getX() + 10, getY() + 45, getX() + width - 10, getY() + 48, color);
        }
    }
}
