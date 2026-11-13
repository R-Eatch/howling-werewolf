/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.*;

/**
 * Server-owned work orders. The companion walks to storage around its anchor, opens it with the
 * vanilla lid animation, and only commits each transfer once it stands at the destination.
 * Items never leave a container until that transaction, so stopping at any moment loses nothing.
 */
public final class CompanionSorting {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MinecraftServer, State> STATES = new WeakHashMap<>();
    public static final int SCAN_LIMIT = 64;
    private static final int LID_TICKS = 14, SETTLE_TICKS = 6, WALK_LIMIT = 300, IDLE_SCAN = 200, RETRY = 40, NEXT_PASS = 20, UNREACHABLE_RETRY = 1200;
    private static final int REMARK_GAP = 6000, REMARK_RANGE = 32;
    private static final int LEASH = CompanionSortSettings.RADIUS + 8;
    private static final double REACH_HORIZONTAL = 2.6, REACH_VERTICAL = 3.2;

    /** Serialized for the UI; the lowercase name selects the status text. */
    public enum Phase { IDLE, WALKING, OPENING, MOVING, CLOSING }
    public record ScanEntry(BlockPos position, boolean usable, Set<Item> filter) {}
    private record Preview(BlockPos center, List<ScanEntry> entries) {}
    private record Lease(ResourceLocation dimension, BlockPos position) {}
    private record Endpoint(CompanionSortPlanner.Storage storage, List<BlockEntity> members, List<BlockState> layout) {
        boolean matches(@Nullable Endpoint current) {
            return current != null && members.equals(current.members) && layout.equals(current.layout)
                    && storage.handler().size() == current.storage.handler().size()
                    && storage.filter().equals(current.storage.filter());
        }
        BlockPos id() { return storage.id(); }
    }
    private static final class Trip {
        final Endpoint source, target;
        final ArrayDeque<CompanionSortPlanner.Move> moves = new ArrayDeque<>();
        Trip(Endpoint source, Endpoint target) { this.source = source; this.target = target; }
        boolean within() { return source == target; }
    }
    private static final class Task {
        List<Endpoint> endpoints = List.of();
        final ArrayDeque<Trip> trips = new ArrayDeque<>();
        @Nullable Trip trip;
        boolean atTarget;
        Phase phase = Phase.IDLE;
        int timer, walked;
        long nextScan;
        boolean reported, warnedUnreachable;
        // The opening order counts as the announcement of the first round.
        boolean announced = true;
        long nextRemark;
        int moved;
        List<ScanEntry> scan = List.of();
        final Set<BlockPos> unreachable = new HashSet<>();
        long retryUnreachable;
    }
    private static final class State {
        final Map<UUID, Task> tasks = new LinkedHashMap<>();
        final Map<UUID, Preview> previews = new HashMap<>();
        final Map<Lease, UUID> leases = new HashMap<>();
    }
    private static final class Budget { int moves = 16, scans = 2; }

    private static State state(MinecraftServer server) { return STATES.computeIfAbsent(server, ignored -> new State()); }
    public static void register() { ServerLifecycleEvents.SERVER_STOPPED.register(STATES::remove); }
    public static boolean enabled() { return HWConfig.ENABLE_COMPANIONS.get() && HWConfig.ENABLE_COMPANION_SORTING.get(); }

    public static void cancel(MinecraftServer server, UUID record) {
        State state = state(server);
        state.tasks.remove(record);
        state.leases.values().removeIf(record::equals);
        WerewolfCompanionEntity entity = CompanionSavedData.get(server).loaded(record);
        if (entity != null) entity.endSortVisuals();
    }

