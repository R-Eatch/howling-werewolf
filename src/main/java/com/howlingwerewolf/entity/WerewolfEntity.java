package com.howlingwerewolf.entity;

import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Shared biped werewolf entity. Wild werewolves use only the base attributes; Alpha-summoned
 * pack members opt into the trial buffs, attack effects and natural reduction through saved data.
 * 双足狼人共用实体：野生狼人仅使用基础属性，月冠召唤的狼群成员通过持久化标记获得试炼增益、攻击效果与自然减伤。
 */
public final class WerewolfEntity extends Monster {
    private static final int SUMMONED_EFFECT_DURATION = 20 * 60 * 10;
    private static final int SUMMONED_RESISTANCE_DURATION = 20 * 30;

    private boolean summonedByAlpha;
    private UUID alphaOwner;

    public WerewolfEntity(EntityType<? extends WerewolfEntity> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.20D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.16D, true));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.88D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(WerewolfEntity.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, HunterEntity.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10,
                true, false, WerewolfEntity::isAttackablePlayer));
    }

    private static boolean isAttackablePlayer(@Nullable LivingEntity entity) {
        return entity instanceof Player player && !player.isCreative() && !player.isSpectator();
    }

    @Override
    @SuppressWarnings("deprecation") // NeoForge marks Mob#finalizeSpawn as override-only.
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        applyDifficultyDamage(level.getDifficulty());
        setHealth(getMaxHealth());
        return result;
    }

    private void applyDifficultyDamage(Difficulty difficulty) {
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(difficulty == Difficulty.HARD ? 9.0D : 6.0D);
    }

    /** Configure an Alpha ally without changing the wild variant's behavior. / 配置月冠援军，不改变野生变体行为。 */
    public void configureForAlpha(UUID owner, @Nullable LivingEntity target) {
        summonedByAlpha = true;
        alphaOwner = owner;
        xpReward = 0;
        setPersistenceRequired();
        applyDifficultyDamage(level().getDifficulty());
        setHealth(getMaxHealth());
        refreshSummonedEffects();
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                SUMMONED_RESISTANCE_DURATION, 0, true, true, true));
        if (target != null) setTarget(target);
    }

    private void refreshSummonedEffects() {
        if (!summonedByAlpha) return;
        addEffect(new MobEffectInstance(MobEffects.GLOWING, SUMMONED_EFFECT_DURATION, 0,
                true, false, true));
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SUMMONED_EFFECT_DURATION, 0,
                true, true, true));
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, SUMMONED_EFFECT_DURATION, 0,
                true, true, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && summonedByAlpha && tickCount % (20 * 60 * 5) == 0) {
            refreshSummonedEffects();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && summonedByAlpha && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 15, 1), this);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5, 1), this);
        }
        return hurt;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        if (summonedByAlpha
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.90F;
            // NeoForge 1.21 applies damage from the container, not from this method argument.
            DamageContainer damage = damageContainers.peek();
            damage.setNewDamage(amount);
        }
        super.actuallyHurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    public boolean isSummonedByAlpha() {
        return summonedByAlpha;
    }

    @Nullable
    public UUID getAlphaOwner() {
        return alphaOwner;
    }

    @Override
    protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>
            getDefaultLootTable() {
        return summonedByAlpha ? BuiltInLootTables.EMPTY : super.getDefaultLootTable();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SummonedByAlpha", summonedByAlpha);
        if (alphaOwner != null) tag.putUUID("AlphaOwner", alphaOwner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summonedByAlpha = tag.getBoolean("SummonedByAlpha");
        alphaOwner = tag.hasUUID("AlphaOwner") ? tag.getUUID("AlphaOwner") : null;
        xpReward = summonedByAlpha ? 0 : 10;
    }

    /** Hunters recognize transformed players as werewolves too. / 猎人也会把已变身玩家识别为狼人。 */
    public static boolean isTransformedWerewolfPlayer(@Nullable LivingEntity entity) {
        return entity instanceof Player player && !player.isCreative() && !player.isSpectator()
                && WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false);
    }
}
