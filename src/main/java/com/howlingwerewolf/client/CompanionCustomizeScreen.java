/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A fitting room: every coat is one click away, and nothing changes on the companion until applied. */
public final class CompanionCustomizeScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.";
    private static final int STAGE_X = 10, STAGE_W = 170, SIDE_X = 190, SIDE_W = 270;
    private final List<UiButton> coats = new ArrayList<>();
    private final List<String> coatIds = new ArrayList<>();
    private EditBox name;
    private UiButton saveName, applySkin;
    private String pendingName;
    private String pendingSkin = "";
    private String model;
    private int actionDelay;

    CompanionCustomizeScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable(KEY + "customize"), parent, snapshot, selected);
        CompoundTag record = selectedRecord();
        pendingName = record == null ? "" : record.getStringOr("CustomName", "");
        pendingSkin = record == null ? "" : record.getStringOr("Skin", "");
        model = snapshot.getStringOr("Model", "modern");
    }
    @Override protected Tab tab() { return Tab.LOOK; }
    @Override protected void onSnapshot(CompoundTag previous) {
        String nextModel = snapshot.getStringOr("Model", model);
        if (!nextModel.equals(model)) {
            // The world switched model family: coats differ, so rebuild from the companion's own coat.
            model = nextModel;
            CompoundTag record = selectedRecord();
            pendingSkin = record == null ? "" : record.getStringOr("Skin", "");
            rebuildWidgets();
        }
    }
    @Override protected void build(int left, int top) {
        previewActive = true;
        coats.clear(); coatIds.clear();
        preview.bounds(left + STAGE_X, top + BODY_TOP + 4, STAGE_W, 242);
        name = addRenderableWidget(new EditBox(font, left + SIDE_X + 5, top + 58, SIDE_W - 92, 12, Component.translatable(KEY + "name")));
        name.setBordered(false);
        name.setMaxLength(64);
        name.setValue(pendingName);
        name.setResponder(value -> pendingName = value);
        saveName = addRenderableWidget(new UiButton(left + SIDE_X + SIDE_W - 72, top + 52, 72, 20, Component.translatable(KEY + "save_name"), Look.SOFT,
                () -> request(CompanionActionPacket.Action.RENAME, name.getValue())));
        ListTag skins = snapshot.getListOrEmpty("Skins");
        int columns = 3, gap = 4, chipW = (SIDE_W - gap * (columns - 1)) / columns;
        for (int i = 0; i < skins.size(); i++) {
            CompoundTag skin = skins.getCompoundOrEmpty(i);
            String id = skin.getStringOr("Id", "");
            coatIds.add(id);
            UiButton chip = new UiButton(left + SIDE_X + i % columns * (chipW + gap), top + 116 + i / columns * 24, chipW, 21,
                    Component.literal(skin.getStringOr("Name", "")), Look.CHIP, () -> { pendingSkin = id; refreshControls(); });
            coats.add(addRenderableWidget(chip));
        }
        applySkin = addRenderableWidget(new UiButton(left + SIDE_X, top + 256, SIDE_W, 24, Component.translatable(KEY + "apply_skin"), Look.PRIMARY,
                () -> request(CompanionActionPacket.Action.SKIN, pendingSkin)));
    }
    private void request(CompanionActionPacket.Action action, String value) {
        if (selected == null || actionDelay > 0) return;
        send(action, selected, value);
        actionDelay = 5; refreshControls();
    }
    @Override public void tick() {
        if (actionDelay > 0) actionDelay--;
        super.tick();
    }
    @Override protected void refreshControls() {
        if (name == null) return;
        CompoundTag record = selectedRecord();
        boolean editable = record != null && systemEnabled() && mode(record) != CompanionMode.HUNT && mode(record) != CompanionMode.DEAD;
        saveName.active = editable && actionDelay == 0 && !name.getValue().equals(record.getStringOr("CustomName", ""));
        applySkin.active = editable && actionDelay == 0 && !pendingSkin.equals(record.getStringOr("Skin", ""));
        name.setEditable(editable);
        name.setTextColor(CompanionUi.TEXT);
        for (int i = 0; i < coats.size(); i++) {
            coats.get(i).active = editable;
            coats.get(i).selected(coatIds.get(i).equals(pendingSkin));
        }
    }
    @Override protected void body(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY) {
        if (selectedRecord() == null) return;
        preview.render(graphics, model, pendingSkin, scale, mouseX, mouseY);
        int x = left + SIDE_X;
        CompanionUi.section(graphics, font, Component.translatable(KEY + "name"), x, top + BODY_TOP + 6, SIDE_W);
        CompanionUi.card(graphics, x, top + 52, SIDE_W - 78, 20, CompanionUi.SURFACE, CompanionUi.LINE);
        CompanionUi.wrapped(graphics, font, Component.translatable(KEY + "name_hint"), x, top + 76, SIDE_W, CompanionUi.FAINT, 1);
        CompanionUi.section(graphics, font, Component.translatable(KEY + "skin").append(" · " + (model.equals("modern") ? "Modern" : "Classic")), x, top + 102, SIDE_W);
        int rows = (coats.size() + 2) / 3;
        CompanionUi.wrapped(graphics, font, Component.translatable(KEY + "skin_hint"), x, top + 116 + rows * 24 + 2, SIDE_W, CompanionUi.FAINT, 2);
    }
    @Override protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) {
        return preview.contains(mouseX, mouseY) ? Component.translatable(KEY + "preview_hint") : null;
    }
}
