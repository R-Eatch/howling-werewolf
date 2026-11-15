package com.howlingwerewolf.entity;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.world.SettlementData;
import com.howlingwerewolf.world.SettlementPositions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public final class TribeWerewolfEntity extends PathfinderMob implements WerewolfNpcAppearance {
    private static final NpcAppearanceData.Keys APPEARANCE = new NpcAppearanceData.Keys(
            SynchedEntityData.defineId(TribeWerewolfEntity.class, EntityDataSerializers.BOOLEAN),
            SynchedEntityData.defineId(TribeWerewolfEntity.class, EntityDataSerializers.INT));
    @Nullable private BlockPos site;
    @Nullable private SettlementPositions.Routine routine;

    public TribeWerewolfEntity(EntityType<? extends TribeWerewolfEntity> type, Level level) {
        super(type, level);
        xpReward = 5;
        setPersistenceRequired();
        NpcAppearanceData.initialize(this, APPEARANCE, false);
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.ARMOR, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ATTACK_DAMAGE, WerewolfEntity.feralAttackDamage(net.minecraft.world.Difficulty.NORMAL))
                .add(Attributes.FOLLOW_RANGE, 30);
    }

    public void settlementSpawn(BlockPos site, ModernSkin coat, SettlementPositions.Routine routine) {
        this.site = site.immutable();
        this.routine = routine;
        restrictTo(site, 48);
        NpcAppearanceData.setCoat(entityData, APPEARANCE, coat);
    }

    public boolean hasSettlement() { return site != null; }
    public boolean belongsTo(BlockPos site) { return site.equals(this.site); }

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
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12, true));
        goalSelector.addGoal(3, new SettlementRoutineGoal(this, () -> routine));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.9));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8) {
            @Override public boolean canUse() { return !isSleeping() && level().isDay() && super.canUse(); }
        });
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 9));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, SettlementCombat::isHunter));
    }

    public void protectChief(LivingEntity attacker) {
        setLastHurtByMob(attacker);
        setTarget(attacker);
    }

    @Override public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null && isSleeping()) stopSleeping();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (site != null && entity instanceof TribeWerewolfEntity resident && resident.belongsTo(site)) return true;
        if (entity instanceof SettlementLeaderEntity leader && leader.supports(this)) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        boolean hurt = super.hurt(source, damage);
        if (hurt && site != null && !level().isClientSide && source.getEntity() instanceof LivingEntity attacker && canAttack(attacker) && !isAlliedTo(attacker)) {
            for (TribeWerewolfEntity resident : level().getEntitiesOfClass(TribeWerewolfEntity.class, getBoundingBox().inflate(16),
                    ally -> ally != this && ally.isAlive() && ally.belongsTo(site) && ally.canAttack(attacker))) {
                resident.protectChief(attacker);
            }
        }
        return hurt;
    }

    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        SettlementRoutineGoal.wakeForActivity(this);
        if (tickCount % 20 == 0) {
            NpcAppearanceData.refresh(this, APPEARANCE);
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(WerewolfEntity.feralAttackDamage(level().getDifficulty()));
        }
    }

    @Override public boolean canUsePortal(boolean ignorePassenger) { return false; }

    @Override public void die(DamageSource source) {
        boolean wasDead = dead;
        super.die(source);
        if (!wasDead && dead && site != null && level() instanceof ServerLevel server) {
            SettlementData.get(server).onDeath(server, site, getUUID(), null);
        }
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (site != null) tag.putLong("Settlement", site.asLong());
        if (routine != null) tag.put("Routine", routine.save());
        NpcAppearanceData.save(this, APPEARANCE, tag);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Settlement")) {
            BlockPos position = BlockPos.of(tag.getLong("Settlement"));
            settlementSpawn(position, ModernSkin.ASHEN, SettlementPositions.Routine.load(tag.getCompound("Routine"), position));
        }
        NpcAppearanceData.load(this, APPEARANCE, tag, false);
    }
}
