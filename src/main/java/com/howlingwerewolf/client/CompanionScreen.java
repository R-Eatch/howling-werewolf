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
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** The pack page: roster on the left, the selected companion before the moon, its orders on the right. */
public final class CompanionScreen extends CompanionFrameScreen {
    private static final String KEY = "screen.howlingwerewolf.companion.";
    private static final int ROSTER_X = 12, ROSTER_W = 134, ROSTER_TOP = 34, ROSTER_STEP = 50, ORDER_W = 116, ORDER_TOP = 44, ORDER_STEP = 38;
    private static final int MOON_RADIUS = 80, RING_TICKS = 64, DEED_W = 78, DEED_H = 17;
    private static final CompanionActionPacket.Action[] ORDERS = {CompanionActionPacket.Action.FOLLOW, CompanionActionPacket.Action.GUARD,
            CompanionActionPacket.Action.REST, CompanionActionPacket.Action.SORT, CompanionActionPacket.Action.HUNT};
    private static final CompanionActionPacket.Action[] DEEDS = {CompanionActionPacket.Action.RECALL, CompanionActionPacket.Action.INVENTORY,
            CompanionActionPacket.Action.UPGRADE};
    private final Map<CompanionActionPacket.Action, UiButton> commands = new EnumMap<>(CompanionActionPacket.Action.class);
    private final List<UiButton> slots = new ArrayList<>();
    private EditBox address;
    private UiButton saveAddress, resetAddress, appearance, dismiss;
    private int actionDelay;

    public CompanionScreen(@Nullable Screen parent, CompoundTag snapshot) { this(parent, snapshot, null); }
    CompanionScreen(@Nullable Screen parent, CompoundTag snapshot, @Nullable UUID selected) {
        super(Component.translatable("screen.howlingwerewolf.companions"), parent, snapshot, selected);
        String target = snapshot.getStringOr("OpenTarget", "");
        if (!target.isEmpty()) try { this.selected = UUID.fromString(target); } catch (IllegalArgumentException ignored) {}
    }
    @Override protected Tab tab() { return Tab.PACK; }
    @Override protected int actionsWidth() { return DEED_W * 2 + 16; }

    @Override protected void onSnapshot(CompoundTag previous) {
        String before = previous.getStringOr("Address", "");
        if (address != null && address.getValue().equals(before)) address.setValue(snapshot.getStringOr("Address", ""));
    }

    private static int moonX(int left) { return left + (ROSTER_X + ROSTER_W + WIDTH - ORDER_W - 14) / 2; }
    private static int moonY(int top) { return top + TOP_BAND + 28 + MOON_RADIUS + 13; }

