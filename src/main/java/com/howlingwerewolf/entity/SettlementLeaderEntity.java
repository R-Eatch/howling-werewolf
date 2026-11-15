package com.howlingwerewolf.entity;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.world.SettlementData;
import com.howlingwerewolf.world.SettlementPositions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public final class SettlementLeaderEntity extends AbstractVillager implements WerewolfNpcAppearance {
    private static final NpcAppearanceData.Keys APPEARANCE = new NpcAppearanceData.Keys(
            SynchedEntityData.defineId(SettlementLeaderEntity.class, EntityDataSerializers.BOOLEAN),
            SynchedEntityData.defineId(SettlementLeaderEntity.class, EntityDataSerializers.INT));
    @Nullable private BlockPos site;
    @Nullable private Long villageKey;
    @Nullable private SettlementPositions.Routine routine;
    private final SettlementDialogue dialogue = new SettlementDialogue(this);
    private long healReadyAt;
    private long workDay = -1;
    private long nextRestockAt;
    private int restocksToday;

    public SettlementLeaderEntity(EntityType<? extends SettlementLeaderEntity> type, Level level) {
        super(type, level);
        xpReward = isWarden() ? 16 : 10;
        setPersistenceRequired();
        NpcAppearanceData.initialize(this, APPEARANCE, false);
        setCustomName(Component.translatable(type.getDescriptionId()));
        setCustomNameVisible(true);
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        equipSilverhideArmor();
        if (isWarden()) {
            equip(EquipmentSlot.MAINHAND, wardenSword());
        } else {
            setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.TRAPDOOR, -1.0F);
            equip(EquipmentSlot.FEET, new ItemStack(ModItems.SILVERHIDE_BOOTS.get()));
        }
    }

    private ItemStack wardenSword() {
        ItemStack sword = new ItemStack(ModItems.SILVER_SWORD.get());
        registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.SHARPNESS)
                .ifPresent(sharpness -> sword.enchant(sharpness, 2));
        return sword;
    }

    private void equip(EquipmentSlot slot, ItemStack stack) {
        setItemSlot(slot, stack);
        setDropChance(slot, 0);
    }

    private void equipSilverhideArmor() {
        if (getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            equip(EquipmentSlot.CHEST, new ItemStack(ModItems.SILVERHIDE_CHESTPLATE.get()));
        }
        if (getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
            equip(EquipmentSlot.LEGS, new ItemStack(ModItems.SILVERHIDE_LEGGINGS.get()));
        }
    }

    public static AttributeSupplier.Builder wardenAttributes() {
        return HunterEntity.createAttributes().add(Attributes.MAX_HEALTH, 40);
    }

    public static AttributeSupplier.Builder chiefAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ARMOR, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.29).add(Attributes.ATTACK_DAMAGE,
                        WerewolfEntity.feralAttackDamage(net.minecraft.world.Difficulty.NORMAL) * 1.25).add(Attributes.FOLLOW_RANGE, 36);
    }

    public boolean isWarden() { return getType() == ModEntities.HUNTER_CAPTAIN.get(); }

    public boolean hasSettlement() { return site != null; }

    @Nullable BlockPos trainingYard() {
        return isWarden() && site != null && level() instanceof ServerLevel server
                ? SettlementPositions.trainingYard(server, site) : null;
    }

    public void settlementSpawn(BlockPos site, @Nullable Long villageKey, ModernSkin coat, SettlementPositions.Routine routine) {
        this.site = site.immutable();
        this.villageKey = villageKey;
        this.routine = routine;
        NpcAppearanceData.setCoat(entityData, APPEARANCE, coat);
        setHomeTo(villageKey == null ? site : BlockPos.of(villageKey).atY(site.getY()), isWarden() ? 64 : 48);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        NpcAppearanceData.define(builder, APPEARANCE);
    }

    @Override public WerewolfModelSet getNpcModelSet() { return NpcAppearanceData.modelSet(entityData, APPEARANCE); }
    @Override public ModernSkin getNpcCoat() { return NpcAppearanceData.coat(entityData, APPEARANCE); }
    @Override public WerewolfForm getNpcForm() { return WerewolfForm.WEREWOLF; }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new HealSquadGoal());
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.08, true));
        goalSelector.addGoal(4, isWarden() ? new WardenPatrolGoal() : new SettlementRoutineGoal(this, () -> routine));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7) {
            @Override public boolean canUse() { return !isSleeping() && (isWarden() || SettlementPositions.isDay(level())) && super.canUse(); }
        });
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (isWarden()) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity, level) -> HunterEntity.isWerewolfTarget(entity)));
            targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity, level) -> HunterEntity.isVillageThreat(entity)));
        } else {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10,
                    true, false, (entity, level) -> SettlementCombat.isHunter(entity)));
        }
    }

    public boolean supports(@Nullable Entity entity) {
        if (isWarden()) return villageKey != null && entity instanceof HunterEntity hunter && hunter.belongsToVillage(villageKey);
        return site != null && entity instanceof TribeWerewolfEntity resident && resident.belongsTo(site);
    }

    @Override protected boolean considersEntityAsAlly(Entity entity) { return supports(entity) || super.considersEntityAsAlly(entity); }

    void defend(LivingEntity attacker, ServerPlayer player) {
        if (isSleeping()) stopSleeping();
        setLastHurtByMob(attacker);
        setTarget(attacker);
        dialogue.defendPlayer(player);
    }

    @Override
    public boolean hurtServer(ServerLevel server, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(server, source, damage);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && canAttack(attacker) && !supports(attacker)) {
            if (getTradingPlayer() instanceof net.minecraft.server.level.ServerPlayer player) player.closeContainer();
            setTradingPlayer(null);
            for (Mob ally : squad(28)) {
                if (ally.canAttack(attacker) && !ally.isAlliedTo(attacker)) {
                    if (ally instanceof HunterEntity hunter) hunter.protectCaptain(attacker);
                    else ((TribeWerewolfEntity) ally).protectChief(attacker);
                }
            }
        }
        return hurt;
    }

    private List<Mob> squad(double range) {
        return level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(range, 5, range),
                mob -> mob.isAlive() && distanceToSqr(mob) <= range * range && supports(mob));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (SettlementRoutineGoal.wakeForActivity(this)) return;
        if (tickCount % 20 != 0) return;
        NpcAppearanceData.refresh(this, APPEARANCE);
        if (!isWarden()) getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(WerewolfEntity.feralAttackDamage(level().getDifficulty()) * 1.25);
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            addEffect(new MobEffectInstance(MobEffects.STRENGTH, 50, 0));
            addEffect(new MobEffectInstance(MobEffects.SPEED, 50, 0));
            // Regeneration I needs at least a full 50-tick effect interval to heal.
            if (!hasEffect(MobEffects.REGENERATION)) addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 50, 0));
            addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
            dialogue.fighting(target);
            return;
        }
        dialogue.idle();
        if (isTrading() || !atWorkTime()) return;
        long day = level().getOverworldClockTime() / SettlementData.DAY;
        if (workDay != day) {
            workDay = day;
            restocksToday = 0;
        }
        if (restocksToday >= 2 || level().getGameTime() < nextRestockAt) return;
        MerchantOffers offers = getOffers();
        if (offers.stream().noneMatch(offer -> offer.getUses() > 0)) return;
        if (routine == null || !routine.work().closerThan(blockPosition(), 3)) return;
        offers.forEach(MerchantOffer::resetUses);
        restocksToday++;
        nextRestockAt = level().getGameTime() + 2400;
    }

    @Override public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null && isSleeping()) stopSleeping();
    }

    @Override
    public boolean canRestock() { return site != null; }

    private boolean atWorkTime() {
        long hour = Math.floorMod(level().getOverworldClockTime(), SettlementData.DAY);
        return site != null && hour >= 2000 && hour < 9000
                && level().getBlockState(site).is(ModBlocks.SETTLEMENT_MARKER.get());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || getTarget() != null && getTarget().isAlive() || isTrading() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player instanceof ServerPlayer visitor && !getOffers().isEmpty()) {
            dialogue.greet(visitor);
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override protected void updateTrades(ServerLevel level) { WerewolfTrades.draw(this, level, getOffers(), isWarden() ? WerewolfTrades.MerchantRole.WARDEN : WerewolfTrades.MerchantRole.CHIEF); }
    @Override protected void rewardTradeXp(MerchantOffer offer) {}
    @Override public void notifyTrade(MerchantOffer offer) {
        super.notifyTrade(offer);
        if (getTradingPlayer() instanceof ServerPlayer player) dialogue.traded(player);
    }
    @Override public boolean showProgressBar() { return false; }
    @Override public boolean canUsePortal(boolean ignorePassenger) { return false; }
    @Override public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }

    @Override
    public void die(DamageSource source) {
        boolean wasDead = dead;
        super.die(source);
        if (!wasDead && dead && site != null && level() instanceof ServerLevel server) {
            SettlementData.get(server).onDeath(server, site, getUUID(), this);
        }
    }

    public CompoundTag saveTradeStock() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("WorkDay", workDay);
        tag.putLong("NextRestock", nextRestockAt);
        tag.putInt("Restocks", restocksToday);
        return tag;
    }

    public void restoreTradeStock(CompoundTag tag) {
        workDay = tag.contains("WorkDay") ? tag.getLongOr("WorkDay", -1) : -1;
        nextRestockAt = tag.getLongOr("NextRestock", 0);
        restocksToday = tag.getIntOr("Restocks", 0);
        if (tag.contains("Offers")) {
            MerchantOffers.CODEC.parse(registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("Offers"))
                    .result().ifPresent(stock -> { getOffers().clear(); getOffers().addAll(stock); });
        }
    }

    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (site != null) output.putLong("Settlement", site.asLong());
        if (villageKey != null) output.putLong("VillageKey", villageKey);
        if (routine != null) output.store("Routine", CompoundTag.CODEC, routine.save());
        output.putLong("HealReadyAt", healReadyAt);
        output.putLong("HealShoutReadyAt", dialogue.nextHealShout);
        output.store("TradeStock", CompoundTag.CODEC, saveTradeStock());
        NpcAppearanceData.save(this, APPEARANCE, output);
    }

    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        if (isWarden()) equipSilverhideArmor();
        input.getLong("Settlement").ifPresent(value -> {
            BlockPos pos = BlockPos.of(value);
            settlementSpawn(pos, input.getLong("VillageKey").orElse(null), ModernSkin.ASHEN,
                    SettlementPositions.Routine.load(input.read("Routine", CompoundTag.CODEC).orElseGet(CompoundTag::new), pos));
        });
        healReadyAt = input.getLongOr("HealReadyAt", 0);
        dialogue.nextHealShout = input.getLongOr("HealShoutReadyAt", 0);
        input.read("TradeStock", CompoundTag.CODEC).ifPresent(this::restoreTradeStock);
        NpcAppearanceData.load(this, APPEARANCE, input, false);
    }

    private final class HealSquadGoal extends Goal {
        private LivingEntity patient;

        HealSquadGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override public boolean canUse() {
            if (isTrading() || isSleeping() || level().getGameTime() < healReadyAt || (tickCount + getId()) % 10 != 0) return false;
            patient = getHealth() < getMaxHealth() ? SettlementLeaderEntity.this
                    : squad(20).stream().filter(mob -> mob.getHealth() < mob.getMaxHealth())
                    .min(Comparator.comparingDouble(mob -> mob.getHealth() / mob.getMaxHealth())).orElse(null);
            return patient != null;
        }

        @Override public boolean canContinueToUse() {
            return patient != null && patient.isAlive() && patient.getHealth() < patient.getMaxHealth()
                    && distanceToSqr(patient) < 26 * 26 && !isTrading() && level().getGameTime() >= healReadyAt;
        }

        @Override public void stop() { patient = null; }

        @Override public void tick() {
            if (getHealth() < getMaxHealth()) patient = SettlementLeaderEntity.this;
            boolean healingSelf = patient == SettlementLeaderEntity.this;
            if (!healingSelf) {
                getLookControl().setLookAt(patient, 30, 30);
                if (distanceToSqr(patient) > 10 * 10 || !getSensing().hasLineOfSight(patient)) {
                    getNavigation().moveTo(patient, 1.05);
                    return;
                }
            }
            getNavigation().stop();
            var bottle = new SquadHealingPotion(ModEntities.SQUAD_HEALING_POTION.get(), level());
            bottle.setItem(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING));
            bottle.setOwner(SettlementLeaderEntity.this);
            if (healingSelf) {
                bottle.setPos(getX(), getY() + 0.75, getZ());
                bottle.setDeltaMovement(0, -0.55, 0);
            } else {
                bottle.setPos(getX(), getEyeY() - 0.18, getZ());
                Vec3 aim = patient.position().add(patient.getDeltaMovement().scale(2))
                        .add(0, patient.getBbHeight() * 0.55, 0).subtract(bottle.position());
                bottle.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.23, aim.z, 0.76F, 0.7F);
            }
            if (level() instanceof ServerLevel level && level.addFreshEntity(bottle)) {
                playSound(net.minecraft.sounds.SoundEvents.SPLASH_POTION_THROW, 0.9F, 0.9F + random.nextFloat() * 0.2F);
                healReadyAt = level().getGameTime() + 100;
                dialogue.healSquad();
            }
        }
    }

    private final class WardenPatrolGoal extends Goal {
        private Vec3 destination;

        WardenPatrolGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (routine == null || isTrading() || getTarget() != null || (tickCount + getId()) % 20 != 0) return false;
            if (atWorkTime() && getOffers().stream().anyMatch(offer -> offer.getUses() > 0)
                    && !routine.work().closerThan(blockPosition(), 2)) {
                destination = Vec3.atBottomCenterOf(routine.work());
            } else if (!SettlementPositions.isDay(level())) {
                Mob patrol = squad(64).stream().min(Comparator.comparingDouble(SettlementLeaderEntity.this::distanceToSqr)).orElse(null);
                if (patrol != null && distanceToSqr(patrol) > 8 * 8) destination = patrol.position();
                else destination = LandRandomPos.getPos(SettlementLeaderEntity.this, 12, 4);
            } else {
                BlockPos place = switch (random.nextInt(6)) {
                    case 0 -> villageKey == null ? routine.plaza() : BlockPos.of(villageKey).atY(site.getY());
                    case 1 -> routine.tower();
                    case 2 -> routine.home();
                    case 3 -> routine.work();
                    default -> routine.plaza();
                };
                if (place == null || !level().hasChunkAt(place)) return false;
                destination = Vec3.atBottomCenterOf(place);
                if (distanceToSqr(destination) < 3 * 3) destination = LandRandomPos.getPos(SettlementLeaderEntity.this, 8, 3);
            }
            return destination != null;
        }

        @Override public void start() { getNavigation().moveTo(destination.x, destination.y, destination.z, 0.9); }
        @Override public boolean canContinueToUse() { return !isTrading() && getTarget() == null && !getNavigation().isDone(); }
    }
}
