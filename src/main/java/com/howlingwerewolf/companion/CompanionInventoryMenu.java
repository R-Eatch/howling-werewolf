/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/** Native nine-slot container synchronization, with server-side ownership and range validation. */
public final class CompanionInventoryMenu extends ChestMenu {
    public final CompanionRecord record;
    private final WerewolfCompanionEntity entity;

    public CompanionInventoryMenu(int id, Inventory playerInventory, CompanionRecord record, WerewolfCompanionEntity entity) {
        super(MenuType.GENERIC_9x1, id, playerInventory, record.inventory, 1);
        this.record = record;
        this.entity = entity;
    }
    @Override public boolean stillValid(Player player) {
        return com.howlingwerewolf.HWConfig.ENABLE_COMPANIONS.get() && record.belongsTo(player.getUUID()) && record.mode.hasEntity() && entity.isAlive()
                && !entity.isRemoved() && entity.generation() == record.generation
                && player.level() == entity.level() && player.distanceToSqr(entity) <= 64;
    }
}
