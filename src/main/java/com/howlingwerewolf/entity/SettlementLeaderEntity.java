package com.howlingwerewolf.entity;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.world.SettlementData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    private long healReadyAt;
    private long workDay = -1;
    private long nextRestockAt;
    private int restocksToday;

    public SettlementLeaderEntity(EntityType<? extends SettlementLeaderEntity> type, Level level) {
        super(type, level);
        xpReward = 8;
        setPersistenceRequired();
        NpcAppearanceData.initialize(this, APPEARANCE, false);
        if (isWarden()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SILVER_SWORD.get()));
            setDropChance(EquipmentSlot.MAINHAND, 0);
        }
    }

    public static AttributeSupplier.Builder chiefAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 32).add(Attributes.ARMOR, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.29).add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.FOLLOW_RANGE, 36);
    }

    public boolean isWarden() { return getType() == ModEntities.HUNTER_CAPTAIN.get(); }

    public void settlementSpawn(BlockPos site, @Nullable Long villageKey, ModernSkin coat) {
        this.site = site.immutable();
        this.villageKey = villageKey;
        NpcAppearanceData.setCoat(entityData, APPEARANCE, coat);
        restrictTo(villageKey == null ? site : BlockPos.of(villageKey).atY(site.getY()), isWarden() ? 64 : 32);
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
        goalSelector.addGoal(2, new HealSquadGoal());
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.08, true));
        goalSelector.addGoal(5, new FollowPatrolGoal());
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (isWarden()) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, HunterEntity::isWerewolfTarget));
            targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
        }
    }

    public boolean supports(@Nullable Entity entity) {
        if (isWarden()) return villageKey != null && entity instanceof HunterEntity hunter && hunter.belongsToVillage(villageKey);
        return site != null && entity instanceof TribeWerewolfEntity resident && resident.belongsTo(site);
    }

    @Override public boolean isAlliedTo(Entity entity) { return supports(entity) || super.isAlliedTo(entity); }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        boolean hurt = super.hurt(source, damage);
        if (hurt && !level().isClientSide && source.getEntity() instanceof LivingEntity attacker && canAttack(attacker) && !supports(attacker)) {
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
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 20 != 0) return;
        NpcAppearanceData.refresh(this, APPEARANCE);
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            if (isWarden()) {
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50, 0));
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 0));
                // Regeneration I needs at least a full 50-tick effect interval to heal.
                if (!hasEffect(MobEffects.REGENERATION)) addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50, 0));
                addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
            }
            return;
        }
        if (isTrading() || !atWorkTime()) return;
        long day = level().getDayTime() / SettlementData.DAY;
        if (workDay != day) {
            workDay = day;
            restocksToday = 0;
        }
        if (restocksToday >= 2 || level().getGameTime() < nextRestockAt) return;
        MerchantOffers offers = getOffers();
        if (offers.stream().noneMatch(offer -> offer.getUses() > 0)) return;
        if (!site.closerThan(blockPosition(), 3)) return;
        offers.forEach(MerchantOffer::resetUses);
        restocksToday++;
        nextRestockAt = level().getGameTime() + 2400;
    }

    @Override
    public boolean canRestock() { return site != null; }

    private boolean atWorkTime() {
        long hour = Math.floorMod(level().getDayTime(), SettlementData.DAY);
        return site != null && hour >= 2000 && hour < 9000
                && level().getBlockState(site).is(com.howlingwerewolf.content.ModBlocks.SETTLEMENT_MARKER.get());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || getTarget() != null && getTarget().isAlive() || isTrading() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && !getOffers().isEmpty()) {
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override protected void updateTrades() { WerewolfTrades.draw(getOffers(), isWarden() ? WerewolfTrades.MerchantRole.WARDEN : WerewolfTrades.MerchantRole.CHIEF, random); }
    @Override protected void rewardTradeXp(MerchantOffer offer) {}
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
        workDay = tag.contains("WorkDay") ? tag.getLong("WorkDay") : -1;
        nextRestockAt = tag.getLong("NextRestock");
        restocksToday = tag.getInt("Restocks");
        if (tag.contains("Offers")) {
            MerchantOffers.CODEC.parse(registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("Offers"))
                    .result().ifPresent(stock -> { getOffers().clear(); getOffers().addAll(stock); });
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (site != null) tag.putLong("Settlement", site.asLong());
        if (villageKey != null) tag.putLong("VillageKey", villageKey);
        tag.putLong("HealReadyAt", healReadyAt);
        tag.put("TradeStock", saveTradeStock());
        NpcAppearanceData.save(this, APPEARANCE, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Settlement")) settlementSpawn(BlockPos.of(tag.getLong("Settlement")), tag.contains("VillageKey") ? tag.getLong("VillageKey") : null, ModernSkin.ASHEN);
        healReadyAt = tag.getLong("HealReadyAt");
        restoreTradeStock(tag.getCompound("TradeStock"));
        NpcAppearanceData.load(this, APPEARANCE, tag, false);
    }

    private final class HealSquadGoal extends Goal {
        private LivingEntity patient;

        HealSquadGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override public boolean canUse() {
            if (isTrading() || level().getGameTime() < healReadyAt || (tickCount + getId()) % 10 != 0) return false;
            patient = squad(20).stream().filter(mob -> mob.getHealth() <= mob.getMaxHealth() * 0.8F)
                    .min(Comparator.comparingDouble(mob -> mob.getHealth() / mob.getMaxHealth())).orElse(null);
            return patient != null;
        }

        @Override public boolean canContinueToUse() {
            return patient != null && patient.isAlive() && patient.getHealth() < patient.getMaxHealth()
                    && distanceToSqr(patient) < 26 * 26 && !isTrading() && level().getGameTime() >= healReadyAt;
        }

        @Override public void stop() { patient = null; }

        @Override public void tick() {
            getLookControl().setLookAt(patient, 30, 30);
            if (distanceToSqr(patient) > 10 * 10 || !getSensing().hasLineOfSight(patient)) {
                getNavigation().moveTo(patient, 1.05);
                return;
            }
            getNavigation().stop();
            var bottle = new SquadHealingPotion(ModEntities.SQUAD_HEALING_POTION.get(), level());
            bottle.setOwner(SettlementLeaderEntity.this);
            bottle.setPos(getX(), getEyeY() - 0.18, getZ());
            Vec3 aim = patient.position().add(patient.getDeltaMovement().scale(2)).add(0, patient.getBbHeight() * 0.55, 0).subtract(bottle.position());
            bottle.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.23, aim.z, 0.76F, 0.7F);
            if (level().addFreshEntity(bottle)) {
                playSound(net.minecraft.sounds.SoundEvents.SPLASH_POTION_THROW, 0.9F, 0.9F + random.nextFloat() * 0.2F);
                healReadyAt = level().getGameTime() + 200;
            }
        }
    }

    private final class FollowPatrolGoal extends Goal {
        private Vec3 destination;

        FollowPatrolGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (site == null || isTrading() || getTarget() != null || (tickCount + getId()) % 20 != 0) return false;
            boolean headingToWork = atWorkTime() && getOffers().stream().anyMatch(offer -> offer.getUses() > 0);
            if (!site.closerThan(blockPosition(), headingToWork ? 2 : isWarden() ? 48 : 9)) {
                destination = Vec3.atBottomCenterOf(site.above());
                return true;
            }
            if (!isWarden() || headingToWork) return false;
            Mob patrol = squad(32).stream().min(Comparator.comparingDouble(SettlementLeaderEntity.this::distanceToSqr)).orElse(null);
            if (patrol == null || distanceToSqr(patrol) < 10 * 10) return false;
            destination = patrol.position();
            return true;
        }

        @Override public void start() { getNavigation().moveTo(destination.x, destination.y, destination.z, 0.9); }
        @Override public boolean canContinueToUse() { return !isTrading() && getTarget() == null && !getNavigation().isDone(); }
    }
}
