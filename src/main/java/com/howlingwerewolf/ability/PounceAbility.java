package com.howlingwerewolf.ability;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.howlingwerewolf.entity.TribeWerewolfEntity;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PounceAbility {
    public static final int COOLDOWN = 15 * 20;
    public static final int MAX_FLIGHT_TICKS = 70;
    private static final int WINDUP = 3;
    private static final ResourceKey<DamageType> IMPACT = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("howlingwerewolf", "pounce"));

    public enum Motion { WINDUP, FLIGHT, LAND, CLEAR }

    public record Flight(ServerLevel level, WerewolfForm form, Vec3 target, long startedAt,
                         boolean launched, boolean airborne, double startY) {}

    public static void start(ServerPlayer player, Vec3 target) {
        WerewolfData data = WerewolfApi.get(player).orElseThrow();
        if (!data.hasAbility(WerewolfAbility.POUNCE) || !data.isTransformed() || !player.onGround() || invalid(player)) return;
        long now = player.level().getGameTime();
        if (data.getPounceFlight() != null || now < data.getPounceCooldownEnd()) {
            long seconds = Math.max(1, (data.getPounceCooldownEnd() - now + 19) / 20);
            player.displayClientMessage(Component.translatable("message.howlingwerewolf.pounce_cooldown", seconds), true);
            return;
        }
        if (PounceTargeting.launchVelocity(player, data.getForm(), target) == null) {
            player.displayClientMessage(Component.translatable("message.howlingwerewolf.pounce_invalid"), true);
            return;
        }
        data.setPounceFlight(new Flight(player.serverLevel(), data.getForm(), target, now, false, false, player.getY()));
        ModNetwork.showPounce(player, Motion.WINDUP);
    }

    public static void tick(ServerPlayer player, WerewolfData data) {
        Flight flight = data.getPounceFlight();
        if (flight == null) return;
        long now = player.level().getGameTime();
        if (invalid(player) || !data.hasAbility(WerewolfAbility.POUNCE) || data.getForm() != flight.form()
                || player.serverLevel() != flight.level() || now - flight.startedAt() > MAX_FLIGHT_TICKS) {
            end(player, data, Motion.CLEAR);
            return;
        }
        if (!flight.launched()) {
            if (!player.onGround()) end(player, data, Motion.CLEAR);
            else if (now >= flight.startedAt() + WINDUP) launch(player, data, flight, now);
            return;
        }
        if (!player.onGround()) {
            if (!flight.airborne() && player.getY() > flight.startY() + 0.125) {
                data.setPounceFlight(new Flight(flight.level(), flight.form(), flight.target(),
                        flight.startedAt(), true, true, flight.startY()));
            }
        } else if (flight.airborne()) {
            end(player, data, Motion.LAND);
            impact(player, data, flight);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100));
        } else if (now - flight.startedAt() > WINDUP + 6) end(player, data, Motion.CLEAR);
    }

    private static void launch(ServerPlayer player, WerewolfData data, Flight flight, long now) {
        Vec3 velocity = PounceTargeting.launchVelocity(player, flight.form(), flight.target());
        if (velocity == null) {
            end(player, data, Motion.CLEAR);
            player.displayClientMessage(Component.translatable("message.howlingwerewolf.pounce_invalid"), true);
            return;
        }
        player.setDeltaMovement(velocity);
        player.currentImpulseImpactPos = player.position();
        player.setIgnoreFallDamageFromCurrentImpulse(true);
        player.setOnGround(false);
        player.hasImpulse = true;
        data.setPounceFlight(new Flight(flight.level(), flight.form(), flight.target(), now, true, false, player.getY()));
        data.setPounceCooldownEnd(now + COOLDOWN);
        ModNetwork.sync(player, data);
        ModNetwork.showPounce(player, Motion.FLIGHT);
        flight.level().playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 0.6F,
                flight.form() == WerewolfForm.BEAST ? 0.65F : 0.95F);
    }

    private static void impact(ServerPlayer player, WerewolfData data, Flight flight) {
        ServerLevel level = flight.level();
        boolean beast = flight.form() == WerewolfForm.BEAST;
        boolean wolf = flight.form() == WerewolfForm.QUADRUPED;
        double radius = PounceTargeting.impactRadius(flight.form());
        float landingDamage = 10.0F * (float) data.clawDamageMultiplier();
        var damage = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(IMPACT), player);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius, 1.25, radius),
                target -> eligible(player, target))) {
            Vec3 horizontal = target.position().subtract(player.position()).multiply(1, 0, 1);
            if (horizontal.lengthSqr() > radius * radius || !player.hasLineOfSight(target)) continue;
            if (wolf) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 64, 0), player);
            } else if (target.hurt(damage, landingDamage) && beast) {
                target.knockback(1.1, -horizontal.x, -horizontal.z);
            }
        }
        var surface = level.getBlockState(player.getOnPos());
        if (!surface.isAir()) level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, surface),
                player.getX(), player.getY() + 0.12, player.getZ(), beast ? 36 : 16, radius * 0.35, 0.12, radius * 0.35, 0.13);
        level.playSound(null, player.blockPosition(), beast ? SoundEvents.ANVIL_LAND : SoundEvents.PLAYER_SMALL_FALL,
                SoundSource.PLAYERS, beast ? 0.48F : 0.68F, beast ? 0.62F : 0.93F);
    }

    private static boolean eligible(ServerPlayer player, LivingEntity target) {
        return target != player && target.isAlive() && !target.isSpectator() && !player.isAlliedTo(target)
                && !(target instanceof Player other && (other.isCreative() || !player.canHarmPlayer(other)))
                && !(target instanceof OwnableEntity ownable && player.getUUID().equals(ownable.getOwnerUUID()))
                && !(target instanceof WerewolfCompanionEntity companion && companion.isRecruited())
                && !(target instanceof TribeWerewolfEntity resident && resident.getTarget() != player)
                && !(target instanceof SettlementLeaderEntity leader && !leader.isWarden() && leader.getTarget() != player);
    }

    public static boolean invalid(Player player) {
        return !player.isAlive() || player.isSpectator() || player.isPassenger() || player.isInWaterOrBubble()
                || player.isInLava() || player.isFallFlying() || player.getAbilities().flying || player.isUsingItem();
    }

    private static void end(ServerPlayer player, WerewolfData data, Motion motion) {
        data.setPounceFlight(null);
        if (motion == Motion.LAND) player.setDeltaMovement(Vec3.ZERO);
        ModNetwork.showPounce(player, motion);
    }

    private PounceAbility() {}
}