    @Override protected void build(int left, int top) {
        previewActive = true;
        commands.clear(); slots.clear();
        int canvasBottom = top + HEIGHT - LOWER_BAND;
        preview.stage(moonX(left), moonY(top), MOON_RADIUS, moonX(left) - MOON_RADIUS - 30, top + TOP_BAND, moonX(left) + MOON_RADIUS + 30, canvasBottom);
        for (int i = 0; i < CompanionRules.MAX_COMPANIONS; i++) {
            int index = i;
            UiButton card = new UiButton(left + ROSTER_X, top + ROSTER_TOP + i * ROSTER_STEP, ROSTER_W, 44, Component.empty(), Look.ROW, () -> choose(index))
                    .icon(() -> slotIcon(index)).hint(() -> slotHint(index));
            slots.add(addRenderableWidget(card));
        }
        String value = address == null ? snapshot.getStringOr("Address", "") : address.getValue();
        int addressY = top + 200;
        address = addRenderableWidget(new EditBox(font, left + ROSTER_X + 2, addressY + 13, ROSTER_W - 4, 12, Component.translatable(KEY + "address")));
        address.setBordered(false);
        address.setTextShadow(false);
        address.setMaxLength(CompanionRules.MAX_ADDRESS_LENGTH * 2);
        address.setHint(Component.translatable("dialogue.howlingwerewolf.companion.address_default"));
        address.setValue(value);
        int pill = (ROSTER_W - 4) / 2;
        saveAddress = addRenderableWidget(new UiButton(left + ROSTER_X, addressY + 29, pill, 16, Component.translatable(KEY + "save_address"), Look.SOFT, () -> requestAddress(address.getValue())));
        resetAddress = addRenderableWidget(new UiButton(left + ROSTER_X + pill + 4, addressY + 29, pill, 16, Component.translatable(KEY + "reset_address"), Look.SOFT, () -> { address.setValue(""); requestAddress(""); }));

        int orderX = left + WIDTH - ORDER_W - 10;
        List<Supplier<ItemLike>> orderIcons = List.of(() -> Items.LEAD, () -> Items.IRON_SWORD, () -> Items.RED_BED, () -> Items.CHEST, () -> Items.BOW);
        for (int i = 0; i < ORDERS.length; i++) {
            CompanionActionPacket.Action action = ORDERS[i];
            UiButton row = new UiButton(orderX, top + ORDER_TOP + i * ORDER_STEP, ORDER_W, 28, Component.translatable(KEY + action.name().toLowerCase(java.util.Locale.ROOT)), Look.ROW, () -> request(action))
                    .icon(orderIcons.get(i)).hint(() -> disabledReason(action));
            commands.put(action, addRenderableWidget(row));
        }
        // Set apart below the orders: dismissal is final, so it asks again on a page of its own.
        dismiss = addRenderableWidget(new UiButton(orderX, top + ORDER_TOP + ORDERS.length * ORDER_STEP + 2, ORDER_W, 16,
                Component.translatable(KEY + "dismiss"), Look.SOFT, this::askDismiss)
                .hint(() -> Component.translatable(KEY + "dismiss_hint")));
        int bandTop = canvasBottom + 5, right = left + WIDTH - 12;
        for (int i = 0; i < DEEDS.length; i++) {
            CompanionActionPacket.Action action = DEEDS[i];
            UiButton deed = new UiButton(right - (2 - i % 2) * DEED_W - (1 - i % 2) * 4, bandTop + i / 2 * (DEED_H + 3), DEED_W, DEED_H, deedLabel(action), Look.SOFT, () -> request(action))
                    .hint(() -> deedHint(action));
            commands.put(action, addRenderableWidget(deed));
        }
        appearance = addRenderableWidget(new UiButton(right - DEED_W, bandTop + DEED_H + 3, DEED_W, DEED_H, Component.translatable(KEY + "tab.look"), Look.SOFT, () -> showTab(Tab.LOOK))
                .hint(() -> Component.translatable(KEY + "customize")));
        // A fallen companion has one thing to do, so it takes the whole action area.
        commands.put(CompanionActionPacket.Action.REVIVE, addRenderableWidget(new UiButton(right - DEED_W * 2 - 4, bandTop + 4, DEED_W * 2 + 4, 26, deedLabel(CompanionActionPacket.Action.REVIVE), Look.PRIMARY,
                () -> request(CompanionActionPacket.Action.REVIVE)).hint(() -> deedHint(CompanionActionPacket.Action.REVIVE))));
    }
    private ItemLike slotIcon(int index) {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        if (index >= entries.size()) return index < snapshot.getIntOr("Capacity", 0) ? Items.BONE : Items.BARRIER;
        return modeIcon(mode(entries.getCompoundOrEmpty(index)));
    }
    private static ItemLike modeIcon(CompanionMode mode) {
        return switch (mode) {
            case FOLLOW -> Items.LEAD;
            case GUARD -> Items.IRON_SWORD;
            case REST -> Items.RED_BED;
            case SORT -> Items.CHEST;
            case HUNT -> Items.BOW;
            case DEAD -> Items.SKELETON_SKULL;
        };
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
    private void askDismiss() {
        CompoundTag record = selectedRecord();
        if (record == null || selected == null) return;
        minecraft.setScreen(new CompanionDismissScreen(this, selected, record.getStringOr("Name", ""),
                record.getIntOr("Tier", 0), mode(record)));
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
        address.setTextColor(MoonUi.theme().ink());
        saveAddress.active = system && actionDelay == 0 && !address.getValue().equals(snapshot.getStringOr("Address", ""));
        resetAddress.active = system && actionDelay == 0 && (!address.getValue().isEmpty() || !snapshot.getStringOr("Address", "").isEmpty());
        appearance.active = system && record != null && mode(record).hasEntity();
        appearance.visible = record != null && mode(record) != CompanionMode.DEAD;
        dismiss.visible = record != null;
        dismiss.active = system && record != null && actionDelay == 0;
        ListTag entries = snapshot.getListOrEmpty("Companions");
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
    /** How far through its hunt a companion is. */
    private static float huntProgress(CompoundTag record) {
        long total = record.getLongOr("HuntDuration", 0);
        return total <= 0 ? 0 : Mth.clamp(1 - (float) record.getLongOr("HuntRemaining", 0) / total, 0, 1);
    }

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

    @Override protected Focus focus() {
        CompoundTag record = selectedRecord();
        if (record == null) return new Focus(null, Component.translatable(KEY + "empty_more"));
        CompanionMode current = mode(record);
        return new Focus(Component.translatable(current.translationKey()), statusLine(record, current));
    }

    @Override protected void body(GuiGraphicsExtractor graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY) {
        int x = moonX(left), y = moonY(top);
        int fieldY = top + 200;
        CompanionUi.section(graphics, font, Component.translatable(KEY + "address"), left + ROSTER_X, fieldY, ROSTER_W, theme);
        graphics.fill(left + ROSTER_X, fieldY + 25, left + ROSTER_X + ROSTER_W, fieldY + 26, address != null && address.isFocused() ? theme.accent() : theme.line());
        CompoundTag record = selectedRecord();
        if (record == null) { emptyState(graphics, theme, x, y); return; }
        CompanionUi.section(graphics, font, Component.translatable(KEY + "section.orders"), left + WIDTH - ORDER_W - 10, top + 31, ORDER_W, theme);
        MoonUi.glow(graphics, x, y, MOON_RADIUS * 1.7F, theme.bloom());
        preview.render(graphics, snapshot.getStringOr("Model", "modern"), record.getStringOr("Skin", ""), scale, mouseX, mouseY, theme);
        CompanionMode current = mode(record);
        boolean hunting = current == CompanionMode.HUNT;
        float max = Math.max(1, record.getFloatOr("MaxHealth", 30));
        float hp = Math.clamp(record.getFloatOr("Health", 0), 0, max);
        float ratio = hunting ? huntProgress(record) : hp / max;
        int color = hunting ? MoonUi.tint(0xFF2F8F5B, 0xFF74D19E) : theme.accent();
        float ring = MOON_RADIUS + 6;
        int lit = Math.round(ratio * RING_TICKS);
        for (int i = 0; i < RING_TICKS; i++) {
            float angle = i * Mth.TWO_PI / RING_TICKS - Mth.HALF_PI, cos = Mth.cos(angle), sin = Mth.sin(angle);
            boolean on = i < lit;
            float outer = ring + (on ? 5 : 3);
            MoonUi.line(graphics, x + cos * ring, y + sin * ring, x + cos * outer, y + sin * outer, on ? 1.4F : 0.8F, on ? color : theme.line());
        }
        // Name and tier above, then the figures of this tier.
        Component name = Component.literal(record.getStringOr("Name", ""));
        int tier = record.getIntOr("Tier", 0);
        int nameRoom = 150, nameWidth = Math.min(nameRoom, font.width(name));
        int groupX = x - (nameWidth + 8 + CompanionRules.MAX_TIER * 9) / 2;
        CompanionUi.label(graphics, font, name, groupX, top + TOP_BAND + 5, nameRoom, theme.ink());
        for (int i = 0; i < CompanionRules.MAX_TIER; i++) {
            MoonUi.disc(graphics, groupX + nameWidth + 12 + i * 9, top + TOP_BAND + 9, 3, i < tier ? theme.accent() : theme.line());
        }
        int effective = Math.min(tier, snapshot.getIntOr("Skill", 0));
        Component stats = Component.translatable(KEY + "stats", (int) CompanionRules.attackDamage(effective),
                Math.round((1 - CompanionRules.damageMultiplier(effective)) * 100), String.valueOf(CompanionRules.regeneration(effective)));
        String statsText = CompanionUi.fit(font, stats.getString(), 200);
        graphics.text(font, statsText, x - font.width(statsText) / 2, top + TOP_BAND + 16, theme.sub(), false);
        // Below the moon: how far the hunt has come, or how much health is left.
        int footer = y + MOON_RADIUS + 15;
        if (hunting) {
            Component remaining = Component.translatable(KEY + "hunt_remaining", days(record.getLongOr("HuntRemaining", 0)));
            String text = CompanionUi.fit(font, remaining.getString(), 190);
            graphics.text(font, text, x - font.width(text) / 2, footer, theme.sub(), false);
            CompanionUi.meter(graphics, x - 70, footer + 11, 140, 4, ratio, color, theme);
        } else {
            MoonUi.centered(graphics, font, Component.translatable(KEY + "health", Math.round(hp), Math.round(max)), x, footer, theme.sub());
            long cooldown = record.getLongOr("HuntCooldownRemaining", 0);
            if (cooldown > 0) {
                String text = CompanionUi.fit(font, Component.translatable(KEY + "hunt_cooldown", days(cooldown)).getString(), 190);
                graphics.text(font, text, x - font.width(text) / 2, footer + 10, theme.faint(), false);
            }
        }
    }
    private Component statusLine(CompoundTag record, CompanionMode current) {
        if (!record.getBooleanOr("Enabled", false) && current != CompanionMode.DEAD && current != CompanionMode.HUNT) return Component.translatable(KEY + "suspended");
        return Component.translatable(KEY + "mode_desc." + current.getSerializedName());
    }
    private static int modeColor(CompanionMode mode, MoonUi.Theme theme) {
        return switch (mode) {
            case FOLLOW -> MoonUi.tint(0xFF2F76AE, 0xFF8FC7F0);
            case GUARD -> theme.accent();
            case REST -> theme.sub();
            case SORT -> MoonUi.tint(0xFF9C6F14, 0xFFEBC97F);
            case HUNT -> MoonUi.tint(0xFF2F8F5B, 0xFF74D19E);
            case DEAD -> theme.faint();
        };
    }
    private void emptyState(GuiGraphicsExtractor graphics, MoonUi.Theme theme, int x, int y) {
        MoonUi.glow(graphics, x, y - 44, 90, theme.bloom());
        MoonUi.moon(graphics, x, y - 44, 42, 6, theme);
        Component message = systemEnabled() ? Component.translatable(KEY + "empty") : Component.translatable("message.howlingwerewolf.companion.disabled");
        int lineY = y + 10;
        for (var line : font.split(message, 200)) { graphics.text(font, line, x - font.width(line) / 2, lineY, theme.ink(), false); lineY += 11; }
    }
    @Override protected void overlay(GuiGraphicsExtractor graphics, MoonUi.Theme theme, int left, int top, float mouseX, float mouseY) {
        ListTag entries = snapshot.getListOrEmpty("Companions");
        int textX = left + ROSTER_X + CompanionUi.ROW_RADIUS * 2 + 7, room = ROSTER_W - CompanionUi.ROW_RADIUS * 2 - 9;
        for (int i = 0; i < slots.size(); i++) {
            int y = top + ROSTER_TOP + i * ROSTER_STEP;
            if (i >= entries.size()) {
                boolean open = i < snapshot.getIntOr("Capacity", 0);
                graphics.text(font, Component.translatable(KEY + (open ? "slot.empty" : "slot.locked")), textX, y + 18, open ? theme.sub() : theme.faint(), false);
                continue;
            }
            CompoundTag entry = entries.getCompoundOrEmpty(i);
            boolean chosen = slots.get(i).selected();
            CompanionUi.label(graphics, font, Component.literal(entry.getStringOr("Name", "")), textX, y + 5, room, chosen ? theme.accent() : theme.ink());
            CompanionMode current = mode(entry);
            CompanionUi.label(graphics, font, Component.translatable(current.translationKey()), textX, y + 16, room - 26, modeColor(current, theme));
            int tier = entry.getIntOr("Tier", 0);
            for (int pip = 0; pip < CompanionRules.MAX_TIER; pip++) {
                MoonUi.disc(graphics, textX + room - (CompanionRules.MAX_TIER - pip) * 7 + 3, y + 20, 2.2F, pip < tier ? theme.accent() : theme.line());
            }
            float max = Math.max(1, entry.getFloatOr("MaxHealth", 30));
            float health = Math.clamp(entry.getFloatOr("Health", 0), 0, max) / max;
            boolean hunting = current == CompanionMode.HUNT;
            int color = hunting ? MoonUi.tint(0xFF2F8F5B, 0xFF74D19E) : health > 0.5F ? MoonUi.tint(0xFF2F8F5B, 0xFF74D19E) : health > 0.25F ? MoonUi.tint(0xFFB9791F, 0xFFE8AC62) : theme.accent();
            CompanionUi.meter(graphics, textX, y + 29, room, 3, hunting ? huntProgress(entry) : health, color, theme);
        }
        CompoundTag record = selectedRecord();
        if (record != null) {
            for (int i = 0; i < ORDERS.length; i++) {
                UiButton order = commands.get(ORDERS[i]);
                if (order != null && order.visible) graphics.text(font, String.valueOf(i + 1), order.getX() + order.getWidth() - 6, order.getY() + 10, theme.faint(), false);
            }
        }
    }
    @Override protected @Nullable Component areaHint(int left, int top, float mouseX, float mouseY) {
        return selectedRecord() != null && preview.contains(mouseX, mouseY) ? Component.translatable(KEY + "preview_hint") : null;
    }
}
