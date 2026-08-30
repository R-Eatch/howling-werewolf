package com.howlingwerewolf.entity;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.content.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class AlphaWerewolfEntity extends Monster {
    private static final int TELEPORT_COOLDOWN = 20 * 20;
    private static final int REVIVED_TELEPORT_COOLDOWN = 20 * 15;
    private static final int TELEPORT_WARNING = 20;
    private static final int SUMMON_INTERVAL = 20 * 45;
    private static final int SUMMON_CHANNEL = 20 * 5;
    private static final int NATURAL_HEAL_INTERVAL = 50;
    private static final float NATURAL_HEAL_AMOUNT = 4.0F;
    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.translatable("entity.howlingwerewolf.alpha_werewolf"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private UUID trialOwner;
    private BlockPos trialCenter;
    private int nextTeleportTick = TELEPORT_COOLDOWN - TELEPORT_WARNING;
    private int nextSummonTick = SUMMON_INTERVAL;
    private int teleportWarningTicks;
    private int summonTicks;
    private boolean halfHealthSummonTriggered;
    private int pendingForcedSummons;
    private int nextAllowedDamageTick;
    private boolean revived;

    public AlphaWerewolfEntity(EntityType<? extends AlphaWerewolfEntity> type, Level level) {
        super(type, level);
        xpReward = 50;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.70D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new AlphaCombatGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10,
                true, false, entity -> entity instanceof Player player && !player.isCreative()
                && !player.isSpectator()
                && WerewolfApi.get(player).map(data -> data.isWerewolf()).orElse(false)));
    }

    @Override
    @SuppressWarnings("deprecation") // NeoForge marks Mob#finalizeSpawn as override-only.
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        applyDifficultyScaling(level.getDifficulty());
        return result;
    }

    /** Manual ritual spawns do not pass through finalizeSpawn, so scale them explicitly. */
    private void applyDifficultyScaling(Difficulty gameDifficulty) {
        double maxHealth = gameDifficulty == Difficulty.EASY ? 300.0D : 400.0D;
        double attackDamage = gameDifficulty == Difficulty.HARD ? 25.0D
                : gameDifficulty == Difficulty.NORMAL ? 21.0D : 19.0D;
        setAttributeBase(Attributes.MAX_HEALTH, maxHealth);
        setAttributeBase(Attributes.ATTACK_DAMAGE, attackDamage);
        setHealth(getMaxHealth());
        addPermanentCombatEffects();
    }

    private void setAttributeBase(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private void addPermanentCombatEffects() {
        // Strength I before revival, permanently refreshed Strength II afterward.
        // 复活前常驻力量 I，复活后改为持续刷新的常驻力量 II。
        int strengthAmplifier = revived ? 1 : 0;
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                20 * 60 * 30, strengthAmplifier, true, true));
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60 * 30, 0, true, true));
    }

    public void configureForTrial(UUID owner, BlockPos center) {
        trialOwner = owner;
        trialCenter = center.immutable();
        applyDifficultyScaling(level().getDifficulty());
        // The first pack call belongs to the start of the Alpha combat phase.
        // 第一次狼群召唤固定发生在 Alpha 战阶段开始时。
        pendingForcedSummons = 1;
        nextSummonTick = tickCount;
        setCustomName(Component.translatable("entity.howlingwerewolf.alpha_werewolf"));
        setCustomNameVisible(true);
        setPersistenceRequired();
    }

    public UUID getTrialOwner() {
        return trialOwner;
    }

    public BlockPos getTrialCenter() {
        return trialCenter;
    }

    public boolean isSummoning() {
        return summonTicks > 0;
    }

    public boolean hasRevived() {
        return revived;
    }

    private int teleportCooldown() {
        return revived ? REVIVED_TELEPORT_COOLDOWN : TELEPORT_COOLDOWN;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (tickCount % NATURAL_HEAL_INTERVAL == 0 && getHealth() < getMaxHealth()) {
            heal(NATURAL_HEAL_AMOUNT);
        }
        if (tickCount % (20 * 60 * 20) == 0) addPermanentCombatEffects();

        LivingEntity target = getTarget();
        if (!halfHealthSummonTriggered && getHealth() <= getMaxHealth() * 0.50F) {
            halfHealthSummonTriggered = true;
            pendingForcedSummons++;
        }
        if (summonTicks > 0) {
            getNavigation().stop();
            summonTicks--;
            if (tickCount % 5 == 0 && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        getX(), getY() + 1.4D, getZ(), 8, 0.7D, 0.8D, 0.7D, 0.035D);
            }
            if (summonTicks == 0) finishSummoning(target);
            return;
        }
        if (pendingForcedSummons > 0 && tickCount >= nextSummonTick) {
            pendingForcedSummons--;
            beginSummoning();
            return;
        }
        if (target == null || !target.isAlive()) return;
        if (teleportWarningTicks > 0) {
            teleportWarningTicks--;
            if (teleportWarningTicks == 0) {
                performTeleportStrike(target);
                nextTeleportTick = tickCount + teleportCooldown() - TELEPORT_WARNING;
            }
            return;
        }
        if (tickCount >= nextSummonTick) {
            beginSummoning();
            return;
        }
        if (tickCount >= nextTeleportTick) {
            teleportWarningTicks = TELEPORT_WARNING;
            speakToTrialOwner("message.howlingwerewolf.trial.alpha_says_teleport");
        }
    }

    private void beginSummoning() {
        summonTicks = SUMMON_CHANNEL;
        // Periodic and fixed pack calls share one 45-second start-to-start cooldown.
        // 周期召唤与固定召唤共享同一个 45 秒、从吟唱开始计算的冷却。
        nextSummonTick = tickCount + SUMMON_INTERVAL;
        speakToTrialOwner("message.howlingwerewolf.trial.alpha_says_summon");
        level().playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR,
                SoundSource.HOSTILE, 1.4F, 0.62F);
    }

    private void speakToTrialOwner(String key) {
        if (!(level() instanceof ServerLevel serverLevel) || trialOwner == null) return;
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(trialOwner);
        if (owner != null) {
            owner.sendSystemMessage(Component.translatable(key, getDisplayName())
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }
    }

    private void performTeleportStrike(LivingEntity target) {
        Vec3 behind = target.getLookAngle().multiply(-2.0D, 0.0D, -2.0D);
        double x = target.getX() + behind.x;
        double z = target.getZ() + behind.z;
        double y = target.getY();
        teleportTo(x, y, z);
        getLookControl().setLookAt(target, 60.0F, 60.0F);
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        float damage = (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25F;
        if (target.hurt(damageSources().mobAttack(this), damage)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 0), this);
        }
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                    getX(), getY() + 1.2D, getZ(), 36, 0.6D, 1.0D, 0.6D, 0.08D);
        }
        level().playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.2F, 0.72F);
    }

    private void finishSummoning(@Nullable LivingEntity target) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 4; i++) {
            WerewolfEntity minion = ModEntities.WEREWOLF.get().create(serverLevel);
            if (minion == null) continue;
            double angle = Math.PI * 2.0D * i / 4.0D;
            minion.moveTo(getX() + Math.cos(angle) * 2.5D, getY(),
                    getZ() + Math.sin(angle) * 2.5D, getYRot(), 0.0F);
            minion.configureForAlpha(getUUID(), target);
            serverLevel.addFreshEntity(minion);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    minion.getX(), minion.getY() + 0.5D, minion.getZ(),
                    18, 0.35D, 0.4D, 0.35D, 0.06D);
        }
    }

    /** Cancels the first fatal blow on Normal or Hard and enters the Alpha's final combat state. */
    public boolean tryRevive() {
        Difficulty difficulty = level().getDifficulty();
        if (level().isClientSide || revived
                || (difficulty != Difficulty.NORMAL && difficulty != Difficulty.HARD)) return false;
        revived = true;
        float healthRatio = difficulty == Difficulty.HARD ? 0.42F : 0.20F;
        setHealth(getMaxHealth() * healthRatio);
        addPermanentCombatEffects();
        addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 1, true, true));
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60, 0, true, true));
        teleportWarningTicks = 0;
        nextTeleportTick = tickCount + REVIVED_TELEPORT_COOLDOWN - TELEPORT_WARNING;
        pendingForcedSummons++;
        speakToTrialOwner("message.howlingwerewolf.trial.alpha_revives");
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                    getX(), getY() + 1.3D, getZ(), 90, 0.8D, 1.2D, 0.8D, 0.14D);
        }
        level().playSound(null, blockPosition(), SoundEvents.TOTEM_USE,
                SoundSource.HOSTILE, 1.5F, 0.62F);
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        int damageFrequencyLimitTicks = HWConfig.ALPHA_TRIAL_DAMAGE_FREQUENCY_LIMIT_TICKS.get();
        // Normal-form claw attacks and their additive skill damage retain PLAYER_ATTACK and must
        // pass this server-side gate. Only the documented forced-cleanup and Beast void sources bypass it.
        boolean limitedTrialDamage = !level().isClientSide
                && trialOwner != null
                && damageFrequencyLimitTicks > 0
                && !bypassesDamageFrequencyLimit(source);
        if (limitedTrialDamage && tickCount < nextAllowedDamageTick) return false;
        boolean hurt = super.hurt(source, amount);
        if (limitedTrialDamage && hurt) nextAllowedDamageTick = tickCount + damageFrequencyLimitTicks;
        return hurt;
    }

    private boolean bypassesDamageFrequencyLimit(DamageSource source) {
        if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.getEntity() == null) return true;
        if (!source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || !HWConfig.BEAST_VOID_DAMAGE.get()
                || !(source.getEntity() instanceof ServerPlayer attacker)) return false;
        return WerewolfApi.get(attacker)
                .map(data -> data.isTransformed() && data.isBeastMode())
                .orElse(false);
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        // Ordinary damage is reduced by 70%; invulnerability-bypassing damage keeps its full value.
        // 普通伤害固定减免 70%；绕过无敌的伤害保持完整数值。
        if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.30F;
            // Since NeoForge 1.21, LivingEntity#actuallyHurt reads its DamageContainer instead of
            // the method argument. Keep that source of truth in sync with the Forge-era override.
            DamageContainer damage = damageContainers.peek();
            damage.setNewDamage(amount);
        }
        super.actuallyHurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.FALL) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        fallDistance = 0.0F;
        return false;
    }

    @Override
    protected void checkFallDamage(double verticalMovement, boolean onGround,
                                   BlockState landedState, BlockPos landedPosition) {
        // Skip the landing check itself so traversal jumps produce no fall hurt, sound or shake.
        // 直接跳过落地检查，使越障跳跃不产生摔落受伤、音效或镜头晃动。
        fallDistance = 0.0F;
    }

    @Override
    public void die(DamageSource source) {
        // AlphaTrialManager must only see the final death. Revive before calling super.die(),
        // because Forge posts LivingDeathEvent from inside the superclass implementation.
        if (tryRevive()) return;
        com.howlingwerewolf.trial.AlphaTrialManager.onAlphaDefeated(this);
        if (level() instanceof ServerLevel serverLevel) {
            for (WerewolfEntity minion : serverLevel.getEntitiesOfClass(WerewolfEntity.class,
                    getBoundingBox().inflate(32.0D), entity -> getUUID().equals(entity.getAlphaOwner()))) {
                minion.discard();
            }
        }
        super.die(source);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>
            getDefaultLootTable() {
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (trialOwner != null) tag.putUUID("TrialOwner", trialOwner);
        if (trialCenter != null) tag.putLong("TrialCenter", trialCenter.asLong());
        tag.putInt("NextTeleportDelay", Math.max(0, nextTeleportTick - tickCount));
        tag.putInt("NextSummonDelay", Math.max(0, nextSummonTick - tickCount));
        tag.putInt("SummonTicks", summonTicks);
        tag.putInt("TeleportWarningTicks", teleportWarningTicks);
        tag.putBoolean("HalfHealthSummonTriggered", halfHealthSummonTriggered);
        tag.putInt("PendingForcedSummons", pendingForcedSummons);
        tag.putInt("NextAllowedDamageDelay", Math.max(0, nextAllowedDamageTick - tickCount));
        tag.putBoolean("Revived", revived);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        trialOwner = tag.hasUUID("TrialOwner") ? tag.getUUID("TrialOwner") : null;
        trialCenter = tag.contains("TrialCenter") ? BlockPos.of(tag.getLong("TrialCenter")) : null;
        nextTeleportTick = tickCount + Math.max(0, tag.getInt("NextTeleportDelay"));
        nextSummonTick = tickCount + Math.max(0, tag.getInt("NextSummonDelay"));
        summonTicks = Math.max(0, tag.getInt("SummonTicks"));
        teleportWarningTicks = Math.max(0, tag.getInt("TeleportWarningTicks"));
        halfHealthSummonTriggered = tag.getBoolean("HalfHealthSummonTriggered");
        pendingForcedSummons = tag.contains("PendingForcedSummons")
                ? Math.max(0, tag.getInt("PendingForcedSummons"))
                : tag.getBoolean("PendingForcedSummon") ? 1 : 0;
        nextAllowedDamageTick = tickCount + Math.max(0, tag.getInt("NextAllowedDamageDelay"));
        revived = tag.getBoolean("Revived");
    }

    /**
     * A 0.86 vertical impulse clears a four-block wall; the combat goal supplies horizontal
     * momentum when a solid obstacle blocks pursuit. / 0.86 的纵向速度足以越过四格墙，战斗目标在追猎受阻时补充水平动量。
     */
    @Override
    protected float getJumpPower() {
        return 0.86F;
    }

    public boolean tryTraversalJump(Vec3 direction) {
        Vec3 horizontal = direction.multiply(1.0D, 0.0D, 1.0D);
        if (!onGround() || horizontal.lengthSqr() < 0.01D) return false;
        horizontal = horizontal.normalize();
        BlockPos ahead = BlockPos.containing(getX() + horizontal.x * 1.05D,
                getY() + 0.1D, getZ() + horizontal.z * 1.05D);
        boolean obstacle = false;
        for (int y = 0; y < 4; y++) {
            if (!level().getBlockState(ahead.above(y)).getCollisionShape(level(), ahead.above(y)).isEmpty()) {
                obstacle = true;
                break;
            }
        }
        if (!obstacle) return false;
        getJumpControl().jump();
        setDeltaMovement(getDeltaMovement().add(horizontal.x * 0.42D, 0.0D,
                horizontal.z * 0.42D));
        hasImpulse = true;
        return true;
    }
}
