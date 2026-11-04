/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.client.CompanionUi.Look;
import com.howlingwerewolf.client.CompanionUi.UiButton;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.companion.CompanionRules;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.network.CompanionActionPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** The pack page: roster, a live portrait, and the selected companion's status and orders. */
public final class CompanionScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.";
    private static final int ROSTER_X = 10, ROSTER_W = 128, STAGE_X = 146, STAGE_W = 128, DETAIL_X = 282, DETAIL_W = 178;
    private static final CompanionActionPacket.Action[] ORDERS = {CompanionActionPacket.Action.FOLLOW, CompanionActionPacket.Action.GUARD,
            CompanionActionPacket.Action.REST, CompanionActionPacket.Action.SORT, CompanionActionPacket.Action.HUNT};
    private static final CompanionActionPacket.Action[] DEEDS = {CompanionActionPacket.Action.RECALL, CompanionActionPacket.Action.INVENTORY,
            CompanionActionPacket.Action.UPGRADE};
    private final Map<CompanionActionPacket.Action, UiButton> commands = new EnumMap<>(CompanionActionPacket.Action.class);
    private final List<UiButton> slots = new ArrayList<>();
    private EditBox address;
    private UiButton saveAddress, resetAddress, appearance;
    private int actionDelay;
    private @Nullable CompanionActionPacket.Action hovered;

    public CompanionScreen(@Nullable Screen parent, CompoundTag snapshot) { this(parent, snapshot, null); }
    CompanionScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable("screen.howlingwerewolf.companions"), parent, snapshot, selected);
        String target = snapshot.getStringOr("OpenTarget", "");
        if (!target.isEmpty()) try { this.selected = UUID.fromString(target); } catch (IllegalArgumentException ignored) {}
    }
    @Override protected Tab tab() { return Tab.PACK; }

    @Override protected void onSnapshot(CompoundTag previous) {
        String before = previous.getStringOr("Address", "");
        if (address != null && address.getValue().equals(before)) address.setValue(snapshot.getStringOr("Address", ""));
    }

    @Override protected void build(int left, int top) {
        previewActive = true;
        commands.clear(); slots.clear();
        preview.bounds(left + STAGE_X, top + BODY_TOP + 4, STAGE_W, 242);
        for (int i = 0; i < CompanionRules.MAX_COMPANIONS; i++) {
            int index = i;
            UiButton card = new UiButton(left + ROSTER_X, top + 48 + i * 52, ROSTER_W, 48, Component.empty(), Look.CARD, () -> choose(index))
                    .hint(() -> slotHint(index));
            slots.add(addRenderableWidget(card));
        }
        String value = address == null ? snapshot.getStringOr("Address", "") : address.getValue();
        address = addRenderableWidget(new EditBox(font, left + ROSTER_X + 5, top + 222, ROSTER_W - 10, 12, Component.translatable(KEY + "address")));
        address.setBordered(false);
        address.setMaxLength(CompanionRules.MAX_ADDRESS_LENGTH * 2);
        address.setHint(Component.translatable("dialogue.howlingwerewolf.companion.address_default"));
        address.setValue(value);
        saveAddress = addRenderableWidget(new UiButton(left + ROSTER_X, top + 238, 62, 18, Component.translatable(KEY + "save_address"), Look.SOFT, () -> requestAddress(address.getValue())));
        resetAddress = addRenderableWidget(new UiButton(left + ROSTER_X + 66, top + 238, 62, 18, Component.translatable(KEY + "reset_address"), Look.SOFT, () -> { address.setValue(""); requestAddress(""); }));

        int tileW = (DETAIL_W - 8) / 5;
        List<Supplier<ItemLike>> orderIcons = List.of(() -> Items.LEAD, () -> Items.IRON_SWORD, () -> Items.RED_BED, () -> Items.CHEST, () -> Items.BOW);
        for (int i = 0; i < ORDERS.length; i++) {
            CompanionActionPacket.Action action = ORDERS[i];
            UiButton tile = new UiButton(left + DETAIL_X + i * (tileW + 2), top + 112, tileW, 36, Component.translatable(KEY + action.name().toLowerCase(java.util.Locale.ROOT)), Look.TILE, () -> request(action))
                    .icon(orderIcons.get(i)).hint(() -> disabledReason(action));
            commands.put(action, addRenderableWidget(tile));
        }
        int deedW = (DETAIL_W - 4) / 2;
        List<Supplier<ItemLike>> deedIcons = List.of(() -> Items.ENDER_PEARL, () -> Items.CHEST, ModItems.MOONBANE_PEARL::get);
        for (int i = 0; i < DEEDS.length; i++) {
            CompanionActionPacket.Action action = DEEDS[i];
            UiButton deed = new UiButton(left + DETAIL_X + i % 2 * (deedW + 4), top + 212 + i / 2 * 24, deedW, 21, deedLabel(action), Look.SOFT, () -> request(action))
                    .icon(deedIcons.get(i)).hint(() -> deedHint(action));
            commands.put(action, addRenderableWidget(deed));
        }
        appearance = addRenderableWidget(new UiButton(left + DETAIL_X + deedW + 4, top + 236, deedW, 21, Component.translatable(KEY + "tab.look"), Look.SOFT, () -> showTab(Tab.LOOK))
                .icon(() -> Items.NAME_TAG).hint(() -> Component.translatable(KEY + "customize")));
        // A fallen companion has one thing to do, so it takes the whole action area.
        commands.put(CompanionActionPacket.Action.REVIVE, addRenderableWidget(new UiButton(left + DETAIL_X, top + 212, DETAIL_W, 45, deedLabel(CompanionActionPacket.Action.REVIVE), Look.PRIMARY,
                () -> request(CompanionActionPacket.Action.REVIVE)).icon(() -> Items.TOTEM_OF_UNDYING).hint(() -> deedHint(CompanionActionPacket.Action.REVIVE))));
    }
    private void choose(int index) {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        if (index >= entries.size()) return;
        try { selected = UUID.fromString(entries.getCompoundOrEmpty(index).getStringOr("Id", "")); } catch (IllegalArgumentException ignored) {}
        refreshControls();
    }
    private void step(int direction) {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        if (entries.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < entries.size(); i++) if (selected != null && selected.toString().equals(entries.getCompoundOrEmpty(i).getStringOr("Id", ""))) index = i;
        choose(Math.floorMod(index + direction, entries.size()));
    }
    private void requestAddress(String value) {
        send(CompanionActionPacket.Action.ADDRESS, CompanionActionPacket.NO_TARGET, value);
        actionDelay = 5; refreshControls();
    }
    private void request(CompanionActionPacket.Action action) {
        if (selected == null) return;
        if (action == CompanionActionPacket.Action.SORT) { showTab(Tab.SORT); return; }
        send(action, selected, "");
        actionDelay = 5;
        refreshControls();
    }
    private Component deedLabel(CompanionActionPacket.Action action) {
        if (action == CompanionActionPacket.Action.UPGRADE) {
            CompoundTag record = selectedRecord();
            return Component.translatable(KEY + "upgrade", CompanionRules.upgradeCost(record == null ? 0 : record.getIntOr("Tier", 0)));
        }
        return Component.translatable(KEY + action.name().toLowerCase(java.util.Locale.ROOT));
    }
    @Override public void tick() {
        if (actionDelay > 0) actionDelay--;
        if (systemEnabled()) {
            ListTag records = snapshot.getListOrEmpty("Companions");
            for (int i = 0; i < records.size(); i++) {
                CompoundTag record = records.getCompoundOrEmpty(i);
                for (String key : List.of("HuntRemaining", "HuntCooldownRemaining")) record.putLong(key, Math.max(0, record.getLongOr(key, 0) - 1));
            }
        }
        super.tick();
    }
    private boolean canUse(CompanionActionPacket.Action action, CompoundTag record) {
        CompanionMode current = mode(record);
        boolean enabled = record.getBooleanOr("Enabled", false);
        return switch (action) {
            case FOLLOW -> current.hasEntity() && current != CompanionMode.FOLLOW && enabled;
            case GUARD -> current.hasEntity() && current != CompanionMode.GUARD && enabled;
            case REST -> current.hasEntity() && current != CompanionMode.REST;
            case HUNT -> current.hasEntity() && enabled && record.getLongOr("HuntCooldownRemaining", 0) == 0;
            case RECALL -> current != CompanionMode.DEAD;
            case REVIVE -> current == CompanionMode.DEAD;
            case UPGRADE -> current.hasEntity() && enabled && record.getIntOr("Tier", 0) < Math.min(snapshot.getIntOr("Skill", 0), CompanionRules.MAX_TIER);
            case INVENTORY -> current.hasEntity() && record.getBooleanOr("Nearby", false);
            case SORT -> current.hasEntity() && enabled && snapshot.getBooleanOr("SortingEnabled", false);
            default -> false;
        };
    }
    @Override protected void refreshControls() {
        if (address == null) return;
        CompoundTag record = selectedRecord();
        boolean system = systemEnabled();
        address.setEditable(system);
        address.setTextColor(CompanionUi.TEXT);
        saveAddress.active = system && actionDelay == 0 && !address.getValue().equals(snapshot.getStringOr("Address", ""));
        resetAddress.active = system && actionDelay == 0 && (!address.getValue().isEmpty() || !snapshot.getStringOr("Address", "").isEmpty());
        appearance.active = system && record != null && mode(record).hasEntity();
        appearance.visible = record != null && mode(record) != CompanionMode.DEAD;
        ListTag entries = snapshot.getListOrEmpty("Companions");
        int capacity = snapshot.getIntOr("Capacity", 0);
        for (int i = 0; i < slots.size(); i++) {
            UiButton slot = slots.get(i);
            boolean filled = i < entries.size();
            slot.active = filled;
            slot.selected(filled && selected != null && selected.toString().equals(entries.getCompoundOrEmpty(i).getStringOr("Id", "")));
        }
        commands.forEach((action, button) -> {
            if (action == CompanionActionPacket.Action.UPGRADE) button.setMessage(deedLabel(action));
            button.visible = record != null;
            button.active = system && record != null && actionDelay == 0 && canUse(action, record);
            if (record != null && ordinalOrder(action) >= 0) button.selected(modeFor(action) == mode(record));
        });
        if (record != null) {
            boolean dead = mode(record) == CompanionMode.DEAD;
            commands.get(CompanionActionPacket.Action.REVIVE).visible = dead;
            for (var action : DEEDS) commands.get(action).visible = !dead;
        }
    }
    private static int ordinalOrder(CompanionActionPacket.Action action) {
        for (int i = 0; i < ORDERS.length; i++) if (ORDERS[i] == action) return i;
        return -1;
    }
    private static CompanionMode modeFor(CompanionActionPacket.Action action) {
        return switch (action) {
            case FOLLOW -> CompanionMode.FOLLOW;
            case GUARD -> CompanionMode.GUARD;
            case REST -> CompanionMode.REST;
            case SORT -> CompanionMode.SORT;
            default -> CompanionMode.HUNT;
        };
    }
    private @Nullable Component slotHint(int index) {
        if (index < snapshot.getListOrEmpty("Companions").size()) return null;
        return Component.translatable(KEY + (index < snapshot.getIntOr("Capacity", 0) ? "slot.empty_hint" : "slot.locked_hint"));
    }
    private Component disabledReason(CompanionActionPacket.Action action) {
        CompoundTag record = selectedRecord();
        if (record == null) return null;
        Component description = Component.translatable(KEY + "mode_desc." + modeFor(action).getSerializedName());
        if (canUse(action, record) || modeFor(action) == mode(record)) return description;
        Component reason = reason(action, record);
        return reason == null ? description : reason;
    }
    private @Nullable Component reason(CompanionActionPacket.Action action, CompoundTag record) {
        CompanionMode current = mode(record);
        if (current == CompanionMode.DEAD) return Component.translatable(KEY + "reason.dead");
        if (current == CompanionMode.HUNT && action != CompanionActionPacket.Action.RECALL) return Component.translatable(KEY + "reason.hunting");
        if (!record.getBooleanOr("Enabled", false)) return Component.translatable(KEY + "reason.suspended");
        return switch (action) {
            case HUNT -> Component.translatable(KEY + "reason.cooldown", days(record.getLongOr("HuntCooldownRemaining", 0)));
            case SORT -> snapshot.getBooleanOr("SortingEnabled", false) ? null : Component.translatable(KEY + "reason.sort_off");
            case INVENTORY -> Component.translatable(KEY + "reason.far");
            case UPGRADE -> Component.translatable(KEY + "reason.upgrade_max");
            default -> null;
        };
    }
    private Component deedHint(CompanionActionPacket.Action action) {
        CompoundTag record = selectedRecord();
        if (record == null) return null;
        if (!canUse(action, record)) { Component reason = reason(action, record); if (reason != null) return reason; }
        int tier = record.getIntOr("Tier", 0);
        return switch (action) {
            case RECALL -> Component.translatable(KEY + "recall_hint");
            case INVENTORY -> Component.translatable(KEY + "inventory_hint");
            case UPGRADE -> Component.translatable(KEY + "upgrade_hint", CompanionRules.upgradeCost(tier), Math.min(tier + 1, CompanionRules.MAX_TIER));
            case REVIVE -> Component.translatable(KEY + "revive_hint");
            default -> null;
        };
    }
    private static String days(long ticks) { return String.format(java.util.Locale.ROOT, "%.2f", ticks / 24000.0); }

    @Override public boolean keyPressed(KeyEvent event) {
        if (!address.isFocused()) {
            int number = event.key() - org.lwjgl.glfw.GLFW.GLFW_KEY_1;
            if (number >= 0 && number < ORDERS.length) {
                UiButton order = commands.get(ORDERS[number]);
                if (order != null && order.active && order.visible) { request(ORDERS[number]); return true; }
            }
            if (event.isUp() || event.isDown()) { step(event.isUp() ? -1 : 1); return true; }
        }
        return super.keyPressed(event);
    }

    @Override protected void body(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY) {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        int capacity = snapshot.getIntOr("Capacity", 0);
        Component count = Component.literal(entries.size() + " / " + capacity);
        CompanionUi.section(graphics, font, Component.translatable(KEY + "tab.pack").append("  ").append(count), left + ROSTER_X, top + BODY_TOP + 2, ROSTER_W);
        CompanionUi.section(graphics, font, Component.translatable(KEY + "address"), left + ROSTER_X, top + 206, ROSTER_W);
        CompanionUi.card(graphics, left + ROSTER_X, top + 218, ROSTER_W, 16, CompanionUi.SURFACE, CompanionUi.LINE);
        CompoundTag record = selectedRecord();
        hovered = null;
        for (var entry : commands.entrySet()) if (entry.getValue().visible && entry.getValue().isHovered() && ordinalOrder(entry.getKey()) >= 0) hovered = entry.getKey();
        if (record == null) { emptyState(graphics, left, top); return; }
        preview.render(graphics, snapshot.getStringOr("Model", "modern"), record.getStringOr("Skin", ""), scale, mouseX, mouseY);
        int x = left + DETAIL_X, y = top + BODY_TOP + 4;
        CompanionUi.label(graphics, font, Component.literal(record.getStringOr("Name", "")), x, y + 2, DETAIL_W, CompanionUi.TEXT);
        y += 15;
        int tier = record.getIntOr("Tier", 0);
        for (int i = 0; i < CompanionRules.MAX_TIER; i++) {
            CompanionUi.box(graphics, x + i * 9, y + 3, 7, 7, i < tier ? CompanionUi.MOON : CompanionUi.RAISED);
        }
        CompanionMode current = mode(record);
        CompanionUi.pill(graphics, font, Component.translatable(current.translationKey()), x + 34, y, modeColor(current));
        y += 18;
        float max = Math.max(1, record.getFloatOr("MaxHealth", 30));
        float hp = Math.clamp(record.getFloatOr("Health", 0), 0, max);
        float ratio = hp / max;
        CompanionUi.bar(graphics, x, y, DETAIL_W, 11, ratio, ratio > 0.5F ? CompanionUi.GOOD : ratio > 0.25F ? CompanionUi.WARN : CompanionUi.DANGER);
        graphics.centeredText(font, Component.translatable(KEY + "health", Math.round(hp), Math.round(max)), x + DETAIL_W / 2, y + 2, CompanionUi.TEXT);
        y += 14;
        int effective = Math.min(tier, snapshot.getIntOr("Skill", 0));
        Component stats = Component.translatable(KEY + "stats", (int) CompanionRules.attackDamage(effective),
                Math.round((1 - CompanionRules.damageMultiplier(effective)) * 100), String.valueOf(CompanionRules.regeneration(effective)));
        CompanionUi.label(graphics, font, stats, x, y, DETAIL_W, CompanionUi.MUTED);
        CompanionUi.section(graphics, font, Component.translatable(KEY + "section.orders"), x, top + 100, DETAIL_W);
        // Description follows the pointer, and otherwise explains the active order.
        Component text = statusLine(record, current);
        if (hovered != null) text = Component.translatable(KEY + "mode_desc." + modeFor(hovered).getSerializedName());
        CompanionUi.wrapped(graphics, font, text, x, top + 153, DETAIL_W, hovered != null ? CompanionUi.TEXT : CompanionUi.MUTED, 4);
        CompanionUi.section(graphics, font, Component.translatable(KEY + "section.actions"), x, top + 201, DETAIL_W);
    }
    private Component statusLine(CompoundTag record, CompanionMode current) {
        if (current == CompanionMode.HUNT) return Component.translatable(KEY + "hunt_remaining", days(record.getLongOr("HuntRemaining", 0)));
        if (!record.getBooleanOr("Enabled", false) && current != CompanionMode.DEAD) return Component.translatable(KEY + "suspended");
        long cooldown = record.getLongOr("HuntCooldownRemaining", 0);
        Component description = Component.translatable(KEY + "mode_desc." + current.getSerializedName());
        return cooldown > 0 ? description.copy().append("\n").append(Component.translatable(KEY + "hunt_cooldown", days(cooldown))) : description;
    }
    private static int modeColor(CompanionMode mode) {
        return switch (mode) {
            case FOLLOW -> 0xFF8FC7F0;
            case GUARD -> CompanionUi.DANGER;
            case REST -> CompanionUi.MUTED;
            case SORT -> CompanionUi.MOON;
            case HUNT -> CompanionUi.GOOD;
            case DEAD -> CompanionUi.FAINT;
        };
    }
    private void emptyState(GuiGraphicsExtractor graphics, int left, int top) {
        int x = left + STAGE_X, y = top + BODY_TOP + 4, w = STAGE_W + 8 + DETAIL_W;
        CompanionUi.card(graphics, x, y, w, 242, CompanionUi.SURFACE, CompanionUi.LINE);
        CompanionUi.disc(graphics, x + w / 2, y + 62, 26, 0xFFD9BC77);
        CompanionUi.disc(graphics, x + w / 2 + 10, y + 56, 23, CompanionUi.SURFACE);
        Component message = systemEnabled() ? Component.translatable(KEY + "empty") : Component.translatable("message.howlingwerewolf.companion.disabled");
        int lineY = y + 104;
        for (var line : font.split(message, w - 40)) { graphics.centeredText(font, line, x + w / 2, lineY, CompanionUi.TEXT); lineY += 12; }
        graphics.centeredText(font, Component.translatable(KEY + "empty_more"), x + w / 2, lineY + 8, CompanionUi.FAINT);
    }
    @Override protected void overlay(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY) {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        int capacity = snapshot.getIntOr("Capacity", 0);
        for (int i = 0; i < slots.size(); i++) {
            int x = left + ROSTER_X + 8, y = top + 48 + i * 52;
            if (i >= entries.size()) {
                boolean open = i < capacity;
                Component label = Component.translatable(KEY + (open ? "slot.empty" : "slot.locked"));
                graphics.centeredText(font, label, left + ROSTER_X + ROSTER_W / 2, y + 20, open ? CompanionUi.MUTED : CompanionUi.FAINT);
                continue;
            }
            CompoundTag entry = entries.getCompoundOrEmpty(i);
            boolean chosen = slots.get(i).selected();
            CompanionUi.label(graphics, font, Component.literal(entry.getStringOr("Name", "")), x, y + 7, ROSTER_W - 16, chosen ? CompanionUi.MOON : CompanionUi.TEXT);
            CompanionMode current = mode(entry);
            CompanionUi.label(graphics, font, Component.translatable(current.translationKey()), x, y + 19, 70, modeColor(current));
            int tier = entry.getIntOr("Tier", 0);
            for (int pip = 0; pip < CompanionRules.MAX_TIER; pip++) {
                CompanionUi.box(graphics, x + ROSTER_W - 16 - (CompanionRules.MAX_TIER - pip) * 7, y + 20, 5, 5, pip < tier ? CompanionUi.MOON : CompanionUi.RAISED);
            }
            float max = Math.max(1, entry.getFloatOr("MaxHealth", 30));
            float ratio = Math.clamp(entry.getFloatOr("Health", 0), 0, max) / max;
            CompanionUi.bar(graphics, x, y + 33, ROSTER_W - 16, 6, ratio, ratio > 0.5F ? CompanionUi.GOOD : ratio > 0.25F ? CompanionUi.WARN : CompanionUi.DANGER);
        }
    }
    @Override protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) {
        return selectedRecord() != null && preview.contains(mouseX, mouseY) ? Component.translatable(KEY + "preview_hint") : null;
    }
}
