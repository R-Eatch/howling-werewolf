package com.howlingwerewolf.entity;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public final class HunterEntity extends PathfinderMob {
    private static final int TRIAL_EFFECT_DURATION = 20 * 60 * 30;
    private static final String TRIAL_NAME_KEY = "entity.howlingwerewolf.trial_hunter";
    private static final ResourceLocation HUNTER_LOOT = new ResourceLocation(
            HowlingWerewolf.MOD_ID, "entities/hunter");
    /** Tracked so clients can dress the trial's Silver-Oath Watcher; saved as TrialHunter as before. */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> TRIAL_HUNTER =
            net.minecraft.network.syncher.SynchedEntityData.defineId(HunterEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private boolean villagePatrol;
    private long villageKey;
    @Nullable private LivingEntity captainAttacker;
    private long protectUntil;

    public HunterEntity(EntityType<? extends HunterEntity> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TRIAL_HUNTER, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new HunterCrossbowGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.08D, false) {
            @Override public boolean canUse() { return !holdsHunterCrossbow() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !holdsHunterCrossbow() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(5, new VisitTrainingYardGoal());
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new Goal() {
            { setFlags(java.util.EnumSet.of(Flag.TARGET)); }
            @Override public boolean canUse() {
                return captainAttacker != null && captainAttacker.isAlive() && level().getGameTime() < protectUntil
                        && distanceToSqr(captainAttacker) <= 32 * 32 && canAttack(captainAttacker);
            }
            @Override public void start() { setTarget(captainAttacker); }
            @Override public void tick() { if (getTarget() != captainAttacker) setTarget(captainAttacker); }
            @Override public void stop() {
                if (getTarget() == captainAttacker) setTarget(null);
                captainAttacker = null;
            }
        });
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10,
                true, false, HunterEntity::isWerewolfTarget));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                target -> isTrialHunter() ? target instanceof Zombie : isVillageThreat(target)));
        targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers(HunterEntity.class));
    }

    static boolean isWerewolfTarget(@Nullable LivingEntity entity) {
        return entity instanceof WerewolfEntity
                || entity instanceof AlphaWerewolfEntity
                || entity instanceof AlphaMinionEntity
                || entity instanceof TribeWerewolfEntity
                || entity instanceof SettlementLeaderEntity leader && !leader.isWarden()
                || isWerewolfPlayer(entity);
    }

    static boolean isVillageThreat(LivingEntity entity) {
        return entity instanceof Zombie || entity instanceof Raider;
    }

    public boolean holdsHunterCrossbow() {
        return !isTrialHunter() && getMainHandItem().is(ModItems.HUNTER_CROSSBOW.get());
    }

    @Override public ItemStack getProjectile(ItemStack weapon) {
        ItemStack ammunition = super.getProjectile(weapon);
        return weapon.is(ModItems.HUNTER_CROSSBOW.get()) && ammunition.isEmpty() ? new ItemStack(Items.ARROW) : ammunition;
    }

    private static boolean isWerewolfPlayer(@Nullable LivingEntity entity) {
        return entity instanceof Player player && !player.isCreative() && !player.isSpectator()
                && WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof Player player && isWerewolfPlayer(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 0), this);
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * 10, 0), this);
        }
        return hurt;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData groupData,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SILVER_SWORD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return result;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && serverLevel.isVillage(blockPosition()) ? 6 : 3;
    }

    public boolean isTrialHunter() {
        return entityData.get(TRIAL_HUNTER);
    }

    public void configureForTrial() {
        entityData.set(TRIAL_HUNTER, true);
        setPersistenceRequired();
        setCustomName(Component.translatable(TRIAL_NAME_KEY));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SILVER_SWORD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setHealth(getMaxHealth());
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.REGENERATION, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.GLOWING, TRIAL_EFFECT_DURATION, 0));
    }

    /** Village patrol members persist and stay near their assigned village. / 村庄巡猎队保持持久化并驻守所属村庄。 */
    public void configureForVillagePatrol(long villageKey) {
        villagePatrol = true;
        this.villageKey = villageKey;
        restrictTo(BlockPos.of(villageKey), 48);
        setPersistenceRequired();
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextFloat() < 0.3F
                ? ModItems.HUNTER_CROSSBOW.get() : ModItems.SILVER_SWORD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public boolean belongsToVillage(long villageKey) {
        return villagePatrol && this.villageKey == villageKey;
    }

    private final class VisitTrainingYardGoal extends Goal {
        private BlockPos yard;
        private Path route;
        private long nextVisit;
        private long leaveAt;

        VisitTrainingYardGoal() { setFlags(java.util.EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (!villagePatrol || isTrialHunter() || getTarget() != null || !level().isDay()) return false;
            long now = level().getGameTime();
            if (nextVisit == 0) nextVisit = now + 20L * (10 + random.nextInt(31));
            if (now < nextVisit) return false;
            nextVisit = now + 20L * (45 + random.nextInt(61));

            // The saved village key is a structure start in the XZ plane, with Y=0.
            AABB village = new AABB(BlockPos.of(villageKey).atY(getBlockY())).inflate(64, 32, 64);
            for (var warden : level().getEntitiesOfClass(SettlementLeaderEntity.class, village,
                    leader -> leader.isAlive() && leader.isWarden() && leader.supports(HunterEntity.this))) {
                yard = warden.trainingYard();
                if (yard == null || !level().hasChunkAt(yard)) continue;
                route = getNavigation().createPath(yard, 1, 64);
                if (route != null && route.canReach()) return true;
            }
            return false;
        }

        @Override public void start() {
            leaveAt = level().getGameTime() + 400;
            getNavigation().moveTo(route, 0.95);
        }

        @Override public boolean canContinueToUse() {
            return getTarget() == null && level().isDay() && level().getGameTime() < leaveAt
                    && (!getNavigation().isDone() || yard.closerToCenterThan(position(), 5));
        }

        @Override public void tick() {
            if (!getNavigation().isDone() || random.nextInt(reducedTickDelay(50)) != 0) return;
            Vec3 step = LandRandomPos.getPos(HunterEntity.this, 3, 1);
            if (step != null && yard.closerToCenterThan(step, 4)) {
                getNavigation().moveTo(step.x, step.y, step.z, 0.7);
            }
        }

        @Override public void stop() { getNavigation().stop(); }
    }

    void protectCaptain(LivingEntity attacker) {
        if (!villagePatrol || isTrialHunter()) return;
        captainAttacker = attacker;
        protectUntil = level().getGameTime() + 160;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return isTrialHunter() ? net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY : HUNTER_LOOT;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, lootingLevel, recentlyHit);
        if (!isTrialHunter()) return;
        spawnAtLocation(ModBlocks.WOLFSBANE.get());
        if (random.nextFloat() < 0.50F) spawnAtLocation(ModBlocks.WOLFSBANE.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TrialHunter", isTrialHunter());
        tag.putBoolean("VillagePatrol", villagePatrol);
        if (villagePatrol) tag.putLong("VillageKey", villageKey);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(TRIAL_HUNTER, tag.getBoolean("TrialHunter"));
        villagePatrol = tag.getBoolean("VillagePatrol");
        villageKey = tag.getLong("VillageKey");
        if (villagePatrol) restrictTo(BlockPos.of(villageKey), 48);
        // Saves before 1.3.5 pinned the trial name tag; a name-tag rename is left alone.
        if (isTrialHunter() && getCustomName() != null && getCustomName().getContents()
                instanceof net.minecraft.network.chat.contents.TranslatableContents name
                && TRIAL_NAME_KEY.equals(name.getKey())) setCustomNameVisible(false);
    }
}
