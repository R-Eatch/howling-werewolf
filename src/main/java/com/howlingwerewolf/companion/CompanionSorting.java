/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.*;

/** Server-owned work orders. Only explicitly registered vanilla storage is accessed. */
@EventBusSubscriber(modid = "howlingwerewolf")
public final class CompanionSorting {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MinecraftServer, State> STATES = new WeakHashMap<>();
    private static final ThreadLocal<Boolean> PROBING = ThreadLocal.withInitial(() -> false);
    private record Lease(Identifier dimension, BlockPos position) {}
    private record Registration(UUID record, long expires) {}
    private record Endpoint(CompanionSortPlanner.Storage storage, List<BlockEntity> members, List<BlockState> layout) {
        boolean matches(Endpoint current) {
            return current != null && members.equals(current.members) && layout.equals(current.layout)
                    && storage.handler().size() == current.storage.handler().size();
        }
    }
    private static final class Task {
        List<Endpoint> endpoints = List.of();
        ArrayDeque<CompanionSortPlanner.Move> moves = new ArrayDeque<>();
        long nextScan;
        boolean reported;
        int moved;
    }
    private static final class State {
        final Map<UUID, Registration> registrations = new HashMap<>();
        final Map<UUID, Task> tasks = new LinkedHashMap<>();
        final Map<Lease, UUID> leases = new HashMap<>();
    }
    private static State state(MinecraftServer server) { return STATES.computeIfAbsent(server, ignored -> new State()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { STATES.remove(event.getServer()); }
    public static boolean enabled() { return HWConfig.ENABLE_COMPANIONS.get() && HWConfig.ENABLE_COMPANION_SORTING.get(); }
    public static void endRegistration(ServerPlayer player) { state(player.level().getServer()).registrations.remove(player.getUUID()); }
    public static void cancel(MinecraftServer server, UUID record) {
        State state = state(server); state.tasks.remove(record);
        state.leases.values().removeIf(record::equals);
        state.registrations.values().removeIf(reg -> reg.record.equals(record));
    }
    public static void action(ServerPlayer player, CompanionRecord record, String operation) {
        if (!record.belongsTo(player.getUUID())) return;
        if (!enabled()) { CompanionManager.message(player, "sort_disabled"); return; }
        if (!record.mode.hasEntity() || !CompanionManager.enabled(player, record)) { CompanionManager.message(player, "unavailable"); return; }
        MinecraftServer server = player.level().getServer();
        CompanionSavedData data = CompanionSavedData.get(server);
        WerewolfCompanionEntity entity = data.loaded(record.id);
        if (entity == null || !entity.isAlive() || !data.isCurrent(record, entity)
                || entity.level() != player.level() || entity.distanceToSqr(player) > 16 * 16) {
            CompanionManager.message(player, "sort_nearby"); return;
        }
        CompanionMode previousMode = record.mode;
        switch (operation) {
            case "register" -> {
                cancel(server, record.id);
                record.mode = CompanionMode.REST;
                if (record.sorting.containers().isEmpty()) record.sorting = new CompanionSortSettings(
                        player.level().dimension().identifier(), entity.blockPosition(), List.of(), record.sorting.consolidate());
                if (!record.sorting.dimension().equals(player.level().dimension().identifier())) { CompanionManager.message(player, "sort_dimension"); break; }
                state(server).registrations.put(player.getUUID(), new Registration(record.id, server.overworld().getGameTime() + 6000));
                CompanionManager.message(player, "sort_register");
            }
            case "clear" -> {
                cancel(server, record.id); record.mode = CompanionMode.REST;
                record.sorting = new CompanionSortSettings(player.level().dimension().identifier(), entity.blockPosition(), List.of(), record.sorting.consolidate());
                CompanionManager.message(player, "sort_cleared");
            }
            case "toggle" -> {
                cancel(server, record.id); record.mode = CompanionMode.REST;
                record.sorting = new CompanionSortSettings(record.sorting.dimension(), record.sorting.center(), record.sorting.containers(), !record.sorting.consolidate());
            }
            case "start" -> {
                if (record.mode == CompanionMode.SORT) return;
                if (record.sorting.containers().isEmpty()) { CompanionManager.message(player, "sort_empty"); return; }
                if (!record.sorting.dimension().equals(player.level().dimension().identifier()) || !record.sorting.contains(entity.blockPosition())) {
                    CompanionManager.message(player, "sort_nearby"); return;
                }
                cancel(server, record.id); record.mode = CompanionMode.SORT;
                CompanionDialogue.say(player, record, "sort");
            }
            default -> { return; }
        }
        if (record.mode == CompanionMode.REST && previousMode != CompanionMode.REST) CompanionDialogue.say(player, record, "rest");
        entity.setTarget(null); entity.setAggressive(false); entity.getNavigation().stop(); entity.resetIdleDialogue();
        data.setDirty();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void register(PlayerInteractEvent.RightClickBlock event) {
        if (PROBING.get() || event.isCanceled() || event.getUseBlock() == TriState.FALSE
                || event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
                || !enabled() || !player.isShiftKeyDown()) return;
        MinecraftServer server = player.level().getServer();
        Registration registration = state(server).registrations.get(player.getUUID());
        if (registration == null) return;
        if (server.overworld().getGameTime() > registration.expires) { endRegistration(player); return; }
        CompanionRecord record = CompanionSavedData.get(server).find(registration.record);
        if (record == null || !record.belongsTo(player.getUUID()) || !CompanionManager.enabled(player, record)
                || record.mode != CompanionMode.REST || player.distanceToSqr(Vec3.atCenterOf(event.getPos())) > 64) { endRegistration(player); return; }
        event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        if (!record.sorting.dimension().equals(player.level().dimension().identifier())) { CompanionManager.message(player, "sort_dimension"); return; }
        Endpoint endpoint = resolve(player, record.sorting, event.getPos(), true);
        if (endpoint == null) { CompanionManager.message(player, "sort_invalid"); return; }
        List<BlockPos> positions = new ArrayList<>(record.sorting.containers());
        BlockPos position = endpoint.storage.id();
        if (positions.remove(position)) CompanionManager.message(player, "sort_removed", positions.size());
        else {
            if (positions.size() >= CompanionSortSettings.LIMIT) { CompanionManager.message(player, "sort_limit"); return; }
            positions.add(position); positions.sort(BlockPos::compareTo);
            CompanionManager.message(player, "sort_added", positions.size());
        }
        record.sorting = new CompanionSortSettings(record.sorting.dimension(), record.sorting.center(), positions, record.sorting.consolidate());
        CompanionSavedData.get(server).setDirty();
    }

    /** Ordinary interaction protection is consulted without opening or looting the container. */
    private static boolean mayAccess(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos)
                || level.getServer().isUnderSpawnProtection(level, pos, player)
                || !player.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)) return false;
        PROBING.set(true);
        try {
            var event = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND,
                    pos, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
            return !event.isCanceled() && event.getUseBlock() != TriState.FALSE;
        } finally { PROBING.remove(); }
    }