    public static void action(ServerPlayer player, CompanionRecord record, String operation) {
        if (!record.belongsTo(player.getUUID())) return;
        MinecraftServer server = player.level().getServer();
        CompanionSavedData data = CompanionSavedData.get(server);
        WerewolfCompanionEntity entity = data.loaded(record.id);
        boolean nearby = entity != null && entity.isAlive() && data.isCurrent(record, entity)
                && entity.level() == player.level() && entity.distanceToSqr(player) <= 16 * 16;
        // The sorting page refreshes this on its own, so an unavailable preview stays silent.
        if (operation.equals("preview")) {
            if (enabled() && nearby && record.mode != CompanionMode.SORT && CompanionManager.enabled(player, record)) {
                state(server).previews.put(record.id, new Preview(entity.blockPosition(), survey(player, entity, new CompanionSortSettings(
                        player.level().dimension().location(), entity.blockPosition(), List.of(), record.sorting.consolidate()), new ArrayList<>())));
            }
            return;
        }
        if (!enabled()) { CompanionManager.message(player, "sort_disabled"); return; }
        if (!record.mode.hasEntity() || !CompanionManager.enabled(player, record)) { CompanionManager.message(player, "unavailable"); return; }
        switch (operation) {
            case "toggle" -> {
                record.sorting = record.sorting.withConsolidate(!record.sorting.consolidate());
                // A running job replans with the new method from where it stands.
                if (record.mode == CompanionMode.SORT) cancel(server, record.id);
            }
            case "start" -> {
                if (record.mode == CompanionMode.SORT) return;
                if (!nearby) { CompanionManager.message(player, "sort_nearby"); return; }
                cancel(server, record.id);
                record.sorting = record.sorting.anchoredAt(player.level().dimension().location(), entity.blockPosition());
                record.mode = CompanionMode.SORT;
                CompanionDialogue.say(player, record, "sort");
                entity.setTarget(null); entity.setAggressive(false); entity.getNavigation().stop(); entity.resetIdleDialogue();
                entity.clearHostileTargets();
            }
            default -> { return; }
        }
        data.setDirty();
    }

