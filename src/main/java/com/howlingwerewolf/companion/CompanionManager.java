/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.network.CompanionActionPacket;
import com.howlingwerewolf.network.CompanionViewPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class CompanionManager {
    public static final ResourceLocation HUNT_LOOT = new ResourceLocation("howlingwerewolf", "gameplay/companion_hunt");

    public static int skillRank(ServerPlayer player) {
        return WerewolfApi.get(player).map(data -> data.isWerewolf() ? data.getTreeSkillRank(WerewolfTreeSkill.PACK_OATH) : 0).orElse(0);
    }
    public static int capacity(ServerPlayer player) {
        if (!HWConfig.ENABLE_COMPANIONS.get()) return 0;
        return CompanionRules.capacity(skillRank(player), WerewolfApi.get(player).map(data -> data.hasDefeatedAlpha()).orElse(false));
    }
    public static boolean enabled(ServerPlayer player, CompanionRecord record) {
        int index = CompanionSavedData.get(player.getServer()).ownedBy(player.getUUID()).indexOf(record);
        return index >= 0 && index < capacity(player);
    }
    public static int effectiveTier(ServerPlayer player, CompanionRecord record) { return Math.min(record.tier, skillRank(player)); }

    public static void handle(ServerPlayer player, CompanionActionPacket packet) {
        if (!player.isAlive() || player.isSpectator()) return;
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        long now = player.getServer().overworld().getGameTime();
        data.synchronizeAvailability(HWConfig.ENABLE_COMPANIONS.get(), now);
        if (!HWConfig.ENABLE_COMPANIONS.get() && packet.action() != CompanionActionPacket.Action.OPEN
                && packet.action() != CompanionActionPacket.Action.REFRESH) { message(player, "disabled"); return; }
        var requests = packet.action() == CompanionActionPacket.Action.OPEN || packet.action() == CompanionActionPacket.Action.REFRESH
                ? data.lastRequests : data.lastActions;
        Long previous = requests.get(player.getUUID());
        if (previous != null && now - previous < 2) return;
        requests.put(player.getUUID(), now);
        switch (packet.action()) {
            case OPEN -> {
                if (packet.target().equals(CompanionActionPacket.NO_TARGET)) sendView(player, true);
                else {
                    CompanionRecord record = data.find(packet.target());
                    if (record != null && record.belongsTo(player.getUUID())) sendView(player, true, record.id);
                }
            }
            case REFRESH -> sendView(player, false);
            case ADDRESS -> {
                if (!data.setAddress(player.getUUID(), packet.value().strip())) message(player, "invalid_address");
                sendView(player, false);
            }
            case RECRUIT -> recruit(player, packet.target());
            default -> {
                CompanionRecord record = data.find(packet.target());
                if (record == null || !record.belongsTo(player.getUUID())) return;
                switch (packet.action()) {
                    case FOLLOW -> command(player, record, CompanionMode.FOLLOW);
                    case GUARD -> command(player, record, CompanionMode.GUARD);
                    case REST -> command(player, record, CompanionMode.REST);
                    case HUNT -> startHunt(player, record);
                    case RECALL -> recall(player, record);
                    case UPGRADE -> upgrade(player, record);
                    case REVIVE -> revive(player, record);
                    case INVENTORY -> openInventory(player, record);
                    case RENAME -> rename(player, record, packet.value());
                    case SKIN -> changeSkin(player, record, packet.value());
                    case SORT -> CompanionSorting.action(player, record, packet.value());
                    case DISMISS -> dismiss(player, record);
                    default -> {}
                }
                sendView(player, false);
            }
        }
    }

    public static void offerRecruitment(ServerPlayer player, WerewolfCompanionEntity npc) {
        if (!HWConfig.ENABLE_COMPANIONS.get()) { message(player, "disabled"); return; }
        if (!npc.canOfferRecruitment(player)) return;
        npc.offerTo(player);
        CompoundTag tag = new CompoundTag();
        tag.putString("View", "recruit");
        tag.putString("Npc", npc.getUUID().toString());
        tag.putString("Name", npc.getName().getString());
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CompanionViewPacket(tag, true));
    }

    private static void recruit(ServerPlayer player, UUID npcId) {
        Entity target = player.serverLevel().getEntity(npcId);
        if (!(target instanceof WerewolfCompanionEntity npc) || !npc.acceptsOffer(player)) return;
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        if (skillRank(player) == 0) { message(player, "skill_required"); return; }
        if (data.ownedBy(player.getUUID()).size() >= capacity(player)) { message(player, "full"); return; }
        if (!hasItems(player, ModItems.ALPHA_WEREWOLF_BADGE.get(), 1)) { message(player, "badge_required"); return; }
        CompanionRecord record = new CompanionRecord(player.getUUID(), npc.getNpcSkinId(),
                player.level().dimension().location(), npc.blockPosition());
        record.health = Math.min(npc.getHealth(), (float) CompanionRules.maxHealth(0));
        closeInventory(player, record);
        data.add(record);
        npc.bind(record);
        if (!data.claim(record, npc)) throw new IllegalStateException("A new companion already has a projection");
        npc.clearHostileTargets();
        consume(player, ModItems.ALPHA_WEREWOLF_BADGE.get(), 1);
        data.setDirty();
        npc.sayTo(player, "recruited");
        sendView(player, true);
    }

    private static void command(ServerPlayer player, CompanionRecord record, CompanionMode mode) {
        if (record.mode == mode) return;
        if (!record.mode.hasEntity()) { message(player, "unavailable"); return; }
        if (mode != CompanionMode.REST && !enabled(player, record)) { message(player, "skill_required"); return; }
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        WerewolfCompanionEntity entity = data.loaded(record.id);
        if (entity == null && mode != CompanionMode.REST) entity = spawnNear(player, record, record.health);
        if (entity == null && mode != CompanionMode.REST) { message(player, "no_space"); return; }
        record.mode = mode;
        if (entity != null) {
            entity.setTarget(null); entity.setAggressive(false); entity.getNavigation().stop(); entity.resetIdleDialogue();
            entity.clearHostileTargets();
        }
        say(player, record, mode.getSerializedName());
        data.setDirty();
    }

    private static void startHunt(ServerPlayer player, CompanionRecord record) {
        if (!record.mode.hasEntity() || !enabled(player, record)) { message(player, "unavailable"); return; }
        long now = player.getServer().overworld().getGameTime();
        if (record.huntCooldownEnd > now) { message(player, "hunt_cooldown", (record.huntCooldownEnd - now + 1199) / 1200); return; }
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        WerewolfCompanionEntity entity = data.loaded(record.id);
        if (entity != null) remember(entity);
        if (record.health <= 0) return;
        closeInventory(player, record);
        record.huntStart = player.getServer().overworld().getGameTime();
        record.huntEnd = record.huntStart + CompanionRules.MIN_HUNT_TICKS
                + player.getRandom().nextInt(CompanionRules.MAX_HUNT_TICKS - CompanionRules.MIN_HUNT_TICKS + 1);
        record.mode = CompanionMode.HUNT;
        say(player, record, "hunting");
        if (entity != null) entity.discard();
        data.setDirty();
    }

    private static void recall(ServerPlayer player, CompanionRecord record) {
        if (record.mode == CompanionMode.DEAD) { message(player, "dead"); return; }
        if (record.mode == CompanionMode.HUNT) { finishHunt(player, record, true); return; }
        if (spawnNear(player, record, record.health) == null) { message(player, "no_space"); return; }
        record.mode = enabled(player, record) ? CompanionMode.FOLLOW : CompanionMode.REST;
        CompanionSavedData.get(player.getServer()).setDirty();
        say(player, record, "recalled");
    }

    private static void finishHunt(ServerPlayer player, CompanionRecord record, boolean notifyFailure) {
        if (record.mode != CompanionMode.HUNT) return;
        long now = player.getServer().overworld().getGameTime();
        long elapsed = CompanionRules.huntElapsed(now, record.huntStart, record.huntEnd);
        long cooldownEnd = CompanionRules.huntCooldownEnd(now, record.huntEnd);
        int rolls = CompanionRules.huntRolls(elapsed, effectiveTier(player, record));
        WerewolfCompanionEntity entity = spawnNear(player, record, record.health);
        if (entity == null) { if (notifyFailure) message(player, "no_space"); return; }
        // Commit the state before drawing loot: another recall/refresh cannot settle this hunt twice.
        record.mode = enabled(player, record) ? CompanionMode.FOLLOW : CompanionMode.REST;
        record.huntStart = record.huntEnd = 0;
        record.huntCooldownEnd = cooldownEnd;
        CompanionSavedData.get(player.getServer()).setDirty();
        LootTable table = player.getServer().getLootData().getLootTable(HUNT_LOOT);
        LootParams params = new LootParams.Builder(player.serverLevel())
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity).create(LootContextParamSets.CHEST);
        for (int roll = 0; roll < rolls; roll++) {
            table.getRandomItems(params, stack -> {
                ItemStack overflow = record.inventory.addItem(stack.copy());
                if (!overflow.isEmpty()) entity.spawnAtLocation(overflow);
            });
        }
        say(player, record, "returned");
        player.sendSystemMessage(Component.translatable("message.howlingwerewolf.companion.hunt_result", record.name(), elapsed / 1200, rolls));
    }

    private static void upgrade(ServerPlayer player, CompanionRecord record) {
        if (!record.mode.hasEntity() || !enabled(player, record) || record.tier >= skillRank(player)
                || record.tier >= CompanionRules.MAX_TIER) { message(player, "upgrade_locked"); return; }
        int cost = CompanionRules.upgradeCost(record.tier);
        if (!hasItems(player, ModItems.MOONBANE_PEARL.get(), cost)) { message(player, "pearls_required", cost); return; }
        consume(player, ModItems.MOONBANE_PEARL.get(), cost);
        record.tier++;
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        WerewolfCompanionEntity entity = data.loaded(record.id);
        if (entity != null) entity.refreshStats();
        data.setDirty();
        message(player, "upgraded", record.name(), record.tier);
        say(player, record, "upgrade");
    }

    private static void revive(ServerPlayer player, CompanionRecord record) {
        if (record.mode != CompanionMode.DEAD) return;
        if (!hasItems(player, ModItems.ALPHA_WEREWOLF_BADGE.get(), 1)) { message(player, "badge_required"); return; }
        float health = (float) CompanionRules.maxHealth(effectiveTier(player, record));
        WerewolfCompanionEntity entity = spawnNear(player, record, health);
        if (entity == null) { message(player, "no_space"); return; }
        consume(player, ModItems.ALPHA_WEREWOLF_BADGE.get(), 1);
        record.health = health;
        record.mode = enabled(player, record) ? CompanionMode.FOLLOW : CompanionMode.REST;
        CompanionSavedData.get(player.getServer()).setDirty();
        entity.sayTo(player, "revived");
    }

    /**
     * Releases a companion for good: the record, tier and coat are gone, and nothing spent on it
     * is refunded. Whatever it carried goes back to the owner, so a dismissal never destroys items.
     */
    private static void dismiss(ServerPlayer player, CompanionRecord record) {
        MinecraftServer server = player.getServer();
        CompanionSavedData data = CompanionSavedData.get(server);
        CompanionSorting.cancel(server, record.id);
        closeInventory(player, record);
        String name = record.name();
        if (record.mode.hasEntity()) say(player, record, "dismissed");
        WerewolfCompanionEntity entity = data.loaded(record.id);
        if (entity != null) entity.discard();
        data.remove(record);
        for (int slot = 0; slot < record.inventory.getContainerSize(); slot++) {
            ItemStack stack = record.inventory.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack);
        }
        message(player, "dismissed", name);
    }

    public static void openControls(ServerPlayer player, WerewolfCompanionEntity entity) {
        if (!HWConfig.ENABLE_COMPANIONS.get()) { message(player, "disabled"); return; }
        CompanionRecord record = entity.record();
        if (record == null || !record.belongsTo(player.getUUID()) || !record.mode.hasEntity()
                || !entity.isAlive() || entity.level() != player.level()
                || !player.canReach(entity, 1)
                || !CompanionSavedData.get(player.getServer()).isCurrent(record, entity)) return;
        handle(player, new CompanionActionPacket(CompanionActionPacket.Action.OPEN, record.id));
    }

    public static void openInventory(ServerPlayer player, CompanionRecord record) {
        if (!HWConfig.ENABLE_COMPANIONS.get()) { message(player, "disabled"); return; }
        if (!record.belongsTo(player.getUUID()) || !record.mode.hasEntity()) { message(player, "unavailable"); return; }
        WerewolfCompanionEntity entity = CompanionSavedData.get(player.getServer()).loaded(record.id);
        if (entity == null || !entity.isAlive() || entity.level() != player.level() || entity.distanceToSqr(player) > 64) {
            message(player, "inventory_range"); return;
        }
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                new CompanionInventoryMenu(id, inventory, record, entity), Component.literal(record.name())));
        entity.sayOccasionally(player, "inventory");
    }

    private static void rename(ServerPlayer player, CompanionRecord record, String value) {
        if (!record.mode.hasEntity()) { message(player, "unavailable"); return; }
        String name = value.strip();
        if (name.codePointCount(0, name.length()) > 32 || name.codePoints().anyMatch(code -> Character.isISOControl(code) || code == 0xA7)) {
            message(player, "invalid_name"); return;
        }
        if (record.customName.equals(name)) return;
        record.customName = name;
        appearanceChanged(player, record);
        say(player, record, "renamed");
    }
    private static void changeSkin(ServerPlayer player, CompanionRecord record, String skin) {
        if (!record.mode.hasEntity()) { message(player, "unavailable"); return; }
        if (HWConfig.npcModelSet() == WerewolfModelSet.MODERN) {
            if (ModernSkin.bySkinId(skin) == null) return;
            if (record.modernSkin.equals(skin)) return;
            record.modernSkin = skin;
        } else {
            boolean valid = java.util.Arrays.stream(WerewolfSkin.values()).anyMatch(coat -> ("howlingwerewolf:" + coat.getId()).equals(skin));
            if (!valid) return;
            if (record.classicSkin.equals(skin)) return;
            record.classicSkin = skin;
        }
        appearanceChanged(player, record);
        say(player, record, "reskinned");
    }
    private static void appearanceChanged(ServerPlayer player, CompanionRecord record) {
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        data.setDirty();
        WerewolfCompanionEntity entity = data.loaded(record.id);
        if (entity != null) entity.refreshAppearance();
    }

    @Nullable private static WerewolfCompanionEntity spawnNear(ServerPlayer player, CompanionRecord record, float health) {
        ServerLevel level = player.serverLevel();
        WerewolfCompanionEntity next = ModEntities.WEREWOLF_COMPANION.get().create(level);
        if (next == null) return null;
        BlockPos spot = findSafeSpot(level, next, player.blockPosition(), 4);
        if (spot == null) return null;
        CompanionSavedData data = CompanionSavedData.get(level.getServer());
        WerewolfCompanionEntity old = data.loaded(record.id);
        if (old != null) { remember(old); health = old.getHealth(); }
        int previousGeneration = record.generation;
        CompanionMode previousMode = record.mode;
        next.bind(record, previousGeneration + 1);
        next.setHealth(Mth.clamp(health, 1, next.getMaxHealth()));
        next.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, player.getYRot(), 0);
        // A protection mod may cancel EntityJoinLevelEvent. Keep the previous projection and
        // lease until the replacement has actually joined; no player commands interleave here.
        if (!level.addFreshEntity(next)) { next.discard(); return null; }
        if (record.generation != previousGeneration || record.mode != previousMode) { next.discard(); return null; }
        closeInventory(player, record);
        if (old != null) old.discard();
        record.generation = previousGeneration + 1;
        if (!data.claim(record, next)) { next.discard(); return null; }
        remember(next);
        data.setDirty();
        return next;
    }

    @Nullable public static BlockPos findSafeSpot(ServerLevel level, WerewolfCompanionEntity entity, BlockPos center, int radius) {
        int[] heights = {0, 1, -1, 2, -2, 3, -3, 4, -4};
        for (int ring = 1; ring <= radius; ring++) for (int x = -ring; x <= ring; x++) for (int z = -ring; z <= ring; z++) {
            if (Math.max(Math.abs(x), Math.abs(z)) != ring) continue;
            for (int dy : heights) {
                BlockPos pos = center.offset(x, dy, z);
                if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)) continue;
                var floor = level.getBlockState(pos.below());
                if (!floor.isFaceSturdy(level, pos.below(), Direction.UP) || floor.is(Blocks.MAGMA_BLOCK)
                        || floor.is(Blocks.CAMPFIRE) || floor.is(Blocks.SOUL_CAMPFIRE) || floor.is(Blocks.CACTUS)) continue;
                if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()
                        || level.getBlockState(pos).is(Blocks.POWDER_SNOW)) continue;
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) return pos;
            }
        }
        return null;
    }

    public static void remember(WerewolfCompanionEntity entity) {
        CompanionRecord record = entity.record();
        if (record == null || !(entity.level() instanceof ServerLevel level)) return;
        CompanionSavedData data = CompanionSavedData.get(level.getServer());
        if (!data.isCurrent(record, entity)) return;
        float health = record.mode == CompanionMode.DEAD ? 0 : entity.getHealth();
        ResourceLocation dimension = level.dimension().location();
        if (record.health != health || !record.position.equals(entity.blockPosition()) || !record.dimension.equals(dimension)) {
            record.health = health;
            record.position = entity.blockPosition().immutable();
            record.dimension = dimension;
            data.setDirty();
        }
    }

    public static void died(WerewolfCompanionEntity entity) {
        CompanionRecord record = entity.record();
        if (record == null || !(entity.level() instanceof ServerLevel level)) return;
        CompanionSavedData data = CompanionSavedData.get(level.getServer());
        if (!data.isCurrent(record, entity) || record.mode == CompanionMode.DEAD) return;
        record.mode = CompanionMode.DEAD;
        record.health = 0;
        record.huntStart = record.huntEnd = 0;
        data.release(record, entity);
        data.setDirty();
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(record.owner);
        if (player != null) {
            closeInventory(player, record);
            say(player, record, "fallen");
            message(player, "died", record.name()); sendView(player, false);
        }
    }

    public static void tick(MinecraftServer server) {
        CompanionSavedData data = CompanionSavedData.get(server);
        data.removeUntracked();
        data.synchronizeAvailability(HWConfig.ENABLE_COMPANIONS.get(), server.overworld().getGameTime());
        if (!HWConfig.ENABLE_COMPANIONS.get()) {
            CompanionSorting.tick(server);
            for (WerewolfCompanionEntity entity : data.activeEntities()) entity.park();
            return;
        }
        CompanionSorting.tick(server);
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            for (CompanionRecord record : data.ownedBy(player.getUUID())) {
                if (!enabled(player, record) && record.mode.hasEntity() && record.mode != CompanionMode.REST) {
                    record.mode = CompanionMode.REST;
                    data.setDirty();
                }
                if (record.mode == CompanionMode.HUNT && server.overworld().getGameTime() >= record.huntEnd) {
                    finishHunt(player, record, false);
                } else if (record.mode == CompanionMode.FOLLOW || record.mode == CompanionMode.GUARD) {
                    WerewolfCompanionEntity entity = data.loaded(record.id);
                    if (entity == null || entity.level() != player.level() || entity.distanceToSqr(player) > 32 * 32) {
                        spawnNear(player, record, record.health);
                    }
                } else if ((record.mode == CompanionMode.REST || record.mode == CompanionMode.SORT) && data.loaded(record.id) == null) {
                    restoreResting(server, record);
                }
            }
        }
    }

    private static void restoreResting(MinecraftServer server, CompanionRecord record) {
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, record.dimension));
        if (level == null || !level.hasChunkAt(record.position) || !level.isPositionEntityTicking(record.position)) return;
        WerewolfCompanionEntity entity = ModEntities.WEREWOLF_COMPANION.get().create(level);
        if (entity == null) return;
        entity.moveTo(record.position.getX() + 0.5, record.position.getY(), record.position.getZ() + 0.5, 0, 0);
        if (!level.noCollision(entity)) return;
        record.generation++;
        entity.bind(record);
        CompanionSavedData data = CompanionSavedData.get(server);
        if (data.claim(record, entity) && !level.addFreshEntity(entity)) data.release(record, entity);
        data.setDirty();
    }

    public static boolean mayAttack(ServerPlayer owner, LivingEntity target) {
        if (!HWConfig.ENABLE_COMPANIONS.get()) return false;
        if (target == owner || !target.isAlive() || target instanceof Creeper || target.isAlliedTo(owner)
                || target instanceof TamableAnimal pet && owner.getUUID().equals(pet.getOwnerUUID())) return false;
        if (target instanceof WerewolfCompanionEntity companion && companion.record() != null
                && companion.record().belongsTo(owner.getUUID())) return false;
        if (target instanceof WerewolfCompanionEntity companion && companion.owner() != null
                && (!owner.canHarmPlayer(companion.owner()) || owner.isAlliedTo(companion.owner()))) return false;
        if (target instanceof net.minecraft.world.entity.player.Player player) return owner.canHarmPlayer(player) && !player.isCreative() && !player.isSpectator();
        return !target.isInvulnerable();
    }
    public static void defend(ServerPlayer owner, LivingEntity target) {
        if (!mayAttack(owner, target)) return;
        CompanionSavedData data = CompanionSavedData.get(owner.level().getServer());
        for (CompanionRecord record : data.ownedBy(owner.getUUID())) {
            if (record.mode != CompanionMode.GUARD || !enabled(owner, record)) continue;
            WerewolfCompanionEntity entity = data.loaded(record.id);
            if (entity != null && entity.isAlive() && entity.level() == target.level() && entity.distanceToSqr(target) <= 48 * 48) {
                if (entity.getTarget() != target) entity.sayOccasionally(owner, "combat");
                entity.setTarget(target);
            }
        }
    }

    public static void sendView(ServerPlayer player, boolean open) {
        sendView(player, open, null);
    }

    private static void sendView(ServerPlayer player, boolean open, @Nullable UUID selected) {
        CompoundTag tag = createView(player);
        // Only explicit opens carry a view target. Routine refreshes preserve the
        // client's selected companion, detail view, preview and keyboard focus.
        if (open) tag.putString("OpenTarget", selected == null ? "" : selected.toString());
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CompanionViewPacket(tag, open));
    }

    /** Owner-only overview data, also used when reopening the focused controls. */
    public static CompoundTag createView(ServerPlayer player) {
        CompanionSavedData data = CompanionSavedData.get(player.getServer());
        CompoundTag tag = new CompoundTag();
        tag.putString("View", "manage");
        tag.putBoolean("SystemEnabled", HWConfig.ENABLE_COMPANIONS.get());
        tag.putBoolean("SortingEnabled", CompanionSorting.enabled());
        tag.putInt("Capacity", capacity(player));
        tag.putInt("Skill", skillRank(player));
        tag.putString("Address", data.address(player.getUUID()));
        tag.putString("Model", HWConfig.npcModelSet().id());
        ListTag skins = new ListTag();
        if (HWConfig.npcModelSet() == WerewolfModelSet.MODERN) {
            for (ModernSkin skin : ModernSkin.values()) addSkin(skins, skin.skinId());
        } else {
            for (WerewolfSkin skin : WerewolfSkin.values()) addSkin(skins, "howlingwerewolf:" + skin.getId());
        }
        tag.put("Skins", skins);
        ListTag entries = new ListTag();
        long now = data.clock(player.getServer().overworld().getGameTime());
        for (CompanionRecord record : data.ownedBy(player.getUUID())) {
            WerewolfCompanionEntity entity = data.loaded(record.id);
            if (entity != null) remember(entity);
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", record.id.toString());
            entry.putString("Name", record.name());
            entry.putString("CustomName", record.customName);
            entry.putString("Skin", record.skin());
            entry.putInt("Tier", record.tier);
            entry.putInt("Mode", record.mode.ordinal());
            entry.putBoolean("SortNearby", entity != null && entity.level() == player.level() && entity.distanceToSqr(player) <= 16 * 16);
            CompanionSorting.writeView(entry, record, player.getServer());
            entry.putFloat("Health", record.health);
            entry.putFloat("MaxHealth", (float) CompanionRules.maxHealth(effectiveTier(player, record)));
            entry.putBoolean("Enabled", enabled(player, record));
            entry.putLong("HuntRemaining", record.mode == CompanionMode.HUNT ? Math.max(0, record.huntEnd - now) : 0);
            entry.putLong("HuntDuration", record.mode == CompanionMode.HUNT ? Math.max(0, record.huntEnd - record.huntStart) : 0);
            entry.putLong("HuntCooldownRemaining", Math.max(0, record.huntCooldownEnd - now));
            entry.putBoolean("Nearby", entity != null && entity.isAlive() && entity.level() == player.level() && entity.distanceToSqr(player) <= 64);
            entries.add(entry);
        }
        tag.put("Companions", entries);
        return tag;
    }

    private static void addSkin(ListTag skins, String id) {
        CompoundTag skin = new CompoundTag();
        skin.putString("Id", id);
        skin.putString("Name", CompanionRecord.nameForSkin(id));
        skins.add(skin);
    }

    private static boolean hasItems(ServerPlayer player, Item item, int count) {
        if (player.getAbilities().instabuild) return true;
        int found = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) found += stack.getCount();
        }
        return found >= count;
    }
    private static void consume(ServerPlayer player, Item item, int count) {
        if (player.getAbilities().instabuild) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && count > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int taken = Math.min(count, stack.getCount());
            stack.shrink(taken);
            count -= taken;
        }
        player.getInventory().setChanged();
    }
    public static void closeInventory(ServerPlayer player, CompanionRecord record) {
        if (player.containerMenu instanceof CompanionInventoryMenu menu && menu.record == record) player.closeContainer();
    }
    public static void say(ServerPlayer player, CompanionRecord record, String key) {
        CompanionDialogue.say(player, record, key);
    }
    public static void message(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable("message.howlingwerewolf.companion." + key, args));
    }
    private CompanionManager() {}
}
