/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The small slice of an item-transfer API that sorting needs, over a vanilla {@link Container}.
 * A move is checked and then committed in one server tick, and a transaction puts every slot it
 * touched back unless it is committed, so a refused half never leaves items in between.
 */
public final class SortTransfer {
    /** An exact item: the item and all of its NBT. */
    public static final class ItemResource {
        public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);
        private final ItemStack prototype;
        private ItemResource(ItemStack stack) { prototype = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1); }
        public static ItemResource of(ItemStack stack) { return stack.isEmpty() ? EMPTY : new ItemResource(stack); }
        public boolean isEmpty() { return prototype.isEmpty(); }
        public Item getItem() { return prototype.getItem(); }
        public int getMaxStackSize() { return prototype.getMaxStackSize(); }
        public ItemStack toStack(int count) { return prototype.copyWithCount(count); }
        @Override public boolean equals(Object other) {
            return other instanceof ItemResource resource && ItemStack.isSameItemSameTags(prototype, resource.prototype)
                    && prototype.isEmpty() == resource.prototype.isEmpty();
        }
        @Override public int hashCode() {
            return prototype.isEmpty() ? 0 : 31 * Item.getId(prototype.getItem()) + (prototype.getTag() == null ? 0 : prototype.getTag().hashCode());
        }
    }

    /** One logical storage: a chest, a double chest seen as one, or a barrel. */
    public static final class Handler {
        private final Container container;
        public Handler(Container container) { this.container = container; }
        public int size() { return container.getContainerSize(); }
        public ItemResource getResource(int slot) { return ItemResource.of(container.getItem(slot)); }
        public int getAmountAsInt(int slot) { return container.getItem(slot).getCount(); }
        public boolean isValid(int slot, ItemResource resource) { return container.canPlaceItem(slot, resource.toStack(1)); }
        public int getCapacityAsInt(int slot, ItemResource resource) { return Math.min(container.getMaxStackSize(), resource.getMaxStackSize()); }
        public int extract(int slot, ItemResource resource, int amount, Transaction transaction) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || !resource.equals(ItemResource.of(stack))) return 0;
            int taken = Math.min(amount, stack.getCount());
            transaction.remember(container, slot);
            ItemStack rest = stack.copy();
            rest.shrink(taken);
            container.setItem(slot, rest.isEmpty() ? ItemStack.EMPTY : rest);
            return taken;
        }
        public int insert(int slot, ItemResource resource, int amount, Transaction transaction) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty() && !resource.equals(ItemResource.of(stack))) return 0;
            if (!isValid(slot, resource)) return 0;
            int room = getCapacityAsInt(slot, resource) - stack.getCount();
            int placed = Math.max(0, Math.min(room, amount));
            if (placed == 0) return 0;
            transaction.remember(container, slot);
            container.setItem(slot, resource.toStack(stack.getCount() + placed));
            return placed;
        }
    }

    /** Restores every slot it touched when closed without a commit. */
    public static final class Transaction implements AutoCloseable {
        private record Saved(Container container, int slot, ItemStack stack) {}
        private final List<Saved> journal = new ArrayList<>();
        private boolean committed;
        public static Transaction openRoot() { return new Transaction(); }
        void remember(Container container, int slot) {
            for (Saved saved : journal) if (saved.container == container && saved.slot == slot) return;
            journal.add(new Saved(container, slot, container.getItem(slot).copy()));
        }
        public void commit() {
            committed = true;
            List<Container> changed = new ArrayList<>();
            for (Saved saved : journal) if (!changed.contains(saved.container)) changed.add(saved.container);
            changed.forEach(Container::setChanged);
        }
        @Override public void close() {
            if (committed) return;
            for (int i = journal.size() - 1; i >= 0; i--) journal.get(i).container.setItem(journal.get(i).slot, journal.get(i).stack);
        }
    }

    private SortTransfer() {}
}
