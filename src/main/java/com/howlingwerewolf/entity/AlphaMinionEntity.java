package com.howlingwerewolf.entity;

import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class AlphaMinionEntity extends Wolf {
    private UUID alphaOwner;

    public AlphaMinionEntity(EntityType<? extends AlphaMinionEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.9D));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10,
                true, false, entity -> entity instanceof Player player && !player.isCreative()
                && !player.isSpectator()
                && WerewolfApi.get(player).map(data -> data.isWerewolf()).orElse(false)));
    }

    public void configure(UUID owner, @org.jetbrains.annotations.Nullable LivingEntity target) {
        alphaOwner = owner;
        setPersistenceRequired();
        if (target != null) setTarget(target);
        setHealth(getMaxHealth());
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 10, 0));
    }

    public UUID getAlphaOwner() {
        return alphaOwner;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 10, 0), this);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0), this);
        }
        return hurt;
    }

    @Override
    protected net.minecraft.resources.ResourceLocation getDefaultLootTable() {
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (alphaOwner != null) tag.putUUID("AlphaOwner", alphaOwner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        alphaOwner = tag.hasUUID("AlphaOwner") ? tag.getUUID("AlphaOwner") : null;
    }
}
