package com.howlingwerewolf.entity;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public final class HunterEntity extends PathfinderMob {
    private static final int TRIAL_EFFECT_DURATION = 20 * 60 * 30;
    private boolean trialHunter;
    private boolean villagePatrol;
    private long villageKey;

    public HunterEntity(EntityType<? extends HunterEntity> type, Level level) {
        super(type, level);
        xpReward = 8;
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
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.08D, false));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10,
                true, false, (entity, level) -> isWerewolfTarget(entity)));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
        targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers(HunterEntity.class));
    }

    private static boolean isWerewolfTarget(@Nullable LivingEntity entity) {
        return entity instanceof WerewolfEntity
                || entity instanceof AlphaWerewolfEntity
                || entity instanceof AlphaMinionEntity
                || isWerewolfPlayer(entity);
    }

    private static boolean isWerewolfPlayer(@Nullable LivingEntity entity) {
        return entity instanceof Player player && !player.isCreative() && !player.isSpectator()
                && WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof Player player && isWerewolfPlayer(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 10, 0), this);
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 10, 0), this);
        }
        return hurt;
    }

    @Override
    @SuppressWarnings("deprecation") // NeoForge marks Mob#finalizeSpawn as override-only.
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, groupData);
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
        return trialHunter;
    }

    public void configureForTrial() {
        trialHunter = true;
        setPersistenceRequired();
        setCustomName(Component.translatable("entity.howlingwerewolf.trial_hunter"));
        setCustomNameVisible(true);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SILVER_SWORD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setHealth(getMaxHealth());
        addEffect(new MobEffectInstance(MobEffects.STRENGTH, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.SPEED, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.REGENERATION, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.RESISTANCE, TRIAL_EFFECT_DURATION, 0));
        addEffect(new MobEffectInstance(MobEffects.GLOWING, TRIAL_EFFECT_DURATION, 0));
    }

    /** Village patrol members persist and stay near their assigned village. / 村庄巡猎队保持持久化并驻守所属村庄。 */
    public void configureForVillagePatrol(long villageKey) {
        villagePatrol = true;
        this.villageKey = villageKey;
        setHomeTo(BlockPos.of(villageKey), 48);
        setPersistenceRequired();
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SILVER_SWORD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public boolean belongsToVillage(long villageKey) {
        return villagePatrol && this.villageKey == villageKey;
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, net.minecraft.world.damagesource.DamageSource source,
                                     boolean recentlyHit) {
        if (!trialHunter) super.dropFromLootTable(level, source, recentlyHit);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(level, ModBlocks.WOLFSBANE.get());
        if (random.nextFloat() < 0.50F) spawnAtLocation(level, ModBlocks.WOLFSBANE.get());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TrialHunter", trialHunter);
        tag.putBoolean("VillagePatrol", villagePatrol);
        if (villagePatrol) tag.putLong("VillageKey", villageKey);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        trialHunter = tag.getBooleanOr("TrialHunter", false);
        villagePatrol = tag.getBooleanOr("VillagePatrol", false);
        villageKey = tag.getLongOr("VillageKey", 0L);
        if (villagePatrol) setHomeTo(BlockPos.of(villageKey), 48);
    }
}
