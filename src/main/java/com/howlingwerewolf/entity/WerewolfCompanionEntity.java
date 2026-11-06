/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.entity;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.WerewolfSkin;
import com.howlingwerewolf.companion.CompanionDialogue;
import com.howlingwerewolf.companion.CompanionManager;
import com.howlingwerewolf.companion.CompanionMode;
import com.howlingwerewolf.companion.CompanionRecord;
import com.howlingwerewolf.companion.CompanionRules;
import com.howlingwerewolf.companion.CompanionSavedData;
import com.howlingwerewolf.companion.CompanionSorting;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** A temporary merchant, or one live projection of a persistent, player-owned companion. */
public final class WerewolfCompanionEntity extends AbstractVillager implements WerewolfNpcAppearance {
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(WerewolfCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> RECRUITED = SynchedEntityData.defineId(WerewolfCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    /** Nine offers; each merchant brings six of them, without duplicates and without restocking. */
    private record Trade(Supplier<ItemLike> wants, int wantsCount, @Nullable Supplier<ItemLike> also, int alsoCount,
                         Supplier<ItemLike> gives, int givesCount, int maxUses) {
        MerchantOffer offer() {
            return new MerchantOffer(new ItemStack(wants.get(), wantsCount), also == null ? ItemStack.EMPTY : new ItemStack(also.get(), alsoCount),
                    new ItemStack(gives.get(), givesCount), maxUses, 0, 0.0F);
        }
    }
    private static final List<Trade> TRADES = List.of(
            new Trade(() -> Items.EMERALD, 6, null, 0, () -> ModItems.SILVER_INGOT.get(), 4, 8),
            new Trade(() -> Items.IRON_INGOT, 16, null, 0, () -> ModItems.SILVER_INGOT.get(), 2, 8),
            new Trade(() -> Items.EMERALD, 12, () -> ModBlocks.WOLFSBANE.get(), 4, () -> ModItems.MOONBANE_PEARL.get(), 1, 4),
            new Trade(() -> ModBlocks.WOLFSBANE.get(), 6, () -> Items.DIAMOND, 1, () -> ModItems.MOONBANE_PEARL.get(), 1, 4),
            new Trade(() -> ModItems.SILVER_INGOT.get(), 4, null, 0, () -> ModBlocks.WOLFSBANE.get(), 2, 8),
            new Trade(() -> ModBlocks.WOLFSBANE.get(), 4, () -> Items.EMERALD, 2, () -> ModItems.WEREWOLF_POTION.get(), 1, 2),
            new Trade(() -> ModItems.SILVER_INGOT.get(), 4, () -> Items.EMERALD, 2, () -> ModItems.WOLFSBANE_POTION.get(), 1, 2),
            new Trade(() -> Items.EMERALD, 4, null, 0, () -> ModBlocks.WOLFSBANE.get(), 4, 8),
            new Trade(() -> ModBlocks.WOLFSBANE.get(), 4, null, 0, () -> ModItems.SILVER_INGOT.get(), 2, 8));
    private static final int OFFERED = 6;
    private CompanionRecord record;
    private int generation;
    private long expiresAt;
    private UUID offeredTo;
    private long offerExpires;
    private UUID summoner;
    private BlockPos altar;
    private int arrivalTicks;
    private boolean approachingAltar;
    private long lastHurt;
    private long lastAmbientLine = -200;
    private long nextIdleLine;
    private String merchantClassicSkin;
    private String merchantModernSkin;
    private final SimpleContainer merchantInventory = new SimpleContainer(CompanionRules.INVENTORY_SIZE);
    private final Set<BlockPos> openedContainers = new HashSet<>();
    private boolean sortBusy;

    public WerewolfCompanionEntity(EntityType<? extends WerewolfCompanionEntity> type, Level level) {
        super(type, level);
        setCanPickUpLoot(false);
        setPersistenceRequired();
        xpReward = 0;
        expiresAt = level.getGameTime() + CompanionRules.MERCHANT_LIFETIME;
        resetIdleDialogue();
        if (!level.isClientSide()) {
            merchantModernSkin = ModernSkin.values()[random.nextInt(ModernSkin.values().length)].skinId();
            merchantClassicSkin = "howlingwerewolf:" + WerewolfSkin.values()[random.nextInt(WerewolfSkin.values().length)].getId();
            refreshAppearance();
            setHealth(getMaxHealth());
        }
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, CompanionRules.maxHealth(0))
                .add(Attributes.ATTACK_DAMAGE, CompanionRules.attackDamage(0))
                .add(Attributes.MOVEMENT_SPEED, 0.30).add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.20);
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, ModernSkin.ASHEN.skinId());
        entityData.define(RECRUITED, false);
    }
    private void setSkin(String skin) {
        entityData.set(SKIN, CompanionRecord.normalizeSkin(skin));
        setCustomName(Component.literal(record == null ? CompanionRecord.nameForSkin(getNpcSkinId()) : record.name()));
        setCustomNameVisible(true);
    }
    @Override public String getNpcSkinId() { return entityData.get(SKIN); }
    @Override public WerewolfModelSet getNpcModelSet() {
        return ModernSkin.bySkinId(getNpcSkinId()) == null ? WerewolfModelSet.CLASSIC : WerewolfModelSet.MODERN;
    }
    @Override public ModernSkin getNpcCoat() {
        ModernSkin coat = ModernSkin.bySkinId(getNpcSkinId());
        return coat == null ? ModernSkin.ASHEN : coat;
    }
    @Override public WerewolfForm getNpcForm() { return WerewolfForm.WEREWOLF; }
    @Nullable public CompanionRecord record() { return record; }
    public int generation() { return generation; }
    public boolean isRecruited() { return entityData.get(RECRUITED); }
    public void refreshAppearance() {
        if (level().isClientSide()) return;
        setSkin(record != null ? record.skin() : HWConfig.npcModelSet() == WerewolfModelSet.MODERN ? merchantModernSkin : merchantClassicSkin);
    }

    public void bind(CompanionRecord record) {
        bind(record, record.generation);
    }
    public void bind(CompanionRecord record, int generation) {
        this.record = record;
        this.generation = generation;
        entityData.set(RECRUITED, true);
        setSkin(record.skin());
        offeredTo = null;
        altar = null;
        setTradingPlayer(null);
        offers = new MerchantOffers();
        getNavigation().stop();
        refreshStats();
        setHealth(Mth.clamp(record.health, 0, getMaxHealth()));
    }
    @Nullable public ServerPlayer owner() {
        return record == null || !(level() instanceof ServerLevel level) ? null : level.getServer().getPlayerList().getPlayer(record.owner);
    }
    public void refreshStats() {
        ServerPlayer owner = owner();
        int tier = record == null ? 0 : owner == null ? record.tier : CompanionManager.effectiveTier(owner, record);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(CompanionRules.maxHealth(tier));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(CompanionRules.attackDamage(tier));
        if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new Goal() {
            { setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP)); }
            @Override public boolean canUse() { return record != null && record.mode == CompanionMode.REST; }
            @Override public void start() { getNavigation().stop(); setTarget(null); }
        });
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true) {
            @Override public boolean canUse() { return canFight() && super.canUse(); }
            @Override public boolean canContinueToUse() { return canFight() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(4, new Goal() {
            private int pathDelay;
            { setFlags(EnumSet.of(Flag.MOVE)); }
            @Override public boolean canUse() {
                ServerPlayer player = owner();
                return record != null && (record.mode == CompanionMode.FOLLOW || record.mode == CompanionMode.GUARD)
                        && player != null && player.level() == level() && getTarget() == null && distanceToSqr(player) > 9;
            }
            @Override public boolean canContinueToUse() {
                ServerPlayer player = owner();
                return player != null && record != null && (record.mode == CompanionMode.FOLLOW || record.mode == CompanionMode.GUARD)
                        && getTarget() == null && player.level() == level() && distanceToSqr(player) > 4;
            }
            @Override public void start() { pathDelay = 0; }
            @Override public void stop() { getNavigation().stop(); }
            @Override public void tick() {
                ServerPlayer player = owner();
                if (player != null && --pathDelay <= 0) { pathDelay = 10; getNavigation().moveTo(player, 1.15); }
            }
        });
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.65) {
            @Override public boolean canUse() { return !isRecruited() && altar == null && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isRecruited() && altar == null && super.canContinueToUse(); }
        });
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10) {
            @Override public boolean canUse() { return !sortBusy && super.canUse(); }
            @Override public boolean canContinueToUse() { return !sortBusy && super.canContinueToUse(); }
        });
        goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() { return !sortBusy && super.canUse(); }
            @Override public boolean canContinueToUse() { return !sortBusy && super.canContinueToUse(); }
        });
    }
    private boolean canFight() {
        ServerPlayer player = owner();
        return record != null && record.mode == CompanionMode.GUARD && player != null
                && CompanionManager.enabled(player, record) && getTarget() != null && CompanionManager.mayAttack(player, getTarget());
    }
    /** Following, resting or sorting: monsters leave it alone, and like a follower it does not fight back (only Guard fights). */
    public boolean isFollowingPassively() {
        return isRecruited() && record != null
                && (record.mode == CompanionMode.FOLLOW || record.mode == CompanionMode.REST || record.mode == CompanionMode.SORT);
    }
    public static boolean ignoresFollowingCompanion(LivingEntity attacker, @Nullable LivingEntity target) {
        return attacker instanceof Mob && !(attacker instanceof WerewolfCompanionEntity)
                && attacker.getType().getCategory() == MobCategory.MONSTER
                && target instanceof WerewolfCompanionEntity companion && companion.isFollowingPassively();
    }
    public void clearHostileTargets() {
        if (!isFollowingPassively() || !(level() instanceof ServerLevel level)) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(128),
                mob -> ignoresFollowingCompanion(mob, this))) {
            boolean targeting = mob.getTarget() == this;
            var brain = mob.getBrain();
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == this) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                targeting = true;
            }
            if (targeting) { mob.setTarget(null); mob.setAggressive(false); mob.getNavigation().stop(); }
        }
    }
    @Override public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(record != null && record.mode != CompanionMode.GUARD ? null : target);
    }
    @Override public boolean canAttack(LivingEntity target) {
        ServerPlayer player = owner();
        return player != null && record != null && record.mode == CompanionMode.GUARD
                && CompanionManager.enabled(player, record) && CompanionManager.mayAttack(player, target) && super.canAttack(target);
    }
    @Override public boolean doHurtTarget(Entity target) {
        return target instanceof LivingEntity living && canAttack(living) && super.doHurtTarget(target);
    }

    @Override public void aiStep() {
        if (!level().isClientSide() && !HWConfig.ENABLE_COMPANIONS.get()) { park(); return; }
        // Vanilla only advances the swing timer for monsters and players; villagers never attack.
        updateSwingTime();
        if (!level().isClientSide() && getTarget() != null && !canAttack(getTarget())) {
            setTarget(null); setAggressive(false); getNavigation().stop();
        }
        super.aiStep();
        if (!(level() instanceof ServerLevel level)) return;
        if (tickCount % 20 == 0) refreshAppearance();
        if (!isRecruited()) {
            if (level.getGameTime() >= expiresAt) {
                ServerPlayer visitor = summoner == null ? null : level.getServer().getPlayerList().getPlayer(summoner);
                if (visitor != null && visitor.level() == level && distanceToSqr(visitor) <= 16 * 16) sayTo(visitor, "farewell");
                closeTrading(); discard(); return;
            }
            tickArrival(level);
            return;
        }
        if (record == null) { discard(); return; }
        CompanionSavedData data = CompanionSavedData.get(level.getServer());
        if (!data.isCurrent(record, this) || !record.mode.hasEntity()) { discard(); return; }
        if (record.mode != CompanionMode.SORT) endSortVisuals();
        ServerPlayer player = owner();
        if (player == null) { CompanionManager.remember(this); discard(); return; }
        if (getTarget() != null && !canAttack(getTarget())) setTarget(null);
        if (tickCount % 20 == 0) {
            refreshStats();
            if (level.getGameTime() - lastHurt >= 100 && getHealth() < getMaxHealth()) {
                heal(CompanionRules.regeneration(CompanionManager.effectiveTier(player, record)));
            }
            CompanionManager.remember(this);
            tickIdleDialogue(player);
        }
    }
    /**
     * Opens the lids the way a player would be seen to: chests through their lid block event and
     * barrels through their OPEN state, with the vanilla sounds. A player's own counter wins.
     */
    public void openSortContainers(List<BlockEntity> members) {
        closeSortContainers();
        boolean sounded = false;
        for (BlockEntity member : members) {
            if (!(member instanceof ChestBlockEntity) && !(member instanceof BarrelBlockEntity)) continue;
            openedContainers.add(member.getBlockPos().immutable());
            sounded |= showLid(member, true, sounded);
        }
    }
    public void closeSortContainers() {
        boolean sounded = false;
        for (BlockPos pos : List.copyOf(openedContainers)) {
            openedContainers.remove(pos);
            // An unloading chunk must stay unloaded.
            if (!level().hasChunkAt(pos)) continue;
            BlockEntity member = level().getBlockEntity(pos);
            if (member instanceof ChestBlockEntity || member instanceof BarrelBlockEntity) sounded |= showLid(member, false, sounded);
        }
    }
    private boolean showLid(BlockEntity member, boolean open, boolean quiet) {
        if (!(level() instanceof ServerLevel level)) return false;
        BlockPos pos = member.getBlockPos();
        BlockState state = member.getBlockState();
        if (CompanionSorting.inUse(level, member)) return false;
        if (member instanceof ChestBlockEntity) {
            level.blockEvent(pos, state.getBlock(), 1, open ? 1 : 0);
            if (!quiet) level.playSound(null, pos, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        } else if (state.hasProperty(BarrelBlock.OPEN)) {
            level.setBlock(pos, state.setValue(BarrelBlock.OPEN, open), 3);
            if (!quiet) level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return true;
    }
    /** Display only, and never dropped: this entity is rebuilt from its record and owns no items. */
    public void setCarriedItem(ItemStack stack) {
        if (ItemStack.matches(stack, getMainHandItem())) return;
        setDropChance(EquipmentSlot.MAINHAND, 0);
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }
    public void setSortBusy(boolean busy) { sortBusy = busy; }
    public void endSortVisuals() {
        closeSortContainers();
        setCarriedItem(ItemStack.EMPTY);
        sortBusy = false;
    }
    public void resetIdleDialogue() { nextIdleLine = level().getGameTime() + 1200 + random.nextInt(1201); }
    private void tickIdleDialogue(ServerPlayer player) {
        long now = level().getGameTime();
        if (now < nextIdleLine || record == null || !record.mode.hasEntity() || getTarget() != null
                || now - lastHurt < 100 || player.level() != level() || distanceToSqr(player) > 64
                || !player.isAlive() || player.isSpectator() || player.isSleeping()
                || player.containerMenu != player.inventoryMenu) return;
        nextIdleLine = now + 1800 + random.nextInt(1801);
        if (CompanionSavedData.get(((ServerLevel) level()).getServer()).allowIdleSpeech(record.owner, now)) {
            sayTo(player, "idle_" + record.mode.getSerializedName());
        }
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || player.isSpectator()) return InteractionResult.PASS;
        if (!level().isClientSide() && !HWConfig.ENABLE_COMPANIONS.get()) {
            if (player instanceof ServerPlayer serverPlayer) CompanionManager.message(serverPlayer, "disabled");
            return InteractionResult.SUCCESS;
        }
        if (isRecruited()) {
            if (player instanceof ServerPlayer serverPlayer && record != null) {
                if (record.belongsTo(player.getUUID())) {
                    if (player.isShiftKeyDown()) CompanionManager.openInventory(serverPlayer, record);
                    else CompanionManager.openControls(serverPlayer, this);
                }
                else CompanionManager.message(serverPlayer, "not_owner");
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (isTrading()) return InteractionResult.sidedSuccess(level().isClientSide());
        if (player instanceof ServerPlayer serverPlayer) {
            altar = null;
            getNavigation().stop();
            if (player.getItemInHand(hand).is(ModItems.ALPHA_WEREWOLF_BADGE.get())) {
                CompanionManager.offerRecruitment(serverPlayer, this);
            } else if (!getOffers().isEmpty()) {
                setTradingPlayer(player);
                openTradingScreen(player, getDisplayName(), 1);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }
    public boolean canOfferRecruitment(ServerPlayer player) {
        return HWConfig.ENABLE_COMPANIONS.get() && !isRecruited() && isAlive() && !isTrading()
                && level().getGameTime() < expiresAt && player.level() == level() && player.canReach(this, 1);
    }
    public void offerTo(ServerPlayer player) { offeredTo = player.getUUID(); offerExpires = level().getGameTime() + 200; }
    public boolean acceptsOffer(ServerPlayer player) {
        return canOfferRecruitment(player) && player.getUUID().equals(offeredTo) && level().getGameTime() <= offerExpires;
    }
    @Override protected void updateTrades() {
        if (!HWConfig.ENABLE_COMPANIONS.get() || isRecruited()) return;
        List<Trade> pool = new ArrayList<>(TRADES);
        for (int i = pool.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Trade swap = pool.get(i); pool.set(i, pool.get(j)); pool.set(j, swap);
        }
        MerchantOffers offers = getOffers();
        for (int i = 0; i < OFFERED; i++) offers.add(pool.get(i).offer());
    }
    @Override protected void rewardTradeXp(MerchantOffer offer) {}
    // A werewolf merchant barks, pants and whines instead of haggling like a villager.
    @Override public net.minecraft.sounds.SoundEvent getNotifyTradeSound() {
        return net.minecraft.sounds.SoundEvents.WOLF_AMBIENT;
    }
    @Override protected net.minecraft.sounds.SoundEvent getTradeUpdatedSound(boolean validTrade) {
        return validTrade ? net.minecraft.sounds.SoundEvents.WOLF_PANT : net.minecraft.sounds.SoundEvents.WOLF_WHINE;
    }
    @Override public boolean showProgressBar() { return false; }
    @Override public void notifyTrade(MerchantOffer offer) {
        if (isRecruited() || !HWConfig.ENABLE_COMPANIONS.get()) return;
        super.notifyTrade(offer);
        if (getTradingPlayer() instanceof ServerPlayer player) sayTo(player, "traded");
    }
    @Override public SimpleContainer getInventory() { return record == null ? merchantInventory : record.inventory; }
    @Override public boolean wantsToPickUp(ItemStack stack) { return false; }
    @Override public boolean canBeLeashed(Player player) { return false; }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }

    @Override protected void actuallyHurt(DamageSource source, float amount) {
        float previousHealth = getHealth();
        lastHurt = level().getGameTime();
        if (record != null && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ServerPlayer player = owner();
            int tier = player == null ? record.tier : CompanionManager.effectiveTier(player, record);
            amount *= CompanionRules.damageMultiplier(tier);
        }
        super.actuallyHurt(source, amount);
        if (level().isClientSide()) return;
        CompanionManager.remember(this);
        ServerPlayer player = owner();
        if (isAlive() && getHealth() < previousHealth && source.getEntity() instanceof LivingEntity attacker && canAttack(attacker)) {
            if (getTarget() != attacker && player != null) sayOccasionally(player, "combat");
            setTarget(attacker);
        }
        if (player != null && isAlive() && previousHealth > getMaxHealth() * 0.25F && getHealth() <= getMaxHealth() * 0.25F) {
            sayOccasionally(player, "wounded");
        }
    }
    @Override public void die(DamageSource source) {
        if (!level().isClientSide()) CompanionManager.died(this);
        closeTrading();
        super.die(source);
    }
    @Override public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            closeSortContainers();
            if (level() instanceof ServerLevel level) CompanionSavedData.get(level.getServer()).untrack(this);
            if (record != null) {
                if (reason == RemovalReason.KILLED) CompanionManager.died(this);
                CompanionManager.remember(this);
                CompanionSavedData.get(((ServerLevel) level()).getServer()).release(record, this);
            }
        }
        closeTrading();
        super.remove(reason);
    }
    @Override public void onAddedToWorld() {
        super.onAddedToWorld();
        if (level() instanceof ServerLevel level) CompanionSavedData.get(level.getServer()).track(this);
    }
    /** A chunk unload removes this projection without calling {@link #remove}; release its lease here. */
    @Override public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (level() instanceof ServerLevel level) {
            CompanionSavedData data = CompanionSavedData.get(level.getServer());
            data.untrack(this);
            if (record != null) { CompanionManager.remember(this); data.release(record, this); }
        }
    }
    public void park() {
        CompanionManager.remember(this);
        ServerPlayer player = owner();
        if (player != null && record != null) CompanionManager.closeInventory(player, record);
        closeTrading();
        setTarget(null);
        getNavigation().stop();
        discard();
    }
    private void closeTrading() {
        if (getTradingPlayer() instanceof ServerPlayer player) player.closeContainer();
        setTradingPlayer(null);
    }
    // Temporary merchants vanish on unload. Recruited entities are reconstructed from owner records.
    @Override public boolean shouldBeSaved() { return false; }
    @Override public boolean saveAsPassenger(CompoundTag tag) { return false; }
    @Override public boolean canChangeDimensions() { return false; }
    @Override public Entity changeDimension(ServerLevel destination, ITeleporter teleporter) {
        // Cross-dimension following/recall goes through the owner record. Vanilla would clone
        // this AbstractVillager and copy its inventory without transferring its record lease.
        return null;
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("CompanionSkin", getNpcSkinId());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CompanionSkin")) setSkin(tag.getString("CompanionSkin"));
    }

    public boolean beginArrival(ServerPlayer player, BlockPos center) {
        summoner = player.getUUID();
        altar = center.immutable();
        arrivalTicks = 0;
        approachingAltar = false;
        Path path = getNavigation().createPath(player, 2);
        if (path == null || !path.canReach()) {
            approachingAltar = true;
            path = getNavigation().createPath(center.above(), 1);
        }
        return path != null && path.canReach() && getNavigation().moveTo(path, 1.25);
    }
    public void setSummoner(ServerPlayer player) { summoner = player.getUUID(); }
    public void arriveAtAltar(BlockPos center) {
        altar = null;
        getNavigation().stop();
        teleportTo(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5);
    }
    private void tickArrival(ServerLevel level) {
        if (altar == null) return;
        if (isTrading()) { altar = null; return; }
        arrivalTicks++;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(summoner);
        if (player != null && player.level() == level && distanceToSqr(player) <= 9
                || approachingAltar && blockPosition().distSqr(altar.above()) <= 4) {
            altar = null;
            getNavigation().stop();
            return;
        }
        if (arrivalTicks > 300) { teleportTo(altar.getX() + 0.5, altar.getY() + 1, altar.getZ() + 0.5); altar = null; return; }
        if (arrivalTicks % 20 != 0) return;
        if (!approachingAltar && player != null && player.level() == level) {
            Path path = getNavigation().createPath(player, 2);
            if (path != null && path.canReach() && getNavigation().moveTo(path, 1.25)) return;
            approachingAltar = true;
        }
        if (approachingAltar || player == null || player.level() != level) {
            approachingAltar = true;
            Path path = getNavigation().createPath(altar.above(), 1);
            if (path == null || !path.canReach() || !getNavigation().moveTo(path, 1.25)) {
                teleportTo(altar.getX() + 0.5, altar.getY() + 1, altar.getZ() + 0.5);
                altar = null;
            }
        }
    }
    public void sayTo(ServerPlayer player, String key) {
        if (record != null) CompanionDialogue.say(player, record, key);
        else CompanionDialogue.send(player, getName().getString(), key, random);
    }
    public void sayOccasionally(ServerPlayer player, String key) {
        long now = level().getGameTime();
        if (now - lastAmbientLine < 200) return;
        lastAmbientLine = now;
        sayTo(player, key);
    }
}