    /** Ordinary interaction protection is consulted without opening or looting the container. */
    private static boolean mayAccess(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos)
                || level.getServer().isUnderSpawnProtection(level, pos, player)
                || !player.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)) return false;
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return UseBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, hit) != InteractionResult.FAIL;
    }

    /** Trapped chests and shulker boxes stay out on purpose: they are the player's untouched storage. */
    private static boolean supported(BlockState block) {
        return block.is(Blocks.CHEST) || block.is(Blocks.BARREL);
    }
    /** Every item framed on any face of these blocks; an empty frame asks for nothing. */
    private static Set<Item> framedItems(ServerLevel level, List<BlockPos> blocks) {
        Set<Item> items = new HashSet<>();
        for (BlockPos block : blocks) {
            for (ItemFrame frame : level.getEntitiesOfClass(ItemFrame.class, new AABB(block).inflate(0.2))) {
                if (!frame.getItem().isEmpty() && frame.getPos().relative(frame.getDirection().getOpposite()).equals(block)) items.add(frame.getItem().getItem());
            }
        }
        return Set.copyOf(items);
    }
    /** A player has this storage open: the companion waits rather than move items under them. */
    public static boolean inUse(ServerLevel level, BlockEntity be) {
        if (be instanceof ChestBlockEntity && ChestBlockEntity.getOpenCount(level, be.getBlockPos()) > 0) return true;
        for (ServerPlayer player : level.players()) {
            if (player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu menu) {
                net.minecraft.world.Container open = menu.getContainer();
                if (open == be || open instanceof net.minecraft.world.CompoundContainer compound && compound.contains((net.minecraft.world.Container) be)) return true;
            }
        }
        return false;
    }
    /** Barrels change their OPEN state as this worker opens them; that is not a layout change. */
    private static BlockState layoutOf(BlockState state) {
        return state.hasProperty(BarrelBlock.OPEN) ? state.setValue(BarrelBlock.OPEN, false) : state;
    }

    @Nullable private static Endpoint resolve(ServerPlayer player, @Nullable WerewolfCompanionEntity self, CompanionSortSettings settings, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!settings.contains(pos) || !level.hasChunkAt(pos)) return null;
        BlockState block = level.getBlockState(pos);
        if (!supported(block)) return null;
        List<BlockPos> positions = new ArrayList<>(); positions.add(pos);
        if (block.getBlock() instanceof ChestBlock && block.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = pos.relative(ChestBlock.getConnectedDirection(block));
            if (!settings.contains(other) || !level.hasChunkAt(other)) return null;
            BlockState otherState = level.getBlockState(other);
            if (!(otherState.getBlock() instanceof ChestBlock) || !supported(otherState) || otherState.getValue(ChestBlock.TYPE) == ChestType.SINGLE
                    || !other.relative(ChestBlock.getConnectedDirection(otherState)).equals(pos)) return null;
            positions.add(other);
        }
        positions.sort(BlockPos::compareTo);
        List<BlockEntity> members = new ArrayList<>();
        List<BlockState> layout = new ArrayList<>();
        for (BlockPos part : positions) {
            BlockEntity be = level.getBlockEntity(part);
            if (!(be instanceof RandomizableContainerBlockEntity container) || container.getLootTable() != null
                    || !container.canOpen(player) || !mayAccess(player, level, part)) return null;
            if (be instanceof ChestBlockEntity chest) {
                if (inUse(level, chest) || ChestBlock.isChestBlockedAt(level, part)) return null;
            } else if (be instanceof BarrelBlockEntity barrel) {
                if (inUse(level, barrel)) return null;
            } else return null;
            members.add(be);
            layout.add(layoutOf(level.getBlockState(part)));
        }
        BlockPos id = positions.getFirst().immutable();
        net.minecraft.world.Container container = members.getFirst() instanceof ChestBlockEntity && block.getBlock() instanceof ChestBlock chestBlock
                ? ChestBlock.getContainer(chestBlock, level.getBlockState(id), level, id, true) : (net.minecraft.world.Container) members.getFirst();
        if (container == null) return null;
        var handler = new SortTransfer.Handler(container);
        if (handler.size() < 1 || handler.size() > 54) return null;
        return new Endpoint(new CompanionSortPlanner.Storage(id, handler, framedItems(level, positions)), List.copyOf(members), List.copyOf(layout));
    }

    /** Nearest storage first; a double chest counts once. Unloaded chunks stay unloaded. */
    private static List<ScanEntry> survey(ServerPlayer player, @Nullable WerewolfCompanionEntity self, CompanionSortSettings settings, List<Endpoint> usable) {
        ServerLevel level = player.serverLevel();
        BlockPos center = settings.center();
        int radius = CompanionSortSettings.RADIUS;
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = (center.getX() - radius) >> 4; x <= (center.getX() + radius) >> 4; x++) {
            for (int z = (center.getZ() - radius) >> 4; z <= (center.getZ() + radius) >> 4; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                    if (settings.contains(pos) && supported(chunk.getBlockState(pos))) candidates.add(pos.immutable());
                }
            }
        }
        candidates.sort(Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(center)).thenComparing(pos -> pos));
        Set<BlockPos> seen = new HashSet<>();
        List<ScanEntry> result = new ArrayList<>();
        for (BlockPos pos : candidates) {
            if (result.size() >= SCAN_LIMIT) break;
            if (seen.contains(pos)) continue;
            BlockState block = level.getBlockState(pos);
            if (block.getBlock() instanceof ChestBlock && block.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                seen.add(pos.relative(ChestBlock.getConnectedDirection(block)));
            }
            seen.add(pos);
            Endpoint endpoint = resolve(player, self, settings, pos);
            if (endpoint != null) usable.add(endpoint);
            result.add(new ScanEntry(pos, endpoint != null, endpoint != null ? endpoint.storage.filter() : Set.of()));
        }
        return List.copyOf(result);
    }

    private static void release(State state, UUID id, Task task) {
        state.leases.values().removeIf(id::equals);
        task.endpoints = List.of(); task.trips.clear(); task.trip = null;
    }
    private static void abandon(State state, UUID id, Task task, WerewolfCompanionEntity entity, long retryAt) {
        entity.endSortVisuals();
        release(state, id, task);
        task.phase = Phase.IDLE; task.nextScan = retryAt;
    }

    public static void tick(MinecraftServer server) {
        State state = state(server);
        long now = server.overworld().getGameTime();
        CompanionSavedData data = CompanionSavedData.get(server);
        if (!enabled()) {
            state.tasks.clear(); state.leases.clear(); state.previews.clear();
            for (WerewolfCompanionEntity entity : data.activeEntities()) {
                CompanionRecord record = entity.record();
                if (record != null && record.mode == CompanionMode.SORT) { record.mode = CompanionMode.REST; data.setDirty(); }
            }
            return;
        }
        Set<UUID> active = new HashSet<>();
        Budget budget = new Budget();
        for (WerewolfCompanionEntity entity : data.activeEntities()) {
            CompanionRecord record = entity.record();
            ServerPlayer player = entity.owner();
            if (record == null || !entity.isAlive() || !data.isCurrent(record, entity)
                    || record.mode != CompanionMode.SORT || player == null || !player.isAlive() || player.isSpectator()
                    || !CompanionManager.enabled(player, record) || entity.level() != player.level()
                    || !record.sorting.dimension().equals(player.level().dimension().location())
                    || entity.blockPosition().distSqr(record.sorting.center()) > LEASH * LEASH) continue;
            active.add(record.id);
            Task task = state.tasks.computeIfAbsent(record.id, ignored -> new Task());
            try {
                advance(server, state, entity, record, player, task, now, budget);
            } catch (RuntimeException failure) {
                abandon(state, record.id, task, entity, now);
                record.mode = CompanionMode.REST; data.setDirty();
                CompanionManager.message(player, "sort_failed");
                LOGGER.warn("Stopped companion sorting {} after transfer failure", record.id, failure);
            }
        }
        state.tasks.entrySet().removeIf(entry -> {
            if (active.contains(entry.getKey())) return false;
            release(state, entry.getKey(), entry.getValue());
            WerewolfCompanionEntity entity = data.loaded(entry.getKey());
            if (entity != null) entity.endSortVisuals();
            return true;
        });
    }

    private static void advance(MinecraftServer server, State state, WerewolfCompanionEntity entity, CompanionRecord record,
                                ServerPlayer player, Task task, long now, Budget budget) {
        switch (task.phase) {
            case IDLE -> {
                entity.setSortBusy(false);
                if (now >= task.nextScan && budget.scans > 0) {
                    budget.scans--;
                    plan(state, entity, record, player, task, now);
                } else if (now % 20 == 0 && entity.getNavigation().isDone() && entity.blockPosition().distSqr(record.sorting.center()) > 4) {
                    BlockPos center = record.sorting.center();
                    entity.getNavigation().moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 1.0);
                }
            }
            case WALKING -> walk(state, entity, record, player, task, now);
            case OPENING -> {
                Endpoint here = destination(task);
                look(entity, here);
                if (--task.timer == LID_TICKS / 2) entity.swing(InteractionHand.MAIN_HAND);
                if (task.timer > 0) return;
                if (task.atTarget) { task.phase = Phase.MOVING; return; }
                // The stack shown in hand is cosmetic: it stays in the source until the deposit commits.
                entity.setCarriedItem(task.trip.moves.peek().resource().toStack(1));
                entity.closeSortContainers();
                task.atTarget = true; task.phase = Phase.WALKING; task.walked = 0;
                entity.getNavigation().stop();
            }
            case MOVING -> deposit(state, entity, record, player, task, now, budget);
            case CLOSING -> {
                look(entity, destination(task));
                if (--task.timer > 0) return;
                entity.closeSortContainers();
                nextTrip(state, entity, record, player, task, now);
            }
        }
    }

    private static void plan(State state, WerewolfCompanionEntity entity, CompanionRecord record, ServerPlayer player, Task task, long now) {
        release(state, record.id, task);
        if (now >= task.retryUnreachable) task.unreachable.clear();
        List<Endpoint> usable = new ArrayList<>();
        task.scan = survey(player, entity, record.sorting, usable);
        // Physical halves are leased too: splitting or joining a double chest cannot hand
        // the same slots to a second worker under a different logical identity.
        ResourceLocation dimension = record.sorting.dimension();
        List<Endpoint> endpoints = new ArrayList<>();
        for (Endpoint endpoint : usable) {
            boolean taken = task.unreachable.contains(endpoint.id()) || endpoint.members.stream()
                    .anyMatch(be -> state.leases.containsKey(new Lease(dimension, be.getBlockPos())));
            if (!taken) endpoints.add(endpoint);
        }
        task.endpoints = List.copyOf(endpoints);
        for (Endpoint endpoint : endpoints) for (BlockEntity member : endpoint.members)
            state.leases.put(new Lease(dimension, member.getBlockPos()), record.id);
        Map<CompanionSortPlanner.Storage, Endpoint> byStorage = new IdentityHashMap<>();
        for (Endpoint endpoint : endpoints) byStorage.put(endpoint.storage, endpoint);
        Map<List<BlockPos>, Trip> grouped = new LinkedHashMap<>();
        for (var move : CompanionSortPlanner.plan(endpoints.stream().map(Endpoint::storage).toList(), record.sorting.consolidate())) {
            Endpoint source = byStorage.get(move.source()), target = byStorage.get(move.target());
            grouped.computeIfAbsent(List.of(source.id(), target.id()), ignored -> new Trip(source, target)).moves.add(move);
        }
        if (grouped.isEmpty()) {
            release(state, record.id, task);
            if (!task.reported && task.moved > 0) {
                if (task.announced) CompanionDialogue.say(player, record, "sorted");
                task.reported = true; task.announced = false;
            }
            task.nextScan = now + IDLE_SCAN;
            return;
        }
        // Fresh work after a finished round. A hopper can feed this endlessly, so remarks are rationed.
        if (task.reported && now >= task.nextRemark && entity.distanceToSqr(player) <= REMARK_RANGE * REMARK_RANGE) {
            CompanionDialogue.say(player, record, "sort_idle_found");
            task.announced = true; task.nextRemark = now + REMARK_GAP;
        }
        task.trips.addAll(grouped.values());
        entity.setSortBusy(true);
        nextTrip(state, entity, record, player, task, now);
    }

    private static void nextTrip(State state, WerewolfCompanionEntity entity, CompanionRecord record, ServerPlayer player, Task task, long now) {
        entity.setCarriedItem(ItemStack.EMPTY);
        Trip nearest = null; double best = Double.MAX_VALUE;
        for (Trip trip : task.trips) {
            double distance = entity.blockPosition().distSqr(trip.source.id());
            if (distance < best) { best = distance; nearest = trip; }
        }
        if (nearest == null) {
            // Another pass settles anything that freed slots made possible, then reports.
            release(state, record.id, task);
            task.phase = Phase.IDLE; task.nextScan = now + NEXT_PASS;
            return;
        }
        task.trips.remove(nearest);
        task.trip = nearest;
        task.atTarget = nearest.within();
        task.phase = Phase.WALKING; task.walked = 0;
        entity.getNavigation().stop();
    }

    private static Endpoint destination(Task task) { return task.atTarget ? task.trip.target : task.trip.source; }
    private static BlockPos nearestMember(WerewolfCompanionEntity entity, Endpoint endpoint) {
        BlockPos nearest = endpoint.id();
        for (BlockEntity member : endpoint.members) {
            if (member.getBlockPos().distToCenterSqr(entity.position()) < nearest.distToCenterSqr(entity.position())) nearest = member.getBlockPos();
        }
        return nearest;
    }
    private static void look(WerewolfCompanionEntity entity, Endpoint endpoint) {
        BlockPos pos = nearestMember(entity, endpoint);
        entity.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 30);
    }
    private static boolean inReach(WerewolfCompanionEntity entity, BlockPos pos) {
        double x = entity.getX() - (pos.getX() + 0.5), z = entity.getZ() - (pos.getZ() + 0.5);
        return x * x + z * z <= REACH_HORIZONTAL * REACH_HORIZONTAL && Math.abs(entity.getY() - pos.getY()) <= REACH_VERTICAL;
    }

    private static void walk(State state, WerewolfCompanionEntity entity, CompanionRecord record, ServerPlayer player, Task task, long now) {
        Endpoint destination = destination(task);
        BlockPos pos = nearestMember(entity, destination);
        if (inReach(entity, pos)) { beginWork(state, entity, record, player, task, now, destination); return; }
        boolean stuck = ++task.walked > WALK_LIMIT;
        if (!stuck && entity.getNavigation().isDone()) {
            Path path = entity.getNavigation().createPath(pos, 2);
            if (path != null && path.canReach()) { entity.getNavigation().moveTo(path, 1.0); return; }
            stuck = true;
        }
        if (!stuck) return;
        // No way up to it on foot: within its long reach it works from where it stands.
        if (inLongReach(entity, pos)) beginWork(state, entity, record, player, task, now, destination);
        else skipUnreachable(state, entity, record, player, task, now, destination);
    }

    private static void beginWork(State state, WerewolfCompanionEntity entity, CompanionRecord record, ServerPlayer player, Task task, long now, Endpoint destination) {
        entity.getNavigation().stop();
        Endpoint current = resolve(player, entity, record.sorting, destination.id());
        if (!destination.matches(current)) { abandon(state, record.id, task, entity, now + RETRY); return; }
        entity.openSortContainers(current.members);
        task.phase = Phase.OPENING; task.timer = LID_TICKS;
    }

    /** The server-set distance a companion can reach into a container it cannot walk up to. */
    private static boolean inLongReach(WerewolfCompanionEntity entity, BlockPos pos) {
        double reach = HWConfig.COMPANION_SORTING_REACH.get();
        return entity.position().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= reach * reach;
    }

    /** Skips this container for the current pass and keeps working through the others. */
    private static void skipUnreachable(State state, WerewolfCompanionEntity entity, CompanionRecord record, ServerPlayer player, Task task, long now, Endpoint endpoint) {
        task.unreachable.add(endpoint.id());
        task.retryUnreachable = now + UNREACHABLE_RETRY;
        if (!task.warnedUnreachable) { CompanionDialogue.say(player, record, "sort_unreachable"); task.warnedUnreachable = true; }
        entity.getNavigation().stop();
        task.trips.removeIf(trip -> trip.source == endpoint || trip.target == endpoint);
        nextTrip(state, entity, record, player, task, now);
    }

    private static void deposit(State state, WerewolfCompanionEntity entity, CompanionRecord record, ServerPlayer player, Task task, long now, Budget budget) {
        Trip trip = task.trip;
        look(entity, destination(task));
        // Fresh work after a finished round starts a new tally.
        if (task.reported) { task.reported = false; task.moved = 0; }
        for (int i = 0; i < 4 && budget.moves > 0 && !trip.moves.isEmpty(); i++, budget.moves--) {
            var move = trip.moves.peek();
            Endpoint source = resolve(player, entity, record.sorting, trip.source.id());
            Endpoint target = trip.within() ? source : resolve(player, entity, record.sorting, trip.target.id());
            // A double chest returns a fresh combined handler per query; use the current one after checking layout.
            if (!trip.source.matches(source) || !trip.target.matches(target)
                    || !CompanionSortPlanner.execute(move.using(source.storage, target.storage))) {
                abandon(state, record.id, task, entity, now + NEXT_PASS);
                return;
            }
            trip.moves.remove();
            task.moved += move.amount();
        }
        if (!trip.moves.isEmpty()) return;
        entity.swing(InteractionHand.MAIN_HAND);
        entity.setCarriedItem(ItemStack.EMPTY);
        entity.level().playSound(null, entity, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.35F, 0.9F + entity.getRandom().nextFloat() * 0.2F);
        task.phase = Phase.CLOSING; task.timer = SETTLE_TICKS;
    }

    public static void writeView(CompoundTag tag, CompanionRecord record, MinecraftServer server) {
        State state = state(server);
        Task task = state.tasks.get(record.id);
        tag.putString("SortDimension", record.sorting.dimension().toString());
        Preview preview = task == null && record.mode != CompanionMode.SORT ? state.previews.get(record.id) : null;
        BlockPos center = preview != null ? preview.center() : record.sorting.center();
        tag.putIntArray("SortCenter", new int[]{center.getX(), center.getY(), center.getZ()});
        tag.putBoolean("SortConsolidate", record.sorting.consolidate());
        tag.putString("SortPhase", task == null ? record.mode == CompanionMode.SORT ? "paused" : "stopped"
                : task.phase == Phase.IDLE && task.moved > 0 && task.reported ? "done" : task.phase.name().toLowerCase(Locale.ROOT));
        tag.putInt("SortMoved", task == null ? 0 : task.moved);
        int remaining = 0;
        if (task != null) {
            for (Trip trip : task.trips) remaining += trip.moves.stream().mapToInt(CompanionSortPlanner.Move::amount).sum();
            if (task.trip != null) remaining += task.trip.moves.stream().mapToInt(CompanionSortPlanner.Move::amount).sum();
        }
        tag.putInt("SortRemaining", remaining);
        if (task != null && task.trip != null) {
            BlockPos at = destination(task).id();
            tag.putIntArray("SortAt", new int[]{at.getX(), at.getY(), at.getZ()});
        }
        List<ScanEntry> entries = task != null ? task.scan : preview != null ? preview.entries() : List.of();
        ListTag positions = new ListTag();
        for (ScanEntry scanned : entries) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("X", scanned.position().getX()); entry.putInt("Y", scanned.position().getY()); entry.putInt("Z", scanned.position().getZ());
            entry.putBoolean("Usable", scanned.usable() && (task == null || !task.unreachable.contains(scanned.position())));
            if (!scanned.filter().isEmpty()) {
                ListTag frames = new ListTag();
                scanned.filter().stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).sorted().forEach(id -> frames.add(StringTag.valueOf(id)));
                entry.put("Frames", frames);
            }
            positions.add(entry);
        }
        tag.put("SortContainers", positions);
    }
    private CompanionSorting() {}
}
