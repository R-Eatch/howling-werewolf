/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.UUID;

public final class CompanionCustomizeScreen extends Screen {
    private static final int PANEL_WIDTH = 432, PANEL_HEIGHT = 264;
    private final CompanionScreen parent;
    private final CompanionPreview preview = new CompanionPreview();
    private float scale = 1;
    private final UUID id;
    private final String initialName;
    private ListTag skins;
    private String model;
    private String selectedSkin;
    private EditBox name;
    private Button skinButton;
    private Button saveName;
    private Button applySkin;
    private boolean editable = true;
    private int actionDelay;
    private int ticks;

    public CompanionCustomizeScreen(CompanionScreen parent, CompoundTag snapshot, CompoundTag record) {
        super(Component.translatable("screen.howlingwerewolf.companion.customize"));
        this.parent = parent;
        this.id = UUID.fromString(record.getStringOr("Id", CompanionActionPacket.NO_TARGET.toString()));
        initialName = record.getStringOr("CustomName", "");
        skins = snapshot.getListOrEmpty("Skins").copy();
        model = snapshot.getStringOr("Model", "modern");
        selectedSkin = record.getStringOr("Skin", "");
        editable = snapshot.getBooleanOr("SystemEnabled", true)
                && available(record);
    }
    public void update(CompoundTag snapshot) {
        parent.update(snapshot);
        editable = false;
        String currentSkin = "";
        ListTag companions = snapshot.getListOrEmpty("Companions");
        for (int i = 0; i < companions.size(); i++) {
            CompoundTag record = companions.getCompoundOrEmpty(i);
            if (id.toString().equals(record.getStringOr("Id", ""))) {
                editable = snapshot.getBooleanOr("SystemEnabled", true)
                        && available(record);
                currentSkin = record.getStringOr("Skin", "");
            }
        }
        String nextModel = snapshot.getStringOr("Model", model);
        skins = snapshot.getListOrEmpty("Skins").copy();
        if (!nextModel.equals(model)) {
            model = nextModel;
            selectedSkin = currentSkin;
            updateSkinLabel();
        }
    }
    public void openManagement(CompoundTag snapshot) {
        parent.update(snapshot);
        minecraft.setScreen(parent);
    }
    private static boolean available(CompoundTag record) {
        int mode = record.getIntOr("Mode", 0);
        return mode != com.howlingwerewolf.companion.CompanionMode.HUNT.ordinal()
                && mode != com.howlingwerewolf.companion.CompanionMode.DEAD.ordinal();
    }
    @Override protected void init() {
        clearWidgets();
        scale = Math.max(0.1F, Math.min(1, Math.min((width - 12F) / PANEL_WIDTH, (height - 12F) / PANEL_HEIGHT)));
        int left = left(), top = top();
        preview.bounds(left + 12, top + 44, 142, 208);
        String value = name == null ? initialName : name.getValue();
        name = addRenderableWidget(new EditBox(font, left + 170, top + 72, 164, 20,
                Component.translatable("screen.howlingwerewolf.companion.name")));
        name.setMaxLength(64);
        name.setValue(value);
        saveName = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.save_name"), button ->
                        request(CompanionActionPacket.Action.RENAME, name.getValue()))
                .bounds(left + 340, top + 72, 76, 20).build());
        skinButton = addRenderableWidget(Button.builder(Component.empty(), button -> cycleSkin())
                .bounds(left + 170, top + 140, 164, 20).build());
        updateSkinLabel();
        applySkin = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.apply_skin"), button ->
                        request(CompanionActionPacket.Action.SKIN, selectedSkin))
                .bounds(left + 340, top + 140, 76, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.guide"), button ->
                        minecraft.setScreen(new CompanionGuideScreen(this)))
                .bounds(left + PANEL_WIDTH - 66, top + 9, 54, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(left + 316, top + 230, 100, 20).build());
        setInitialFocus(name);
    }
    private void request(CompanionActionPacket.Action action, String value) {
        if (!editable || actionDelay > 0) return;
        ClientPacketDistributor.sendToServer(new CompanionActionPacket(action, id, value));
        actionDelay = 5;
    }
    private void cycleSkin() {
        if (skins.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < skins.size(); i++) if (skins.getCompoundOrEmpty(i).getStringOr("Id", "").equals(selectedSkin)) index = i;
        selectedSkin = skins.getCompoundOrEmpty((index + 1) % skins.size()).getStringOr("Id", "");
        updateSkinLabel();
    }
    private void updateSkinLabel() {
        if (skinButton == null) return;
        String label = selectedSkin;
        for (int i = 0; i < skins.size(); i++) {
            CompoundTag skin = skins.getCompoundOrEmpty(i);
            if (skin.getStringOr("Id", "").equals(selectedSkin)) label = skin.getStringOr("Name", "");
        }
        skinButton.setMessage(Component.literal(model.equals("modern") ? "Modern · " : "Classic · ").append(label));
    }
    @Override public void tick() {
        if (actionDelay > 0) actionDelay--;
        saveName.active = applySkin.active = editable && actionDelay == 0;
        skinButton.active = editable;
        name.setEditable(editable);
        if (++ticks % 20 == 0) ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.REFRESH, CompanionActionPacket.NO_TARGET));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {
        extractTransparentBackground(graphics);
        int left = left(), top = top();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(scale, scale);
            graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0181724);
            graphics.outline(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF8674BB);
            graphics.text(font, title, left + 14, top + 14, 0xFFE3D3FF, false);
            preview.render(graphics, model, selectedSkin, scale, x / scale, y / scale);
            graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.name"), left + 170, top + 54, 0xFFE7E2EC, false);
            graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.skin"), left + 170, top + 122, 0xFFE7E2EC, false);
            super.extractRenderState(graphics, Math.round(x / scale), Math.round(y / scale), partialTick);
        } finally { graphics.pose().popMatrix(); }
    }
    private int left() { return (Math.round(width / scale) - PANEL_WIDTH) / 2; }
    private int top() { return (Math.round(height / scale) - PANEL_HEIGHT) / 2; }
    private MouseButtonEvent logical(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return preview.click(event.x() / scale, event.y() / scale, event.button()) || super.mouseClicked(logical(event), doubleClick);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return preview.release(event.button()) || super.mouseReleased(logical(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double x, double y) { return preview.drag(x / scale) || super.mouseDragged(logical(event), x / scale, y / scale); }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) { return preview.scroll(x / scale, y / scale, dy) || super.mouseScrolled(x / scale, y / scale, dx, dy); }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {}
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
