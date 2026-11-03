/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.companion.CompanionRules;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Stable roster beside the selected companion's state and grouped controls. */
public final class CompanionScreen extends Screen {
    private static final int PANEL_WIDTH = 432, PANEL_HEIGHT = 296, DETAIL_X = 160, DETAIL_WIDTH = 258;
    private final Screen parent;
    private CompoundTag snapshot;
    private UUID selected;
    private boolean focusedView;
    private final CompanionPreview preview = new CompanionPreview();
    private EditBox address;
    private Button saveAddress, resetAddress;
    private float scale = 1;
    private int refreshTicks, actionDelay;
    private Button customizeButton;
    private final Map<CompanionActionPacket.Action, Button> commands = new EnumMap<>(CompanionActionPacket.Action.class);

    public CompanionScreen(Screen parent, CompoundTag snapshot) {
        super(Component.translatable("screen.howlingwerewolf.companions"));
        this.parent = parent;
        this.snapshot = snapshot.copy();
        applyOpenTarget();
    }
    public void update(CompoundTag next) {
        boolean rosterChanged = !rosterIds(snapshot).equals(rosterIds(next));
        boolean wasFocused = focusedView;
        String previousAddress = snapshot.getStringOr("Address", "");
        snapshot = next.copy();
        applyOpenTarget();
        if (address != null && address.getValue().equals(previousAddress)) address.setValue(snapshot.getStringOr("Address", ""));
        if (rosterChanged || wasFocused != focusedView) rebuildWidgets(); else updateButtons();
    }
    private void applyOpenTarget() {
        if (!snapshot.contains("OpenTarget")) return;
        String target = snapshot.getStringOr("OpenTarget", "");
        if (target.isEmpty()) { focusedView = false; return; }
        ListTag entries = snapshot.getListOrEmpty("Companions");
        for (int i = 0; i < entries.size(); i++) {
            if (!target.equals(entries.getCompoundOrEmpty(i).getStringOr("Id", ""))) continue;
            try { selected = UUID.fromString(target); focusedView = true; } catch (IllegalArgumentException ignored) {}
            return;
        }
    }
    private static List<String> rosterIds(CompoundTag data) {
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        ListTag entries = data.getListOrEmpty("Companions");
        for (int i = 0; i < entries.size(); i++) ids.add(entries.getCompoundOrEmpty(i).getStringOr("Id", ""));
        return ids;
    }
    @Override protected void init() {
        clearWidgets();
        commands.clear();
        scale = Math.max(0.1F, Math.min(1, Math.min((width - 12F) / PANEL_WIDTH, (height - 12F) / panelHeight())));
        int left = left(), top = top();
        ListTag entries = snapshot.getListOrEmpty("Companions");
        if (selectedRecord() == null && !entries.isEmpty()) selected = UUID.fromString(entries.getCompoundOrEmpty(0).getStringOr("Id", CompanionActionPacket.NO_TARGET.toString()));
        if (focusedView) preview.bounds(left + 12, top + 62, 134, 186);
        for (int i = 0; !focusedView && i < Math.min(3, entries.size()); i++) {
            UUID id = UUID.fromString(entries.getCompoundOrEmpty(i).getStringOr("Id", CompanionActionPacket.NO_TARGET.toString()));
            addRenderableWidget(Button.builder(Component.empty(), button -> { selected = id; updateButtons(); })
                    .bounds(left + 12, top + 62 + i * 48, 134, 43).build());
        }
        CompanionActionPacket.Action[] actions = {CompanionActionPacket.Action.FOLLOW, CompanionActionPacket.Action.GUARD,
                CompanionActionPacket.Action.REST, CompanionActionPacket.Action.HUNT, CompanionActionPacket.Action.RECALL,
                CompanionActionPacket.Action.INVENTORY, CompanionActionPacket.Action.UPGRADE, CompanionActionPacket.Action.REVIVE};
        for (int i = 0; i < actions.length; i++) {
            var action = actions[i];
            commands.put(action, addRenderableWidget(Button.builder(actionText(action), ignored -> request(action))
                    .bounds(left + DETAIL_X + i % 3 * 86, top + 177 + i / 3 * 25, 82, 21).build()));
        }
        customizeButton = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.customize"), button -> {
            CompoundTag record = selectedRecord();
            if (record != null) minecraft.setScreen(new CompanionCustomizeScreen(this, snapshot, record));
        }).bounds(left + DETAIL_X + 172, top + 227, 82, 21).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.guide"), button ->
                        minecraft.setScreen(new CompanionGuideScreen(this)))
                .bounds(left + PANEL_WIDTH - 66, top + 9, 54, 20).build());
        if (focusedView) {
            addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.overview"), button ->
                            ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.OPEN, CompanionActionPacket.NO_TARGET)))
                    .bounds(left + PANEL_WIDTH - 128, top + 9, 54, 20).build());
        } else {
            String value = address == null ? snapshot.getStringOr("Address", "") : address.getValue();
            address = addRenderableWidget(new EditBox(font, left + 64, top + 266, 206, 20,
                    Component.translatable("screen.howlingwerewolf.companion.address")));
            address.setMaxLength(CompanionRules.MAX_ADDRESS_LENGTH * 2);
            address.setHint(Component.translatable("dialogue.howlingwerewolf.companion.address_default"));
            address.setValue(value);
            saveAddress = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.save_address"), button ->
                            requestAddress(address.getValue())).bounds(left + 274, top + 266, 64, 20).build());
            resetAddress = addRenderableWidget(Button.builder(Component.translatable("screen.howlingwerewolf.companion.reset_address"), button -> {
                address.setValue(""); requestAddress("");
            }).bounds(left + 346, top + 266, 72, 20).build());
        }
        updateButtons();
    }
    private void requestAddress(String value) {
        ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.ADDRESS, CompanionActionPacket.NO_TARGET, value));
        actionDelay = 5; updateButtons();
    }
    private Component actionText(CompanionActionPacket.Action action) {
        if (action == CompanionActionPacket.Action.UPGRADE) {
            CompoundTag record = selectedRecord();
            return Component.translatable("screen.howlingwerewolf.companion.upgrade", CompanionRules.upgradeCost(record == null ? 0 : record.getIntOr("Tier", 0)));
        }
        return Component.translatable("screen.howlingwerewolf.companion." + action.name().toLowerCase(java.util.Locale.ROOT));
    }
    private void request(CompanionActionPacket.Action action) {
        if (selected == null) return;
        ClientPacketDistributor.sendToServer(new CompanionActionPacket(action, selected));
        actionDelay = 5;
        updateButtons();
    }
    @Override public void tick() {
        if (actionDelay > 0) actionDelay--;
        if (snapshot.getBooleanOr("SystemEnabled", true)) {
            ListTag records = snapshot.getListOrEmpty("Companions");
            for (int i = 0; i < records.size(); i++) {
                CompoundTag record = records.getCompoundOrEmpty(i);
                for (String key : List.of("HuntRemaining", "HuntCooldownRemaining")) record.putLong(key, Math.max(0, record.getLongOr(key, 0) - 1));
            }
        }
        updateButtons();
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            ClientPacketDistributor.sendToServer(new CompanionActionPacket(CompanionActionPacket.Action.REFRESH, CompanionActionPacket.NO_TARGET));
        }
    }
    private void updateButtons() {
        CompoundTag record = selectedRecord();
        boolean systemEnabled = snapshot.getBooleanOr("SystemEnabled", true);
        if (address != null) {
            address.setEditable(systemEnabled);
            saveAddress.active = systemEnabled && actionDelay == 0 && !address.getValue().equals(snapshot.getStringOr("Address", ""));
            resetAddress.active = systemEnabled && actionDelay == 0 && (!address.getValue().isEmpty() || !snapshot.getStringOr("Address", "").isEmpty());
        }
        if (customizeButton != null) customizeButton.active = systemEnabled && record != null && mode(record).hasEntity();
        commands.forEach((action, button) -> {
            button.setMessage(actionText(action));
            if (!systemEnabled || record == null || actionDelay > 0) { button.active = false; return; }
            CompanionMode current = mode(record);
            boolean enabled = record.getBooleanOr("Enabled", false);
            button.active = switch (action) {
                case FOLLOW -> current.hasEntity() && current != CompanionMode.FOLLOW && enabled;
                case GUARD -> current.hasEntity() && current != CompanionMode.GUARD && enabled;
                case REST -> current.hasEntity() && current != CompanionMode.REST;
                case HUNT -> current.hasEntity() && enabled && record.getLongOr("HuntCooldownRemaining", 0) == 0;
                case RECALL -> current != CompanionMode.DEAD;
                case REVIVE -> current == CompanionMode.DEAD;
                case UPGRADE -> current.hasEntity() && enabled && record.getIntOr("Tier", 0) < snapshot.getIntOr("Skill", 0);
                case INVENTORY -> current.hasEntity() && record.getBooleanOr("Nearby", false);
                default -> false;
            };
        });
    }
    private CompoundTag selectedRecord() {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompoundOrEmpty(i);
            if (selected != null && selected.toString().equals(entry.getStringOr("Id", ""))) return entry;
        }
        return null;
    }
    private static CompanionMode mode(CompoundTag entry) {
        return CompanionMode.values()[Math.clamp(entry.getIntOr("Mode", 0), 0, CompanionMode.values().length - 1)];
    }
    private String fit(String text, int maxWidth) {
        return font.width(text) <= maxWidth ? text : font.plainSubstrByWidth(text, maxWidth - font.width("…")) + "…";
    }
    private void wrapped(GuiGraphicsExtractor graphics, Component text, int x, int y, int width, int color, int maxLines) {
        int count = 0;
        for (var line : font.split(text, width)) {
            if (count++ >= maxLines) break;
            graphics.text(font, line, x, y, color, false); y += 11;
        }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        int left = left(), top = top();
        graphics.pose().pushMatrix(); graphics.pose().scale(scale, scale);
        graphics.fill(left, top, left + PANEL_WIDTH, top + panelHeight(), 0xF0181724);
        graphics.outline(left, top, PANEL_WIDTH, panelHeight(), 0xFF8674BB);
        graphics.text(font, focusedView ? Component.translatable("screen.howlingwerewolf.companion.detail") : title, left + 14, top + 14, 0xFFE3D3FF, false);
        Component status = snapshot.getBooleanOr("SystemEnabled", true)
                ? Component.translatable("screen.howlingwerewolf.companion.capacity", snapshot.getListOrEmpty("Companions").size(), snapshot.getIntOr("Capacity", 0), snapshot.getIntOr("Skill", 0))
                : Component.translatable("message.howlingwerewolf.companion.disabled");
        graphics.text(font, status, left + 14, top + 37, 0xFFB5CDE8, false);
        graphics.fill(left + 151, top + 60, left + 152, top + 248, 0xFF51425F);
        CompoundTag record = selectedRecord();
        if (record == null) {
            graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.no_selection"), left + DETAIL_X, top + 76, 0xFFCFCCD9, false);
        } else {
            if (focusedView) preview.render(graphics, snapshot.getStringOr("Model", "modern"), record.getStringOr("Skin", ""), scale, mouseX / scale, mouseY / scale);
            graphics.text(font, fit(record.getStringOr("Name", ""), DETAIL_WIDTH), left + DETAIL_X, top + 65, 0xFFE3D3FF, false);
            graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.tier", record.getIntOr("Tier", 0)).append(" · ")
                    .append(Component.translatable(mode(record).translationKey())), left + DETAIL_X, top + 82, 0xFFBEB5CF, false);
            float max = Math.max(1, record.getFloatOr("MaxHealth", 30));
            float hp = Math.clamp(record.getFloatOr("Health", 0), 0, max);
            graphics.fill(left + DETAIL_X, top + 99, left + DETAIL_X + DETAIL_WIDTH, top + 111, 0xFF3A2A38);
            graphics.fill(left + DETAIL_X, top + 99, left + DETAIL_X + Math.round(DETAIL_WIDTH * hp / max), top + 111, 0xFFAC4766);
            graphics.centeredText(font, Component.translatable("screen.howlingwerewolf.companion.health", Math.round(hp), Math.round(max)), left + DETAIL_X + DETAIL_WIDTH / 2, top + 101, 0xFFFFFFFF);
            Component detail = mode(record) == CompanionMode.HUNT
                    ? Component.translatable("screen.howlingwerewolf.companion.hunt_remaining", String.format(java.util.Locale.ROOT, "%.2f", record.getLongOr("HuntRemaining", 0) / 24000.0))
                    : record.getLongOr("HuntCooldownRemaining", 0) > 0
                    ? Component.translatable("screen.howlingwerewolf.companion.hunt_cooldown", String.format(java.util.Locale.ROOT, "%.2f", record.getLongOr("HuntCooldownRemaining", 0) / 24000.0))
                    : record.getBooleanOr("Enabled", false) ? Component.empty() : Component.translatable("screen.howlingwerewolf.companion.suspended_short");
            wrapped(graphics, detail, left + DETAIL_X, top + 128, DETAIL_WIDTH, 0xFFD7BA77, 3);
        }
        if (!focusedView) graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.address"), left + 12, top + 272, 0xFFBEB5CF, false);
        super.extractRenderState(graphics, Math.round(mouseX / scale), Math.round(mouseY / scale), partialTick);
        ListTag entries = snapshot.getListOrEmpty("Companions");
        for (int i = 0; !focusedView && i < Math.min(3, entries.size()); i++) {
            CompoundTag entry = entries.getCompoundOrEmpty(i);
            int y = top + 62 + i * 48;
            boolean chosen = selected != null && selected.toString().equals(entry.getStringOr("Id", ""));
            if (chosen) graphics.outline(left + 12, y, 134, 43, 0xFFE3C5FF);
            graphics.text(font, fit(entry.getStringOr("Name", ""), 118), left + 20, y + 9, chosen ? 0xFFFFE8FF : 0xFFE7E2EC, false);
            graphics.text(font, Component.translatable("screen.howlingwerewolf.companion.tier", entry.getIntOr("Tier", 0)).append(" · ")
                    .append(Component.translatable(mode(entry).translationKey())), left + 20, y + 25, 0xFFCFCCD9, false);
            if (mouseX / scale >= left + 12 && mouseX / scale < left + 146 && mouseY / scale >= y && mouseY / scale < y + 43
                    && font.width(entry.getStringOr("Name", "")) > 118) {
                graphics.setTooltipForNextFrame(font, font.split(Component.literal(entry.getStringOr("Name", "")), 240), mouseX, mouseY);
            }
        }
        graphics.pose().popMatrix();
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {}
    private int left() { return (Math.round(width / scale) - PANEL_WIDTH) / 2; }
    private int panelHeight() { return focusedView ? 264 : PANEL_HEIGHT; }
    private int top() { return (Math.round(height / scale) - panelHeight()) / 2; }
    private MouseButtonEvent logical(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return focusedView && preview.click(event.x() / scale, event.y() / scale, event.button()) || super.mouseClicked(logical(event), doubleClick);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        return focusedView && preview.release(event.button()) || super.mouseReleased(logical(event));
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double x, double y) {
        return focusedView && preview.drag(x / scale) || super.mouseDragged(logical(event), x / scale, y / scale);
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        return focusedView && preview.scroll(x / scale, y / scale, dy) || super.mouseScrolled(x / scale, y / scale, dx, dy);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