    private static Endpoint resolve(ServerPlayer player, CompanionSortSettings settings, BlockPos pos, boolean probe) {
        ServerLevel level = player.level();
        if (!settings.contains(pos) || !level.hasChunkAt(pos)) return null;
        var block = level.getBlockState(pos);
        if (!block.is(Blocks.CHEST) && !block.is(Blocks.TRAPPED_CHEST) && !block.is(Blocks.BARREL)) return null;
        List<BlockPos> positions = new ArrayList<>(); positions.add(pos);
        if (block.getBlock() instanceof ChestBlock && block.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = ChestBlock.getConnectedBlockPos(pos, block);
            if (!settings.contains(other) || !level.hasChunkAt(other)) return null;
            var otherState = level.getBlockState(other);
            if (!otherState.is(block.getBlock()) || otherState.getValue(ChestBlock.TYPE) == ChestType.SINGLE
                    || !ChestBlock.getConnectedBlockPos(other, otherState).equals(pos)) return null;
            positions.add(other);
        }
        positions.sort(BlockPos::compareTo);
        List<BlockEntity> members = new ArrayList<>();
        List<BlockState> layout = new ArrayList<>();
        for (BlockPos part : positions) {
            BlockEntity be = level.getBlockEntity(part);
            if (!(be instanceof RandomizableContainerBlockEntity container) || container.getLootTable() != null
                    || !container.canOpen(player) || probe && !mayAccess(player, level, part)) return null;
            if (be.getClass() == ChestBlockEntity.class) {
                if (!((ChestBlockEntity) be).getEntitiesWithContainerOpen().isEmpty() || ChestBlock.isChestBlockedAt(level, part)) return null;
            } else if (be.getClass() == BarrelBlockEntity.class) {
                if (!((BarrelBlockEntity) be).getEntitiesWithContainerOpen().isEmpty()) return null;
            } else return null;
            members.add(be);
            layout.add(level.getBlockState(part));
        }
        BlockPos id = positions.getFirst().immutable();
        var handler = level.getCapability(Capabilities.Item.BLOCK, id, null);
        if (handler == null || handler.size() < 1 || handler.size() > 54) return null;
        return new Endpoint(new CompanionSortPlanner.Storage(id, handler), List.copyOf(members), List.copyOf(layout));
    }

