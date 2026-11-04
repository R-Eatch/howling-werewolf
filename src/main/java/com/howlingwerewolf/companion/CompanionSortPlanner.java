/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.*;

/** Group exact resources once, compact into stable destinations, then transfer without clearing inventories. */
public final class CompanionSortPlanner {
    public record Storage(BlockPos id, ResourceHandler<ItemResource> handler) {}
    private record Cell(Storage storage, int slot, ItemResource resource, int amount) {}
    public record Move(Storage source, int sourceSlot, Storage target, int targetSlot,
                       ItemResource resource, int amount, int expectedSource, int expectedTarget) {}

    public static List<Move> plan(List<Storage> storages, boolean consolidate) {
        Map<ItemResource, List<Cell>> groups = new LinkedHashMap<>();
        Map<Storage, ArrayDeque<Cell>> empty = new LinkedHashMap<>();
        for (Storage storage : storages) {
            ArrayDeque<Cell> free = new ArrayDeque<>(); empty.put(storage, free);
            for (int i = 0; i < storage.handler.size(); i++) {
                ItemResource resource = storage.handler.getResource(i);
                int amount = storage.handler.getAmountAsInt(i);
                Cell cell = new Cell(storage, i, resource, amount);
                if (amount == 0 || resource.isEmpty()) free.add(cell);
                else if (resource.getMaxStackSize() > 1) groups.computeIfAbsent(resource, ignored -> new ArrayList<>()).add(cell);
            }
        }
        List<Move> result = new ArrayList<>();
        for (var group : groups.entrySet()) {
            ItemResource resource = group.getKey();
            List<Cell> cells = group.getValue();
            Map<Storage, Integer> totals = new HashMap<>();
            for (Cell cell : cells) totals.merge(cell.storage, cell.amount, Integer::sum);
            List<Storage> preferred = new ArrayList<>(totals.keySet());
            preferred.sort(Comparator.<Storage>comparingInt(s -> totals.get(s)).reversed()
                    .thenComparing(Storage::id));
            List<Cell> destinations = new ArrayList<>();
            Map<Cell, Integer> desired = new HashMap<>();
            int remaining = cells.stream().mapToInt(Cell::amount).sum();
            for (Storage storage : preferred) {
                List<Cell> occupied = cells.stream().filter(c -> c.storage.equals(storage))
                        .sorted(Comparator.comparingInt(Cell::amount).reversed().thenComparingInt(Cell::slot)).toList();
                for (Cell cell : occupied) {
                    int count = Math.min(remaining, capacity(cell, resource));
                    desired.put(cell, count); destinations.add(cell); remaining -= count;
                }
                if (consolidate) {
                    Iterator<Cell> iterator = empty.get(storage).iterator();
                    while (remaining > 0 && iterator.hasNext()) {
                        Cell cell = iterator.next(); int count = Math.min(remaining, capacity(cell, resource));
                        if (count == 0) continue;
                        iterator.remove(); cells.add(cell); destinations.add(cell);
                        desired.put(cell, count); remaining -= count;
                    }
                }
            }
            // Valid vanilla slots always hold their present amount. Fail closed if an adapter violates that.
            if (remaining != 0) continue;
            ArrayDeque<Cell> donors = new ArrayDeque<>();
            Map<Cell, Integer> amounts = new HashMap<>();
            for (Cell cell : cells) {
                amounts.put(cell, cell.amount);
                if (cell.amount > desired.getOrDefault(cell, 0)) donors.add(cell);
            }
            for (Cell target : destinations) {
                int wanted = desired.getOrDefault(target, 0);
                while (amounts.get(target) < wanted && !donors.isEmpty()) {
                    Cell source = donors.peek();
                    int beforeSource = amounts.get(source), beforeTarget = amounts.get(target);
                    int count = Math.min(wanted - beforeTarget, beforeSource - desired.getOrDefault(source, 0));
                    result.add(new Move(source.storage, source.slot, target.storage, target.slot,
                            resource, count, beforeSource, beforeTarget));
                    amounts.put(source, beforeSource - count); amounts.put(target, beforeTarget + count);
                    if (amounts.get(source) == desired.getOrDefault(source, 0)) donors.remove();
                }
            }
        }
        return List.copyOf(result);
    }

    private static int capacity(Cell cell, ItemResource resource) {
        if (!cell.storage.handler.isValid(cell.slot, resource)) return cell.amount;
        return Math.min(resource.getMaxStackSize(), cell.storage.handler.getCapacityAsInt(cell.slot, resource));
    }

    /** A stale snapshot or a failed insertion aborts both halves of this individual move. */
    public static boolean execute(Move move) {
        ResourceHandler<ItemResource> source = move.source.handler, target = move.target.handler;
        if (source.getAmountAsInt(move.sourceSlot) != move.expectedSource
                || target.getAmountAsInt(move.targetSlot) != move.expectedTarget
                || !source.getResource(move.sourceSlot).equals(move.resource)
                || move.expectedTarget > 0 && !target.getResource(move.targetSlot).equals(move.resource)) return false;
        try (Transaction transaction = Transaction.openRoot()) {
            if (source.extract(move.sourceSlot, move.resource, move.amount, transaction) != move.amount) return false;
            if (target.insert(move.targetSlot, move.resource, move.amount, transaction) != move.amount) return false;
            transaction.commit(); return true;
        }
    }
    private CompanionSortPlanner() {}
}
