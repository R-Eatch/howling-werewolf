/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A fitting room: every coat is one click away, and the companion changes only when you apply it. */
public final class CompanionCustomizeScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.";
    private static final int MOON_X = 124, MOON_RADIUS = 96, SIDE_X = 250, SIDE_W = 258, ACTION_W = 130;
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
        pendingName = record == null ? "" : record.getString("CustomName");
        pendingSkin = record == null ? "" : record.getString("Skin");
        model = CompanionUi.str(snapshot, "Model", "modern");
    }
    @Override protected Tab tab() { return Tab.LOOK; }
    @Override protected int actionsWidth() { return ACTION_W + 10; }
    @Override protected void onSnapshot(CompoundTag previous) {
        String nextModel = CompanionUi.str(snapshot, "Model", model);
        if (!nextModel.equals(model)) {
            // The world switched model family: coats differ, so rebuild from the companion's own coat.
            model = nextModel;
            CompoundTag record = selectedRecord();
            pendingSkin = record == null ? "" : record.getString("Skin");
            rebuildWidgets();
        }
    }
    @Override protected void build(int left, int top) {
        previewActive = true;
        coats.clear(); coatIds.clear();
        int moonY = top + (TOP_BAND + HEIGHT - LOWER_BAND) / 2;
        preview.stage(left + MOON_X, moonY, MOON_RADIUS, left + 6, top + TOP_BAND, left + SIDE_X - 6, top + HEIGHT - LOWER_BAND);
        int x = left + SIDE_X;
        name = addRenderableWidget(new EditBox(font, x + 2, top + 49, SIDE_W - 84, 12, Component.translatable(KEY + "name")));
        name.setBordered(false);
        name.setMaxLength(64);
        name.setValue(pendingName);
        name.setResponder(value -> pendingName = value);
        saveName = addRenderableWidget(new UiButton(x + SIDE_W - 72, top + 44, 72, 18, Component.translatable(KEY + "save_name"), Look.SOFT,
                () -> request(CompanionActionPacket.Action.RENAME, name.getValue())));
        ListTag skins = snapshot.getList("Skins", 10);
        int columns = 3, gap = 4, chipW = (SIDE_W - gap * (columns - 1)) / columns;
        for (int i = 0; i < skins.size(); i++) {
            CompoundTag skin = skins.getCompound(i);
            String id = skin.getString("Id");
            coatIds.add(id);
            UiButton chip = new UiButton(x + i % columns * (chipW + gap), top + 112 + i / columns * 26, chipW, 22,
                    Component.literal(skin.getString("Name")), Look.CHIP, () -> { pendingSkin = id; refreshControls(); });
            coats.add(addRenderableWidget(chip));
        }
        applySkin = addRenderableWidget(new UiButton(left + WIDTH - 12 - ACTION_W, top + HEIGHT - LOWER_BAND + 10, ACTION_W, 26, Component.translatable(KEY + "apply_skin"), Look.PRIMARY,
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
        saveName.active = editable && actionDelay == 0 && !name.getValue().equals(record.getString("CustomName"));
        applySkin.active = editable && actionDelay == 0 && !pendingSkin.equals(record.getString("Skin"));
        name.setEditable(editable);
        name.setTextColor(MoonUi.theme().ink());
        for (int i = 0; i < coats.size(); i++) {
            coats.get(i).active = editable;
            coats.get(i).selected(coatIds.get(i).equals(pendingSkin));
        }
    }
    @Override protected Focus focus() {
        return new Focus(null, Component.translatable(KEY + "skin_hint"));
    }
    @Override protected void body(MoonGraphics graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY) {
        if (selectedRecord() == null) return;
        int moonY = top + (TOP_BAND + HEIGHT - LOWER_BAND) / 2;
        MoonUi.glow(graphics, left + MOON_X, moonY, MOON_RADIUS * 1.6F, theme.bloom());
        preview.render(graphics, model, pendingSkin, scale, mouseX, mouseY, theme);
        int x = left + SIDE_X;
        CompanionUi.section(graphics, font, Component.translatable(KEY + "name"), x, top + 32, SIDE_W, theme);
        graphics.fill(x, top + 62, x + SIDE_W - 80, top + 63, name != null && name.isFocused() ? theme.accent() : theme.line());
        CompanionUi.wrapped(graphics, font, Component.translatable(KEY + "name_hint"), x, top + 68, SIDE_W, theme.faint(), 1);
        CompanionUi.section(graphics, font, Component.translatable(KEY + "skin").append(" · " + (model.equals("modern") ? "Modern" : "Classic")), x, top + 96, SIDE_W, theme);
    }
    @Override protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) {
        return preview.contains(mouseX, mouseY) ? Component.translatable(KEY + "preview_hint") : null;
    }
}