    private static void release(State state, UUID id, Task task) {
        state.leases.values().removeIf(id::equals); task.endpoints = List.of(); task.moves.clear();
    }
    public static void tick(MinecraftServer server) {
        State state = state(server);
        long now = server.overworld().getGameTime();
        if (!enabled()) {
            state.registrations.clear(); state.tasks.clear(); state.leases.clear();
            for (WerewolfCompanionEntity entity : CompanionSavedData.get(server).activeEntities()) {
                CompanionRecord record = entity.record();
                if (record != null && record.mode == CompanionMode.SORT) { record.mode = CompanionMode.REST; CompanionSavedData.get(server).setDirty(); }
            }
            return;
        }
        state.registrations.entrySet().removeIf(e -> e.getValue().expires < now || server.getPlayerList().getPlayer(e.getKey()) == null);
        Set<UUID> active = new HashSet<>();
        int budget = 16; // Total moves across this server tick; each worker additionally caps at four.
        int scans = 2; // Bound full snapshots/planning too, including idle work areas.
        for (WerewolfCompanionEntity entity : CompanionSavedData.get(server).activeEntities()) {
            CompanionRecord record = entity.record();
            ServerPlayer player = entity.owner();
            if (record == null || !entity.isAlive() || !CompanionSavedData.get(server).isCurrent(record, entity)
                    || record.mode != CompanionMode.SORT || player == null || !player.isAlive() || player.isSpectator()
                    || !CompanionManager.enabled(player, record) || entity.level() != player.level()
                    || !record.sorting.dimension().equals(player.level().dimension().identifier())
                    || !record.sorting.contains(entity.blockPosition())) continue;
            active.add(record.id);
            Task task = state.tasks.computeIfAbsent(record.id, ignored -> new Task());
            if (now < task.nextScan || budget <= 0) continue;
            try {
                if (task.moves.isEmpty()) {
                    if (scans == 0) continue;
                    scans--;
                    release(state, record.id, task);
                    LinkedHashMap<BlockPos, Endpoint> unique = new LinkedHashMap<>();
                    for (BlockPos pos : record.sorting.containers()) {
                        Endpoint endpoint = resolve(player, record.sorting, pos, true);
                        if (endpoint != null) unique.putIfAbsent(endpoint.storage.id(), endpoint);
                    }
                    List<Endpoint> endpoints = List.copyOf(unique.values());
                    // Lock physical halves too: splitting or joining a double chest must not
                    // create two independent logical leases for the same underlying slots.
                    boolean busy = endpoints.stream().flatMap(e -> e.members.stream())
                            .anyMatch(be -> state.leases.containsKey(new Lease(record.sorting.dimension(), be.getBlockPos())));
                    if (busy) { task.nextScan = now + 40; continue; }
                    task.endpoints = endpoints;
                    for (Endpoint endpoint : endpoints) for (BlockEntity member : endpoint.members)
                        state.leases.put(new Lease(record.sorting.dimension(), member.getBlockPos()), record.id);
                    task.moves.addAll(CompanionSortPlanner.plan(endpoints.stream().map(Endpoint::storage).toList(), record.sorting.consolidate()));
                    if (task.moves.isEmpty()) { release(state, record.id, task); task.nextScan = now + 100; continue; }
                }
                for (int i = 0; i < 4 && budget > 0 && !task.moves.isEmpty(); i++, budget--) {
                    CompanionSortPlanner.Move move = task.moves.peek();
                    CompanionSortPlanner.Storage source = null, target = null;
                    for (Endpoint old : task.endpoints) {
                        if (old.storage != move.source() && old.storage != move.target()) continue;
                        Endpoint current = resolve(player, record.sorting, old.storage.id(), true);
                        if (!old.matches(current)) break;
                        if (old.storage == move.source()) source = current.storage;
                        if (old.storage == move.target()) target = current.storage;
                    }
                    // A vanilla double chest legitimately returns a fresh CombinedResourceHandler
                    // on every query. Use that current capability after checking slot layout,
                    // rather than requiring object identity or retaining an outdated adapter.
                    if (source == null || target == null || !CompanionSortPlanner.execute(new CompanionSortPlanner.Move(
                            source, move.sourceSlot(), target, move.targetSlot(), move.resource(), move.amount(),
                            move.expectedSource(), move.expectedTarget()))) {
                        release(state, record.id, task); task.nextScan = now + 20; break;
                    }
                    task.moves.remove(); task.moved += move.amount();
                }
                if (task.moves.isEmpty()) {
                    release(state, record.id, task); task.nextScan = now + 100;
                    if (!task.reported && task.moved > 0) {
                        CompanionDialogue.say(player, record, "sorted"); task.reported = true;
                    }
                }
            } catch (RuntimeException failure) {
                release(state, record.id, task); record.mode = CompanionMode.REST; CompanionSavedData.get(server).setDirty();
                CompanionManager.message(player, "sort_failed"); LOGGER.warn("Stopped companion sorting {} after transfer failure", record.id, failure);
            }
        }
        state.tasks.entrySet().removeIf(entry -> {
            if (active.contains(entry.getKey())) return false;
            release(state, entry.getKey(), entry.getValue()); return true;
        });
    }

    public static void writeView(CompoundTag tag, CompanionRecord record) {
        tag.putString("SortDimension", record.sorting.dimension().toString());
        tag.putIntArray("SortCenter", new int[]{record.sorting.center().getX(), record.sorting.center().getY(), record.sorting.center().getZ()});
        tag.putBoolean("SortConsolidate", record.sorting.consolidate());
        ListTag positions = new ListTag();
        for (BlockPos pos : record.sorting.containers()) {
            CompoundTag entry = new CompoundTag(); entry.putInt("X", pos.getX()); entry.putInt("Y", pos.getY()); entry.putInt("Z", pos.getZ()); positions.add(entry);
        }
        tag.put("SortContainers", positions);
    }
    private CompanionSorting() {}
}
