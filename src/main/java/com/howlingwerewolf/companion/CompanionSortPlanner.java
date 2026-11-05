/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.*;

/**
 * Group exact resources once, compact into stable destinations, then transfer without clearing
 * inventories. A framed storage holds only its framed item types: those types gather there from
 * everywhere, and anything else inside moves out as soon as there is room for it.
 */
public final class CompanionSortPlanner {
    /** An empty {@code filter} takes anything. */
    public record Storage(BlockPos id, ResourceHandler<ItemResource> handler, Set<Item> filter) {
        public Storage(BlockPos id, ResourceHandler<ItemResource> handler) { this(id, handler, Set.of()); }
        boolean framed() { return !filter.isEmpty(); }
    }
    private record Cell(Storage storage, int slot, ItemResource resource, int amount) {}
    public record Move(Storage source, int sourceSlot, Storage target, int targetSlot, ItemResource resource, int amount) {
        public Move using(Storage currentSource, Storage currentTarget) {
            return new Move(currentSource, sourceSlot, currentTarget, targetSlot, resource, amount);
        }
    }

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
                else groups.computeIfAbsent(resource, ignored -> new ArrayList<>()).add(cell);
            }
        }
        List<Move> result = new ArrayList<>();
        for (var group : groups.entrySet()) {
            ItemResource resource = group.getKey();
            List<Cell> cells = group.getValue();
            List<Storage> framedHomes = storages.stream().filter(s -> s.filter.contains(resource.getItem())).toList();
            List<Storage> homes = framedHomes.isEmpty() ? storages.stream().filter(s -> !s.framed()).toList() : framedHomes;
            if (homes.isEmpty()) continue;
            List<Cell> placed = cells.stream().filter(c -> homes.contains(c.storage)).toList();
            List<Cell> misplaced = cells.stream().filter(c -> !homes.contains(c.storage)).toList();
            // Single items have no stacks to merge, so they only ever move to answer a frame.
            if (misplaced.isEmpty() && resource.getMaxStackSize() == 1) continue;
            Map<Storage, Integer> totals = new HashMap<>();
            for (Cell cell : placed) totals.merge(cell.storage, cell.amount, Integer::sum);
            List<Storage> preferred = new ArrayList<>(totals.keySet());
            preferred.sort(Comparator.<Storage>comparingInt(s -> totals.get(s)).reversed()
                    .thenComparing(Storage::id));
            List<Cell> destinations = new ArrayList<>();
            Map<Cell, Integer> desired = new HashMap<>();
            int remaining = placed.stream().mapToInt(Cell::amount).sum();
            for (Storage storage : preferred) {
                List<Cell> occupied = placed.stream().filter(c -> c.storage.equals(storage))
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
            int homeless = misplaced.stream().mapToInt(Cell::amount).sum();
            if (homeless > 0) {
                List<Storage> order = new ArrayList<>(preferred);
                homes.stream().filter(home -> !totals.containsKey(home)).sorted(Comparator.comparing(Storage::id)).forEach(order::add);
                for (Storage home : order) homeless = settle(home, resource, homeless, cells, destinations, desired, empty);
                if (homeless > 0 && !framedHomes.isEmpty()) {
                    // The frames are full. What already waits outside them stays together, and a
                    // framed storage still sheds what its frame refuses into plain storage.
                    for (Cell cell : misplaced) {
                        if (cell.storage.framed()) continue;
                        int count = Math.min(homeless, capacity(cell, resource));
                        desired.put(cell, count); destinations.add(cell); homeless -= count;
                    }
                    List<Storage> plain = storages.stream().filter(s -> !s.framed()).sorted(Comparator.comparing(Storage::id)).toList();
                    for (Storage storage : plain) homeless = fill(storage, resource, homeless, cells, destinations, desired, empty);
                }
                // Anything still without room waits where it is for a later pass.
                for (Cell cell : misplaced) {
                    if (homeless == 0 || desired.containsKey(cell)) continue;
                    int keep = Math.min(homeless, cell.amount);
                    desired.put(cell, keep); homeless -= keep;
                }
            }
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
                    result.add(new Move(source.storage, source.slot, target.storage, target.slot, resource, count));
                    amounts.put(source, beforeSource - count); amounts.put(target, beforeTarget + count);
                    if (amounts.get(source) == desired.getOrDefault(source, 0)) donors.remove();
                }
            }
        }
        return List.copyOf(result);
    }

    /** Tops up the destinations already chosen in this storage, then opens its free slots. */
    private static int settle(Storage storage, ItemResource resource, int amount, List<Cell> cells, List<Cell> destinations,
                              Map<Cell, Integer> desired, Map<Storage, ArrayDeque<Cell>> empty) {
        for (Cell cell : destinations) {
            if (amount == 0) break;
            if (!cell.storage.equals(storage)) continue;
            int extra = Math.min(amount, capacity(cell, resource) - desired.get(cell));
            if (extra > 0) { desired.merge(cell, extra, Integer::sum); amount -= extra; }
        }
        return fill(storage, resource, amount, cells, destinations, desired, empty);
    }

    private static int fill(Storage storage, ItemResource resource, int amount, List<Cell> cells, List<Cell> destinations,
                            Map<Cell, Integer> desired, Map<Storage, ArrayDeque<Cell>> empty) {
        Iterator<Cell> free = empty.get(storage).iterator();
        while (amount > 0 && free.hasNext()) {
            Cell cell = free.next(); int count = Math.min(amount, capacity(cell, resource));
            if (count == 0) continue;
            free.remove(); cells.add(cell); destinations.add(cell);
            desired.put(cell, count); amount -= count;
        }
        return amount;
    }

    private static int capacity(Cell cell, ItemResource resource) {
        if (!cell.storage.handler.isValid(cell.slot, resource)) return cell.amount;
        return Math.min(resource.getMaxStackSize(), cell.storage.handler.getCapacityAsInt(cell.slot, resource));
    }

    /**
     * Donor and receiver cells never overlap, so moves stay valid in any order; the worker walks
     * them nearest-first. A changed slot or a failed insertion aborts both halves of this move.
     */
    public static boolean execute(Move move) {
        ResourceHandler<ItemResource> source = move.source.handler, target = move.target.handler;
        if (source.getAmountAsInt(move.sourceSlot) < move.amount
                || !source.getResource(move.sourceSlot).equals(move.resource)
                || target.getAmountAsInt(move.targetSlot) > 0 && !target.getResource(move.targetSlot).equals(move.resource)) return false;
        try (Transaction transaction = Transaction.openRoot()) {
            if (source.extract(move.sourceSlot, move.resource, move.amount, transaction) != move.amount) return false;
            if (target.insert(move.targetSlot, move.resource, move.amount, transaction) != move.amount) return false;
            transaction.commit(); return true;
        }
    }
    private CompanionSortPlanner() {}
}
