package com.howlingwerewolf.entity;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.world.SettlementData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public final class TribeWerewolfEntity extends PathfinderMob implements WerewolfNpcAppearance {
    private static final NpcAppearanceData.Keys APPEARANCE = new NpcAppearanceData.Keys(
            SynchedEntityData.defineId(TribeWerewolfEntity.class, EntityDataSerializers.BOOLEAN),
            SynchedEntityData.defineId(TribeWerewolfEntity.class, EntityDataSerializers.INT));
    @Nullable private BlockPos site;

    public TribeWerewolfEntity(EntityType<? extends TribeWerewolfEntity> type, Level level) {
        super(type, level);
        xpReward = 5;
        setPersistenceRequired();
        NpcAppearanceData.initialize(this, APPEARANCE, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ARMOR, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 24);
    }

    public void settlementSpawn(BlockPos site, ModernSkin coat) {
        this.site = site.immutable();
        setHomeTo(site, 32);
        NpcAppearanceData.setCoat(entityData, APPEARANCE, coat);
    }

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
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12, true));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.9));
        goalSelector.addGoal(5, new StayNearChiefGoal());
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 9));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public void protectChief(LivingEntity attacker) { setLastHurtByMob(attacker); setTarget(attacker); }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (site != null && target instanceof TribeWerewolfEntity resident && resident.belongsTo(site)) return false;
        if (target instanceof SettlementLeaderEntity leader && leader.supports(this)) return false;
        return super.canAttack(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && site != null && source.getEntity() instanceof LivingEntity attacker && canAttack(attacker) && !isAlliedTo(attacker)) {
            for (TribeWerewolfEntity resident : level().getEntitiesOfClass(TribeWerewolfEntity.class, getBoundingBox().inflate(16),
                    ally -> ally != this && ally.isAlive() && ally.belongsTo(site) && ally.canAttack(attacker))) {
                resident.protectChief(attacker);
            }
        }
        return hurt;
    }

    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (tickCount % 20 == 0) NpcAppearanceData.refresh(this, APPEARANCE);
    }

    @Override public boolean canUsePortal(boolean ignorePassenger) { return false; }

    @Override public void die(DamageSource source) {
        boolean wasDead = dead;
        super.die(source);
        if (!wasDead && dead && site != null && level() instanceof ServerLevel server) {
            SettlementData.get(server).onDeath(server, site, getUUID(), null);
        }
    }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (site != null) output.putLong("Settlement", site.asLong());
        NpcAppearanceData.save(this, APPEARANCE, output);
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Settlement").ifPresent(pos -> settlementSpawn(BlockPos.of(pos), ModernSkin.ASHEN));
        NpcAppearanceData.load(this, APPEARANCE, input, false);
    }

    private final class StayNearChiefGoal extends Goal {
        private SettlementLeaderEntity chief;

        StayNearChiefGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (site == null || getTarget() != null || (tickCount + getId()) % 20 != 0) return false;
            chief = level().getEntitiesOfClass(SettlementLeaderEntity.class, getBoundingBox().inflate(28),
                            leader -> leader.isAlive() && leader.supports(TribeWerewolfEntity.this)).stream()
                    .min(Comparator.comparingDouble(TribeWerewolfEntity.this::distanceToSqr)).orElse(null);
            return chief != null && distanceToSqr(chief) > 10 * 10;
        }

        @Override public void start() { getNavigation().moveTo(chief, 0.88); }
        @Override public boolean canContinueToUse() { return chief.isAlive() && distanceToSqr(chief) > 6 * 6 && !getNavigation().isDone(); }
    }
}
