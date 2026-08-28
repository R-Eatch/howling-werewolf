package com.howlingwerewolf.event;

import com.google.common.collect.Multimap;
import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.content.ModTags;
import com.howlingwerewolf.entity.WerewolfEntity;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WerewolfGameplayEvents {
    private static final UUID CLAW_DAMAGE_ID = UUID.fromString("5cba32d2-272f-4a24-b1fb-c388932d0db8");
    private static final UUID WEREWOLF_SPEED_ID = UUID.fromString("d0b906a2-e31e-44db-9665-e1d431452d70");
    private static final UUID KNOCKBACK_RESISTANCE_ID = UUID.fromString("8ce946b7-a8cb-4bba-80e1-fca9ba82cf42");
    private static final UUID BEAST_SPEED_ID = UUID.fromString("e7419e84-8f10-4b61-8e86-62285d0ae9ab");
    private static final UUID QUADRUPED_SPEED_ID = UUID.fromString("23df2c02-d769-40d6-b3d4-2e041c528e2e");
    private static final UUID BEAST_MAX_HEALTH_ID = UUID.fromString("de30fb17-6f01-4db0-8f8c-df1ecab4283d");
    private static final UUID CLAW_BLOCK_REACH_ID = UUID.fromString("217fc7ae-27cf-4f43-b1d2-d48574740755");
    private static final UUID CLAW_ENTITY_REACH_ID = UUID.fromString("acc23f02-b013-45fe-9453-ef0a7e3d20b1");
    private static final int SPIRIT_DURATION_TICKS = 20 * 60 * 4;
    private static final int SPIRIT_COOLDOWN_TICKS = 20 * 60;
    private static final int BLOODY_BITE_COOLDOWN_TICKS = 20 * 60;
    private static final int BLOODY_BITE_EFFECT_TICKS = 20 * 15;
    private static final int MOONBLOOD_REGENERATION_TICKS = 20 * 45;
    private static final int MOONBLOOD_ABSORPTION_TICKS = 20 * 60;
    private static final int MOONBLOOD_RESISTANCE_I_TICKS = 20 * 60;
    private static final int MOONBLOOD_RESISTANCE_II_TICKS = 20 * 25;
    private static final int MOONBLOOD_RESISTANCE_III_TICKS = 20 * 10;
    private static final int MOONBLOOD_SURGE_TICKS = 20 * 90;
    private static final int MOONBLOOD_CRASH_TICKS = 20 * 60;
    private static final int MOONBLOOD_COOLDOWN_TICKS = 20 * 60 * 4;
    private static final int BEAST_TRANSITION_HUNGER_TICKS = 20 * 60;
    private static final int HUMAN_TO_WEREWOLF_STRENGTH_TICKS = 20 * 30;
    private static final int WEREWOLF_TO_HUMAN_WEAKNESS_TICKS = 20 * 30;
    private static final int WEREWOLF_TO_HUMAN_SLOWNESS_TICKS = 20 * 30;
    private static final int WEREWOLF_TO_HUMAN_MINING_FATIGUE_TICKS = 20 * 15;
    private static final int NIGHT_VISION_DURATION_TICKS = 20 * 20;
    private static final int NIGHT_VISION_REFRESH_INTERVAL_TICKS = 20 * 3;
    private static final int TOTEM_XP_INTERVAL_TICKS = 20 * 60;
    private static final float BEAST_ATTACK_MULTIPLIER = 2.0F;
    private static final float QUADRUPED_ATTACK_MULTIPLIER = 0.75F;
    private static final double QUADRUPED_SPEED_MULTIPLIER = 1.75D;
    private static final double QUADRUPED_JUMP_MULTIPLIER = 1.50D;
    private static final float BLOODY_BITE_MULTIPLIER = 1.8F;
    private static final double BEAST_DEFENSE_MULTIPLIER = 1.25D;
    private static final float WEREWOLF_FIRE_MULTIPLIER = 1.5F;
    private static final ThreadLocal<Boolean> LONG_CLAW_SPLASH = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> BLOODY_BITE_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<RawClawHit> RAW_CLAW_HIT = new ThreadLocal<>();
    private static final ItemStack[] IRON_CLAW_SPEED_TOOLS = {
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_SHOVEL),
            new ItemStack(Items.IRON_HOE), new ItemStack(Items.SHEARS)
    };
    private static final ItemStack[] IRON_CLAW_HARVEST_TOOLS = {
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_SHOVEL)
    };
    private static final Map<UUID, FoodSnapshot> FOOD_SNAPSHOTS = new HashMap<>();
    private static final Map<UUID, Float> LAST_EXHAUSTION = new HashMap<>();
    private static final Set<UUID> AUTOMATIC_NIGHT_VISION = new HashSet<>();

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) return;
        if (event.phase == TickEvent.Phase.START) {
            beginHungerTracking(player);
            return;
        }
        WerewolfApi.get(player).ifPresent(data -> {
            processAwakeningAndMoon(player, data);
            processMoonbloodCrash(player, data);
            refreshWerewolfModifiers(player, data);
            processFormRules(player, data);
            processTotemExperience(player, data);
            processSpiritWolves(player, data);
            if (data.getLevel() >= WerewolfData.getMaxLevel() && player.tickCount % 20 == 0) {
                WerewolfGuide.lastAlpha(player);
            }
        });
        finishHungerTracking(player);
    }

    private static void processFormRules(ServerPlayer player, WerewolfData data) {
        if (!data.isTransformed()) {
            clearAutomaticNightVision(player);
            removeSpiritWolves(player, data);
            return;
        }
        if (player.tickCount % 50 == 0) {
            int regeneration = data.getTreeSkillRank(WerewolfTreeSkill.REGENERATION);
            if (regeneration > 0 && player.getHealth() < player.getMaxHealth()) player.heal(regeneration * 0.5F);
        }
        if (player.tickCount % NIGHT_VISION_REFRESH_INTERVAL_TICKS == 0) {
            if (data.hasAbility(WerewolfAbility.NIGHT_VISION) && data.isNightVisionEnabled()) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
                        NIGHT_VISION_DURATION_TICKS, 0, true, false, false));
                AUTOMATIC_NIGHT_VISION.add(player.getUUID());
            } else {
                clearAutomaticNightVision(player);
            }
        }
        removeResistedEffects(player, data.getTreeSkillRank(WerewolfTreeSkill.RESISTANCE));
        if (data.hasAbility(WerewolfAbility.EMPTY_CLAW_SLOT)) enforceEmptyClawSlot(player, data.getClawHotbarSlot());
        enforceFormEquipmentRules(player, data);
    }

    private static void processAwakeningAndMoon(ServerPlayer player, WerewolfData data) {
        ServerLevel overworld = player.getServer().overworld();
        long absoluteDayTime = overworld.getDayTime();
        long timeOfDay = Math.floorMod(absoluteDayTime, 24000L);
        boolean night = timeOfDay >= 13000L && timeOfDay < 23000L;
        boolean inOverworld = player.serverLevel().dimension() == Level.OVERWORLD;
        boolean fullMoon = inOverworld && night && overworld.getMoonPhase() == 0;

        if (data.isInfected() && night && data.getAwakeningDayTime() >= 0L
                && absoluteDayTime >= data.getAwakeningDayTime()) {
            data.setInfected(false);
            data.setWerewolf(true);
            data.setTransformed(true);
            data.setMoonForced(fullMoon);
            applyHumanWerewolfTransitionEffects(player, data, true);
            playTransformation(player, true);
            player.refreshDimensions();
            player.sendSystemMessage(Component.translatable("message.howlingwerewolf.awakened").withStyle(ChatFormatting.DARK_PURPLE));
            ModNetwork.sync(player, data);
        }

        if (!data.isWerewolf() || player.tickCount % 20 != 0) return;
        if (!inOverworld) {
            // Leaving the Overworld removes the lunar lock without changing the player's current form.
            if (data.isMoonForced()) {
                data.setMoonForced(false);
                ModNetwork.sync(player, data);
            }
            return;
        }
        if (fullMoon) WerewolfGuide.firstFullMoon(player);
        if (fullMoon && !data.isTransformed()) {
            data.setTransformed(true);
            data.setMoonForced(true);
            applyHumanWerewolfTransitionEffects(player, data, true);
            playTransformation(player, true);
            player.refreshDimensions();
            player.sendSystemMessage(Component.translatable("message.howlingwerewolf.full_moon").withStyle(ChatFormatting.RED));
            ModNetwork.sync(player, data);
        } else if (fullMoon && !data.isMoonForced()) {
            data.setMoonForced(true);
            ModNetwork.sync(player, data);
        } else if (!fullMoon && data.isMoonForced()) {
            data.setMoonForced(false);
            data.setTransformed(false);
            applyHumanWerewolfTransitionEffects(player, data, false);
            removeSpiritWolves(player, data);
            playTransformation(player, false);
            player.refreshDimensions();
            player.sendSystemMessage(Component.translatable("message.howlingwerewolf.moon_sets").withStyle(ChatFormatting.GRAY));
            ModNetwork.sync(player, data);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beastVoidMeleeDamage(LivingAttackEvent event) {
        if (LONG_CLAW_SPLASH.get() || BLOODY_BITE_DAMAGE.get() || !HWConfig.BEAST_VOID_DAMAGE.get()
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)) return;
        WerewolfApi.get(attacker).ifPresent(data -> {
            if (!data.isTransformed() || !data.isBeastMode()) return;
            // Convert at the attack entrance. Recursing from LivingHurt happens in the middle of
            // damage processing and can be rejected or partially overwritten by the outer hit.
            event.setCanceled(true);
            Holder<DamageType> voidType = attacker.level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD);
            float damage = event.getAmount() * BEAST_ATTACK_MULTIPLIER
                    + moonRendDamage(attacker, event.getEntity(), data);
            event.getEntity().hurt(new DamageSource(voidType, attacker, attacker), damage);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void captureRawClawDamage(LivingAttackEvent event) {
        if (LONG_CLAW_SPLASH.get() || BLOODY_BITE_DAMAGE.get()
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || event.getEntity() == attacker || !attacker.getMainHandItem().isEmpty()) return;
        boolean melee = event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD);
        if (!melee) return;
        WerewolfApi.get(attacker).ifPresent(data -> {
            if (!data.isTransformed() || !data.hasAbility(WerewolfAbility.LONG_CLAWS)) return;
            float rawAmount = event.getAmount();
            if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) && data.isBeastMode()
                    && HWConfig.BEAST_VOID_DAMAGE.get()) {
                // The recursively-created void hit already includes Moonrend. Long Claws keeps
                // using only the primary claw's ordinary raw damage, matching non-void forms.
                rawAmount = Math.max(0.0F,
                        rawAmount - moonRendDamage(attacker, event.getEntity(), data));
            }
            if (event.getSource().is(DamageTypes.PLAYER_ATTACK) && data.isBeastMode()
                    && !HWConfig.BEAST_VOID_DAMAGE.get()) {
                rawAmount *= BEAST_ATTACK_MULTIPLIER;
            } else if (event.getSource().is(DamageTypes.PLAYER_ATTACK) && data.isQuadrupedMode()) {
                rawAmount *= QUADRUPED_ATTACK_MULTIPLIER;
            }
            RAW_CLAW_HIT.set(new RawClawHit(attacker.getId(), event.getEntity().getId(), rawAmount));
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void formMeleeDamage(LivingHurtEvent event) {
        if (LONG_CLAW_SPLASH.get() || BLOODY_BITE_DAMAGE.get()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)) return;
        WerewolfApi.get(attacker).ifPresent(data -> {
            if (!data.isTransformed()) return;
            if (data.isBeastMode() && HWConfig.BEAST_VOID_DAMAGE.get()) return;
            float damage = event.getAmount() * attackMultiplier(data)
                    + moonRendDamage(attacker, event.getEntity(), data);
            event.setAmount(damage);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void livingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        WerewolfApi.get(victim).ifPresent(data -> {
            Entity infectionSource = event.getSource().getEntity();
            double infectionChance = infectionSource instanceof Wolf wolf && !wolf.isTame()
                    ? HWConfig.WOLF_INFECTION_CHANCE.get()
                    : infectionSource instanceof WerewolfEntity werewolf && !werewolf.isSummonedByAlpha()
                    ? HWConfig.FERAL_WEREWOLF_INFECTION_CHANCE.get() : -1.0D;
            if (!data.isWerewolf() && !data.isInfected() && event.getAmount() > 0.0F
                    && infectionChance >= 0.0D
                    && victim.getRandom().nextDouble() < infectionChance) {
                infect(victim, data);
                victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                victim.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5F, 1.0F);
                victim.sendSystemMessage(Component.translatable("message.howlingwerewolf.infected").withStyle(ChatFormatting.LIGHT_PURPLE));
                ModNetwork.sync(victim, data);
            }
            if (!data.isWerewolf() || !data.isTransformed()) return;
            float amount = event.getAmount();
            if (event.getSource().is(DamageTypeTags.IS_FIRE)) amount *= WEREWOLF_FIRE_MULTIPLIER;
            if (event.getSource().getEntity() instanceof LivingEntity attacker
                    && attacker.getMainHandItem().is(ModTags.SILVER_WEAPONS)) {
                amount *= 1.5F;
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            }
            if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                double rawReduction = data.getLevel() * 0.02D
                        + data.getTreeSkillRank(WerewolfTreeSkill.DEFENSE) * 0.08D;
                int armorPieces = data.getForm() == WerewolfForm.WEREWOLF
                        && data.hasAbility(WerewolfAbility.ARMORED_INSTINCT)
                        ? countArmorBearingPieces(victim) : 0;
                double armorSuppression = Math.max(0.0D, 1.0D - armorPieces * 0.25D);
                // Normal biped and quadruped forms share the 90% cap. Beast Form is the
                // deliberate exception and may use its full boosted reduction up to 100%.
                double reductionCap = data.isBeastMode() ? 1.0D : 0.90D;
                double formMultiplier = data.isBeastMode() ? BEAST_DEFENSE_MULTIPLIER : 1.0D;
                double reduction = Math.min(reductionCap,
                        rawReduction * armorSuppression * formMultiplier);
                amount *= (float)(1.0D - reduction);
            }
            if (event.getSource().is(DamageTypes.FALL)) {
                int fallResistance = data.getTreeSkillRank(WerewolfTreeSkill.FALL_RESISTANCE);
                if (fallResistance >= 2) {
                    event.setCanceled(true);
                    return;
                }
                else if (fallResistance == 1) amount *= 0.5F;
            }
        event.setAmount(amount);
        });
    }

    @SubscribeEvent
    public static void lifesteal(LivingDamageEvent event) {
        if (LONG_CLAW_SPLASH.get() || BLOODY_BITE_DAMAGE.get()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker) || event.getEntity() == attacker) return;
        boolean melee = event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD);
        if (!melee || event.getAmount() <= 0.0F) return;
        WerewolfApi.get(attacker).ifPresent(data -> {
            if (!data.isWerewolf() || !data.isTransformed()) return;
            int rank = data.getTreeSkillRank(WerewolfTreeSkill.LIFESTEAL);
            float ratio = rank == 1 ? 0.10F : rank == 2 ? 0.15F : rank >= 3 ? 0.20F : 0.0F;
            if (ratio > 0.0F) attacker.heal(event.getAmount() * ratio);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void longClawSplash(LivingDamageEvent event) {
        if (LONG_CLAW_SPLASH.get() || BLOODY_BITE_DAMAGE.get() || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || event.getEntity() == attacker) return;
        boolean melee = event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD);
        if (!melee || !attacker.getMainHandItem().isEmpty()) return;
        RawClawHit rawHit = RAW_CLAW_HIT.get();
        if (rawHit == null || rawHit.attackerId() != attacker.getId()
                || rawHit.targetId() != event.getEntity().getId()) return;
        RAW_CLAW_HIT.remove();
        WerewolfApi.get(attacker).ifPresent(data -> {
            if (!data.isTransformed() || !data.hasAbility(WerewolfAbility.LONG_CLAWS)) return;
            // The secondary hit starts with half of the primary hit's pre-mitigation
            // damage. Each secondary target then applies its own armor and reductions.
            float splashDamage = rawHit.amount() * 0.5F;
            if (splashDamage <= 0.0F) return;
            LivingEntity primaryTarget = event.getEntity();
            LONG_CLAW_SPLASH.set(true);
            try {
                for (LivingEntity secondary : primaryTarget.level().getEntitiesOfClass(LivingEntity.class,
                        primaryTarget.getBoundingBox().inflate(2.0D, 1.0D, 2.0D), entity ->
                                entity != primaryTarget && entity != attacker && entity.isAlive()
                                        && !attacker.isAlliedTo(entity) && !isOwnedBy(entity, attacker))) {
                    secondary.hurt(event.getSource(), splashDamage);
                }
            } finally {
                LONG_CLAW_SPLASH.set(false);
            }
        });
    }

    @SubscribeEvent
    public static void fireClawAttack(LivingDamageEvent event) {
        if (BLOODY_BITE_DAMAGE.get() || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || event.getEntity() == attacker || !attacker.getMainHandItem().isEmpty()) return;
        boolean melee = event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD);
        if (!melee) return;
        WerewolfApi.get(attacker).ifPresent(data -> {
            if (data.isTransformed() && data.hasAbility(WerewolfAbility.FIRE_CLAWS)) {
                event.getEntity().setSecondsOnFire(8);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventDeathWithBadge(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        WerewolfApi.get(player).ifPresent(data -> {
            ItemStack badge = findAlphaBadgeInHotbar(player);
            if (!data.isWerewolf() || !data.isTransformed() || badge.isEmpty()) return;
            event.setCanceled(true);
            if (!HWConfig.PRESERVE_ALPHA_BADGE_ON_REVIVAL.get()) badge.shrink(1);
            player.setHealth(1.0F);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(),
                    30, 0.5D, 1.0D, 0.5D, 0.1D);
            level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            ModNetwork.showBadgeActivation(player);
        });
    }

    @SubscribeEvent
    public static void prepareClawAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WerewolfApi.get(player).ifPresent(data -> refreshClawDamage(player, data));
        }
    }

    @SubscribeEvent
    public static void livingDeathExperience(LivingDeathEvent event) {
        ServerPlayer player = event.getSource().getEntity() instanceof ServerPlayer directPlayer
                ? directPlayer
                : event.getEntity().getKillCredit() instanceof ServerPlayer creditedPlayer ? creditedPlayer : null;
        if (player == null || event.getEntity() instanceof net.minecraft.world.entity.player.Player
                || event.getEntity() instanceof ArmorStand) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isWerewolf() || !data.isTransformed()) return;
            LivingEntity target = event.getEntity();
            int mastery = data.getTreeSkillRank(WerewolfTreeSkill.HUNTING_MASTERY);
            float multiplier = mastery >= 2 ? 0.75F : mastery == 1 ? 0.60F : 0.50F;
            int amount = Math.max(1, (int)Math.ceil(target.getMaxHealth() * multiplier));
            int levels = data.addScaledExperience(amount);
            if (levels > 0) {
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.level_up",
                        data.getLevel())
                        .withStyle(ChatFormatting.GOLD));
            }
            ModNetwork.sync(player, data);
        });
    }

    @SubscribeEvent
    public static void restrictFood(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItem().isEdible()) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (data.isTransformed() && !data.hasAbility(WerewolfAbility.HARD_LIFE)
                    && !event.getItem().is(ModTags.WEREWOLF_MEAT)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.meat_only")
                        .withStyle(ChatFormatting.RED), true);
            }
        });
    }

    @SubscribeEvent
    public static void restrictCake(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level().getBlockState(event.getPos()).getBlock() instanceof CakeBlock)) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (data.isTransformed() && !data.hasAbility(WerewolfAbility.HARD_LIFE)) {
                event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.meat_only")
                        .withStyle(ChatFormatting.RED), true);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventMaxRankFallImpact(LivingFallEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed()
                    || data.getTreeSkillRank(WerewolfTreeSkill.FALL_RESISTANCE) < 2) return;
            // Cancel before vanilla calculates fall damage or enters the hurt pipeline. This
            // suppresses the hurt sound and camera shake instead of merely reducing damage to 0.
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
            event.setCanceled(true);
            player.fallDistance = 0.0F;
        });
    }

    @SubscribeEvent
    public static void jumpBoost(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed()) return;
            int rank = data.getTreeSkillRank(WerewolfTreeSkill.JUMP);
            double mobilityFactor = armorMobilityFactor(player, data);
            double formMultiplier = data.isBeastMode() ? 1.5D
                    : data.isQuadrupedMode() ? QUADRUPED_JUMP_MULTIPLIER : 1.0D;
            if (rank <= 0 && formMultiplier <= 1.0D) return;
            double desiredHeight = (1.252D + rank * 1.2D * mobilityFactor) * formMultiplier;
            double velocity = velocityForJumpHeight(desiredHeight);
            double bonus = velocity - 0.42D;
            player.setDeltaMovement(player.getDeltaMovement().add(0.0D, bonus, 0.0D));
        });
    }

    @SubscribeEvent
    public static void toolClawBreakSpeed(PlayerEvent.BreakSpeed event) {
        WerewolfApi.get(event.getEntity()).ifPresent(data -> {
            if (!canUseToolClaws(event.getEntity(), data)) return;
            float toolSpeed = bestIronToolSpeed(event.getState());
            if (toolSpeed > 1.0F) {
                event.setNewSpeed(Math.max(event.getNewSpeed(), event.getOriginalSpeed() * toolSpeed));
            }
        });
    }

    @SubscribeEvent
    public static void toolClawHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) return;
        WerewolfApi.get(event.getEntity()).ifPresent(data -> {
            if (!canUseToolClaws(event.getEntity(), data)) return;
            for (ItemStack tool : IRON_CLAW_HARVEST_TOOLS) {
                if (tool.isCorrectToolForDrops(event.getTargetBlock())) {
                    event.setCanHarvest(true);
                    return;
                }
            }
        });
    }

    @SubscribeEvent
    public static void clawLooting(LootingLevelEvent event) {
        DamageSource source = event.getDamageSource();
        if (source == null || !(source.getEntity() instanceof ServerPlayer player)
                || !player.getMainHandItem().isEmpty()) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed()) return;
            int rank = data.getTreeSkillRank(WerewolfTreeSkill.CLAW_EFFICIENCY);
            if (rank > event.getLootingLevel()) event.setLootingLevel(rank);
        });
    }

    @SubscribeEvent
    public static void formSize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (data.isTransformed() && data.isBeastMode()) {
                boolean crouching = event.getPose() == net.minecraft.world.entity.Pose.CROUCHING;
                event.setNewSize(EntityDimensions.scalable(1.0F, crouching ? 2.0F : 3.0F), true);
                event.setNewEyeHeight(crouching ? 1.8F : 2.7F);
            } else if (data.isTransformed() && data.isQuadrupedMode()) {
                event.setNewSize(EntityDimensions.scalable(0.6F, 0.85F), true);
                event.setNewEyeHeight(0.68F);
            }
        });
    }

    public static void requestTransform(ServerPlayer player) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isWerewolf()) return;
            if (data.isMoonForced() && data.isTransformed()) {
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.moon_forced").withStyle(ChatFormatting.RED));
                return;
            }
            boolean transforming = !data.isTransformed();
            data.setTransformed(transforming);
            applyHumanWerewolfTransitionEffects(player, data, transforming);
            if (!transforming) removeSpiritWolves(player, data);
            playTransformation(player, transforming);
            refreshWerewolfModifiers(player, data);
            player.refreshDimensions();
            player.sendSystemMessage(Component.translatable(transforming
                    ? "message.howlingwerewolf.werewolf_on" : "message.howlingwerewolf.human_on")
                    .withStyle(transforming ? ChatFormatting.DARK_PURPLE : ChatFormatting.GRAY));
            ModNetwork.sync(player, data);
        });
    }

    public static void toggleBeastMode(ServerPlayer player) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isWerewolf() || !data.isTransformed()) return;
            if (!data.hasDefeatedAlpha()) {
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.beast_locked")
                        .withStyle(ChatFormatting.DARK_RED));
                return;
            }
            data.setBeastMode(!data.isBeastMode());
            enforceFormEquipmentRules(player, data);
            refreshWerewolfModifiers(player, data);
            player.refreshDimensions();
            playBeastTransformation(player, data.isBeastMode());
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER,
                    BEAST_TRANSITION_HUNGER_TICKS, 0));
            player.sendSystemMessage(Component.translatable(data.isBeastMode()
                            ? "message.howlingwerewolf.beast_on" : "message.howlingwerewolf.beast_off")
                    .withStyle(data.isBeastMode() ? ChatFormatting.DARK_PURPLE : ChatFormatting.GRAY));
            if (data.isBeastMode()) WerewolfGuide.firstBeastTransformation(player);
            ModNetwork.sync(player, data);
        });
    }

    public static void toggleQuadrupedMode(ServerPlayer player) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isWerewolf() || !data.isTransformed()
                    || !data.hasAbility(WerewolfAbility.QUADRUPED_FORM)) return;
            boolean entering = !data.isQuadrupedMode();
            data.setQuadrupedMode(entering);
            enforceFormEquipmentRules(player, data);
            refreshWerewolfModifiers(player, data);
            player.refreshDimensions();
            playQuadrupedTransformation(player, entering);
            player.sendSystemMessage(Component.translatable(entering
                            ? "message.howlingwerewolf.quadruped_on"
                            : "message.howlingwerewolf.quadruped_off")
                    .withStyle(entering ? ChatFormatting.DARK_PURPLE : ChatFormatting.GRAY));
            ModNetwork.sync(player, data);
        });
    }

    public static void toggleNightVision(ServerPlayer player) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.hasAbility(WerewolfAbility.NIGHT_VISION)) return;
            data.setNightVisionEnabled(!data.isNightVisionEnabled());
            if (!data.isNightVisionEnabled()) clearAutomaticNightVision(player);
            player.sendSystemMessage(Component.translatable(data.isNightVisionEnabled()
                    ? "message.howlingwerewolf.night_vision_on" : "message.howlingwerewolf.night_vision_off"));
            ModNetwork.sync(player, data);
        });
    }

    public static void useAbility(ServerPlayer player, WerewolfAbility ability, int targetId) {
        if (ability == WerewolfAbility.SUMMON_WOLF_SPIRIT) summonWolfSpirits(player);
        else if (ability == WerewolfAbility.BLOODY_BITE) bloodyBite(player, targetId);
        else if (ability == WerewolfAbility.MOONBLOOD_SURGE) useMoonbloodSurge(player);
    }

    private static void bloodyBite(ServerPlayer player, int targetId) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed() || !data.hasAbility(WerewolfAbility.BLOODY_BITE)) return;
            if (!player.getMainHandItem().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.bloody_bite_requires_claws")
                        .withStyle(ChatFormatting.GRAY));
                return;
            }
            long gameTime = player.serverLevel().getGameTime();
            if (gameTime < data.getBloodyBiteCooldownEnd()) {
                long seconds = (data.getBloodyBiteCooldownEnd() - gameTime + 19L) / 20L;
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.bloody_bite_cooldown", seconds)
                        .withStyle(ChatFormatting.GRAY));
                return;
            }
            Entity entity = player.serverLevel().getEntity(targetId);
            if (!(entity instanceof LivingEntity target) || !target.isAlive() || target == player
                    || player.isAlliedTo(target) || isOwnedBy(target, player)) {
                sendBloodyBiteTargetError(player);
                return;
            }
            double reach = player.getAttributeValue(ForgeMod.ENTITY_REACH.get());
            double allowedDistance = reach + target.getBbWidth() * 0.5D;
            if (player.distanceToSqr(target) > allowedDistance * allowedDistance || !player.hasLineOfSight(target)) {
                sendBloodyBiteTargetError(player);
                return;
            }

            refreshClawDamage(player, data);
            float clawDamage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            clawDamage *= attackMultiplier(data);
            float biteDamage = clawDamage * BLOODY_BITE_MULTIPLIER;
            DamageSource source;
            if (data.isBeastMode() && HWConfig.BEAST_VOID_DAMAGE.get()) {
                Holder<DamageType> voidType = player.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD);
                source = new DamageSource(voidType, player, player);
            } else {
                source = player.damageSources().playerAttack(player);
            }

            boolean hurt;
            BLOODY_BITE_DAMAGE.set(true);
            try {
                hurt = target.hurt(source, biteDamage);
            } finally {
                BLOODY_BITE_DAMAGE.set(false);
            }
            if (!hurt) return;

            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BLOODY_BITE_EFFECT_TICKS, 1), player);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BLOODY_BITE_EFFECT_TICKS, 2), player);
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, BLOODY_BITE_EFFECT_TICKS, 0), player);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, BLOODY_BITE_EFFECT_TICKS, 0), player);
            data.setBloodyBiteCooldownEnd(gameTime + BLOODY_BITE_COOLDOWN_TICKS);
            player.swing(InteractionHand.MAIN_HAND, true);
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + target.getBbHeight() * 0.6D,
                    target.getZ(), 12, 0.35D, 0.35D, 0.35D, 0.05D);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS, 1.0F, 0.75F);
            ModNetwork.sync(player, data);
        });
    }

    private static void sendBloodyBiteTargetError(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.howlingwerewolf.bloody_bite_no_target")
                .withStyle(ChatFormatting.GRAY));
    }

    private static void summonWolfSpirits(ServerPlayer player) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed() || !data.hasAbility(WerewolfAbility.SUMMON_WOLF_SPIRIT)) return;
            long gameTime = player.serverLevel().getGameTime();
            if (gameTime < data.getWolfSpiritCooldownEnd()) {
                long seconds = (data.getWolfSpiritCooldownEnd() - gameTime + 19L) / 20L;
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.spirit_cooldown", seconds)
                        .withStyle(ChatFormatting.GRAY));
                return;
            }
            removeSpiritWolves(player, data);
            ServerLevel level = player.serverLevel();
            for (int i = 0; i < 5; i++) {
                Wolf wolf = EntityType.WOLF.create(level);
                if (wolf == null) continue;
                double angle = Math.PI * 2.0D * i / 5.0D;
                wolf.moveTo(player.getX() + Math.cos(angle) * 2.0D, player.getY(),
                        player.getZ() + Math.sin(angle) * 2.0D, player.getYRot(), 0.0F);
                wolf.tame(player);
                wolf.setOwnerUUID(player.getUUID());
                wolf.setCollarColor(DyeColor.LIGHT_BLUE);
                wolf.setGlowingTag(true);
                wolf.setPersistenceRequired();
                wolf.setCustomName(Component.translatable("entity.howlingwerewolf.wolf_spirit"));
                level.addFreshEntity(wolf);
                data.getSpiritWolfIds().add(wolf.getUUID());
                level.sendParticles(ParticleTypes.SOUL, wolf.getX(), wolf.getY() + 0.8D, wolf.getZ(),
                        14, 0.3D, 0.5D, 0.3D, 0.03D);
            }
            data.setWolfSpiritExpireTime(gameTime + SPIRIT_DURATION_TICKS);
            data.setWolfSpiritCooldownEnd(gameTime + SPIRIT_COOLDOWN_TICKS);
            level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.8F);
            ModNetwork.sync(player, data);
        });
    }

    private static void useMoonbloodSurge(ServerPlayer player) {
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed() || !data.hasAbility(WerewolfAbility.MOONBLOOD_SURGE)) return;
            long gameTime = player.serverLevel().getGameTime();
            if (gameTime < data.getMoonbloodSurgeCooldownEnd()) {
                long seconds = (data.getMoonbloodSurgeCooldownEnd() - gameTime + 19L) / 20L;
                player.sendSystemMessage(Component.translatable(
                                "message.howlingwerewolf.moonblood_cooldown", seconds)
                        .withStyle(ChatFormatting.GRAY));
                return;
            }

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                    MOONBLOOD_REGENERATION_TICKS, 2));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
                    MOONBLOOD_ABSORPTION_TICKS, 3));
            // Add the resistance stages from weakest/longest to strongest/shortest. Vanilla's
            // hidden-effect chain counts all durations down together, yielding 10s III, 15s II,
            // then 35s I without custom timers. / 从弱到强叠加隐藏效果，得到 10 秒 III、
            // 15 秒 II、35 秒 I 的连续递减抗性。
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    MOONBLOOD_RESISTANCE_I_TICKS, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    MOONBLOOD_RESISTANCE_II_TICKS, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    MOONBLOOD_RESISTANCE_III_TICKS, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                    MOONBLOOD_SURGE_TICKS, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                    MOONBLOOD_SURGE_TICKS, 1));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP,
                    MOONBLOOD_SURGE_TICKS, 0));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                    MOONBLOOD_SURGE_TICKS, 0));
            data.setMoonbloodCrashTime(gameTime + MOONBLOOD_SURGE_TICKS);
            data.setMoonbloodSurgeCooldownEnd(gameTime + MOONBLOOD_COOLDOWN_TICKS);
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0D,
                    player.getZ(), 48, 0.55D, 0.8D, 0.55D, 0.025D);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D,
                    player.getZ(), 18, 0.4D, 0.65D, 0.4D, 0.05D);
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
                    SoundSource.PLAYERS, 1.1F, 0.72F);
            player.sendSystemMessage(Component.translatable("message.howlingwerewolf.moonblood_on")
                    .withStyle(ChatFormatting.DARK_RED));
            ModNetwork.sync(player, data);
        });
    }

    private static void processMoonbloodCrash(ServerPlayer player, WerewolfData data) {
        long crashTime = data.getMoonbloodCrashTime();
        if (crashTime <= 0L || player.serverLevel().getGameTime() < crashTime) return;
        data.setMoonbloodCrashTime(0L);
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                MOONBLOOD_CRASH_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
                MOONBLOOD_CRASH_TICKS, 2));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                MOONBLOOD_CRASH_TICKS, 0));
        removeResistedEffects(player, data.getTreeSkillRank(WerewolfTreeSkill.RESISTANCE));
        player.serverLevel().sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 1.0D,
                player.getZ(), 42, 0.5D, 0.7D, 0.5D, 0.02D);
        player.playNotifySound(SoundEvents.WOLF_WHINE, SoundSource.PLAYERS, 1.0F, 0.65F);
        player.sendSystemMessage(Component.translatable("message.howlingwerewolf.moonblood_crash")
                .withStyle(ChatFormatting.DARK_GRAY));
        ModNetwork.sync(player, data);
    }

    public static void infect(ServerPlayer player, WerewolfData data) {
        data.setWerewolf(false);
        data.setTransformed(false);
        data.setMoonForced(false);
        data.setInfected(true);
        data.setAwakeningDayTime(calculateNextNight(player.getServer().overworld().getDayTime()));
        WerewolfGuide.infection(player);
    }

    public static void awaken(ServerPlayer player, WerewolfData data) {
        data.setInfected(false);
        data.setWerewolf(true);
        data.setTransformed(true);
        data.setMoonForced(false);
        data.setAwakeningDayTime(-1L);
        applyHumanWerewolfTransitionEffects(player, data, true);
        playTransformation(player, true);
        refreshWerewolfModifiers(player, data);
        player.refreshDimensions();
        WerewolfGuide.infection(player);
    }

    public static void refreshWerewolfModifiers(ServerPlayer player, WerewolfData data) {
        if (!data.isTransformed()) {
            removeWerewolfModifiers(player);
            return;
        }
        double mobilityFactor = armorMobilityFactor(player, data);
        setTransientModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), WEREWOLF_SPEED_ID,
                "howlingwerewolf speed", data.getTreeSkillRank(WerewolfTreeSkill.SPEED) * 0.02D
                        * mobilityFactor);
        setTransientModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), BEAST_SPEED_ID,
                "howlingwerewolf beast speed", data.isBeastMode() ? 0.50D : 0.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        setTransientModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), QUADRUPED_SPEED_ID,
                "howlingwerewolf quadruped speed",
                data.isQuadrupedMode() ? QUADRUPED_SPEED_MULTIPLIER - 1.0D : 0.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        setTransientModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_RESISTANCE_ID,
                "howlingwerewolf knockback resistance",
                data.getTreeSkillRank(WerewolfTreeSkill.KNOCKBACK_RESISTANCE) * 0.50D);
        setTransientModifier(player.getAttribute(Attributes.MAX_HEALTH), BEAST_MAX_HEALTH_ID,
                "howlingwerewolf beast max health", data.isBeastMode() ? 20.0D : 0.0D);
        clampHealthToMaximum(player);
        double reach = player.getMainHandItem().isEmpty()
                ? data.getTreeSkillRank(WerewolfTreeSkill.CLAW_EFFICIENCY) * 0.8D : 0.0D;
        setTransientModifier(player.getAttribute(ForgeMod.BLOCK_REACH.get()), CLAW_BLOCK_REACH_ID,
                "howlingwerewolf claw block reach", reach);
        setTransientModifier(player.getAttribute(ForgeMod.ENTITY_REACH.get()), CLAW_ENTITY_REACH_ID,
                "howlingwerewolf claw entity reach", reach);
        refreshClawDamage(player, data);
    }

    private static void refreshClawDamage(ServerPlayer player, WerewolfData data) {
        if (!data.isTransformed() || !player.getMainHandItem().isEmpty()) {
            removeModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), CLAW_DAMAGE_ID);
            return;
        }
        double damage = (4 + data.getLevel() / 5)
                * (1.0D + data.getTreeSkillRank(WerewolfTreeSkill.DAMAGE) * 0.30D);
        setTransientModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), CLAW_DAMAGE_ID,
                "howlingwerewolf claw damage", damage - 1.0D);
    }

    public static void removeWerewolfModifiers(ServerPlayer player) {
        removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), WEREWOLF_SPEED_ID);
        removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), BEAST_SPEED_ID);
        removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), QUADRUPED_SPEED_ID);
        removeModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), CLAW_DAMAGE_ID);
        removeModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_RESISTANCE_ID);
        removeModifier(player.getAttribute(Attributes.MAX_HEALTH), BEAST_MAX_HEALTH_ID);
        removeModifier(player.getAttribute(ForgeMod.BLOCK_REACH.get()), CLAW_BLOCK_REACH_ID);
        removeModifier(player.getAttribute(ForgeMod.ENTITY_REACH.get()), CLAW_ENTITY_REACH_ID);
        clearAutomaticNightVision(player);
        clampHealthToMaximum(player);
    }

    public static void removeSpiritWolves(ServerPlayer player, WerewolfData data) {
        if (data.getSpiritWolfIds().isEmpty()) return;
        for (UUID id : data.getSpiritWolfIds()) {
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity entity = level.getEntity(id);
                if (entity != null) {
                    entity.discard();
                    break;
                }
            }
        }
        data.getSpiritWolfIds().clear();
        data.setWolfSpiritExpireTime(0L);
    }

    public static void clearTransientTracking(net.minecraft.world.entity.player.Player player) {
        if (player instanceof ServerPlayer serverPlayer) clearAutomaticNightVision(serverPlayer);
        UUID id = player.getUUID();
        FOOD_SNAPSHOTS.remove(id);
        LAST_EXHAUSTION.remove(id);
        AUTOMATIC_NIGHT_VISION.remove(id);
    }

    private static void processSpiritWolves(ServerPlayer player, WerewolfData data) {
        if (player.tickCount % 20 != 0 || data.getSpiritWolfIds().isEmpty()) return;
        if (!data.isTransformed() || player.serverLevel().getGameTime() >= data.getWolfSpiritExpireTime()) {
            removeSpiritWolves(player, data);
            ModNetwork.sync(player, data);
        }
    }

    private static void enforceFormEquipmentRules(ServerPlayer player, WerewolfData data) {
        boolean mayWearEquipment = data.getForm() == WerewolfForm.WEREWOLF
                && data.hasAbility(WerewolfAbility.ARMORED_INSTINCT);
        if (mayWearEquipment) return;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            player.setItemSlot(slot, ItemStack.EMPTY);
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
    }

    private static double armorMobilityFactor(net.minecraft.world.entity.player.Player player,
                                               WerewolfData data) {
        if (data.getForm() != WerewolfForm.WEREWOLF
                || !data.hasAbility(WerewolfAbility.ARMORED_INSTINCT)) return 1.0D;
        return Math.max(0.0D, 1.0D - countArmorBearingPieces(player) * 0.175D);
    }

    private static float attackMultiplier(WerewolfData data) {
        if (data.isBeastMode()) return BEAST_ATTACK_MULTIPLIER;
        if (data.isQuadrupedMode()) return QUADRUPED_ATTACK_MULTIPLIER;
        return 1.0F;
    }

    private static float moonRendDamage(ServerPlayer attacker, LivingEntity target, WerewolfData data) {
        if (!attacker.getMainHandItem().isEmpty()) return 0.0F;
        int rank = data.getTreeSkillRank(WerewolfTreeSkill.MOON_REND);
        return rank <= 0 ? 0.0F : target.getMaxHealth() * 0.006F * rank;
    }

    private static void enforceEmptyClawSlot(ServerPlayer player, int reservedSlot) {
        ItemStack stack = player.getInventory().getItem(reservedSlot);
        if (stack.isEmpty()) return;
        if (inventoryCapacityExcludingReserved(player, stack, reservedSlot) < stack.getCount()) return;
        player.getInventory().setItem(reservedSlot, ItemStack.EMPTY);
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && !remaining.isEmpty(); slot++) {
            if (slot == reservedSlot) continue;
            ItemStack existing = player.getInventory().getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (moved > 0) { existing.grow(moved); remaining.shrink(moved); }
            }
        }
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && !remaining.isEmpty(); slot++) {
            if (slot == reservedSlot || !player.getInventory().getItem(slot).isEmpty()) continue;
            player.getInventory().setItem(slot, remaining.copy());
            remaining.setCount(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void preventReservedSlotPickupWhenInventoryIsFull(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed() || !data.hasAbility(WerewolfAbility.EMPTY_CLAW_SLOT)) return;
            ItemStack incoming = event.getItem().getItem();
            int capacity = inventoryCapacityExcludingReserved(player, incoming, data.getClawHotbarSlot());
            if (capacity <= 0) {
                event.setCanceled(true);
            } else if (capacity < incoming.getCount()) {
                // Vanilla would use the reserved claw slot for the remainder. Pick up only
                // what the other 35 slots can hold and leave the rest in the item entity.
                event.setCanceled(true);
                int moved = moveIntoInventoryExcludingReserved(player, incoming,
                        data.getClawHotbarSlot(), capacity);
                if (moved > 0) {
                    var pickedUpItem = incoming.getItem();
                    incoming.shrink(moved);
                    player.take(event.getItem(), moved);
                    player.awardStat(Stats.ITEM_PICKED_UP.get(pickedUpItem), moved);
                    player.onItemPickup(event.getItem());
                    if (incoming.isEmpty()) event.getItem().discard();
                    else event.getItem().setItem(incoming);
                }
            }
        });
    }

    private static int moveIntoInventoryExcludingReserved(ServerPlayer player, ItemStack incoming,
                                                           int reservedSlot, int requested) {
        int remaining = requested;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
            if (slot == reservedSlot) continue;
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, incoming)) continue;
            int moved = Math.min(remaining, Math.min(existing.getMaxStackSize(),
                    player.getInventory().getMaxStackSize()) - existing.getCount());
            if (moved > 0) {
                existing.grow(moved);
                remaining -= moved;
            }
        }
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
            if (slot == reservedSlot || !player.getInventory().getItem(slot).isEmpty()) continue;
            int moved = Math.min(remaining, Math.min(incoming.getMaxStackSize(),
                    player.getInventory().getMaxStackSize()));
            ItemStack inserted = incoming.copy();
            inserted.setCount(moved);
            player.getInventory().setItem(slot, inserted);
            remaining -= moved;
        }
        if (remaining != requested) player.getInventory().setChanged();
        return requested - remaining;
    }

    private static int inventoryCapacityExcludingReserved(ServerPlayer player, ItemStack incoming,
                                                           int reservedSlot) {
        int capacity = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (slot == reservedSlot) continue;
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing.isEmpty()) {
                capacity += incoming.getMaxStackSize();
            } else if (ItemStack.isSameItemSameTags(existing, incoming)) {
                capacity += Math.max(0, Math.min(existing.getMaxStackSize(),
                        player.getInventory().getMaxStackSize()) - existing.getCount());
            }
            if (capacity >= incoming.getCount()) return capacity;
        }
        return capacity;
    }

    private static void beginHungerTracking(ServerPlayer player) {
        UUID id = player.getUUID();
        if (!WerewolfApi.get(player).map(WerewolfData::isWerewolf).orElse(false)) {
            FOOD_SNAPSHOTS.remove(id);
            LAST_EXHAUSTION.remove(id);
            return;
        }
        FoodData food = player.getFoodData();
        float multiplier = hungerMultiplier(player);
        Float previous = LAST_EXHAUSTION.get(id);
        if (previous != null && multiplier > 1.0F) {
            float outsideTick = food.getExhaustionLevel() - previous;
            if (outsideTick > 0.0F) food.addExhaustion(outsideTick * (multiplier - 1.0F));
        }
        FOOD_SNAPSHOTS.put(id, new FoodSnapshot(food.getExhaustionLevel(),
                food.getSaturationLevel(), food.getFoodLevel(), multiplier));
    }

    private static void finishHungerTracking(ServerPlayer player) {
        UUID id = player.getUUID();
        FoodSnapshot start = FOOD_SNAPSHOTS.remove(id);
        if (start == null) {
            LAST_EXHAUSTION.remove(id);
            return;
        }
        FoodData food = player.getFoodData();
        if (start.multiplier > 1.0F) {
            float thresholdUse = Math.max(0.0F, start.saturation - food.getSaturationLevel())
                    + Math.max(0, start.food - food.getFoodLevel());
            float added = food.getExhaustionLevel() - start.exhaustion + thresholdUse * 4.0F;
            if (added > 0.0F) food.addExhaustion(added * (start.multiplier - 1.0F));
        }
        LAST_EXHAUSTION.put(id, food.getExhaustionLevel());
    }

    private static float hungerMultiplier(ServerPlayer player) {
        final float[] result = {1.0F};
        WerewolfApi.get(player).ifPresent(data -> {
            if (!data.isTransformed()) return;
            int rank = data.getTreeSkillRank(WerewolfTreeSkill.SATIETY);
            result[0] = 1.5F - Math.min(3, rank) * (0.5F / 3.0F);
        });
        return result[0];
    }

    private static double velocityForJumpHeight(double desiredHeight) {
        double low = 0.42D;
        double high = 1.5D;
        for (int i = 0; i < 24; i++) {
            double mid = (low + high) * 0.5D;
            if (simulatedJumpHeight(mid) < desiredHeight) low = mid; else high = mid;
        }
        return high;
    }

    private static double simulatedJumpHeight(double initialVelocity) {
        double height = 0.0D;
        double velocity = initialVelocity;
        for (int tick = 0; tick < 80 && velocity > 0.0D; tick++) {
            height += velocity;
            velocity = (velocity - 0.08D) * 0.98D;
        }
        return height;
    }

    private static void setTransientModifier(AttributeInstance instance, UUID id, String name, double amount) {
        setTransientModifier(instance, id, name, amount, AttributeModifier.Operation.ADDITION);
    }

    private static void setTransientModifier(AttributeInstance instance, UUID id, String name, double amount,
                                             AttributeModifier.Operation operation) {
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (existing != null && Math.abs(existing.getAmount() - amount) < 1.0E-6D
                && existing.getOperation() == operation) return;
        if (existing != null) instance.removeModifier(id);
        if (amount != 0.0D) instance.addTransientModifier(
                new AttributeModifier(id, name, amount, operation));
    }

    private static void removeModifier(AttributeInstance instance, UUID id) {
        if (instance != null) instance.removeModifier(id);
    }

    private static void clampHealthToMaximum(ServerPlayer player) {
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void clearAutomaticNightVision(ServerPlayer player) {
        if (!AUTOMATIC_NIGHT_VISION.remove(player.getUUID())) return;
        MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
        if (effect != null && effect.getAmplifier() == 0 && effect.getDuration() <= NIGHT_VISION_DURATION_TICKS
                && effect.isAmbient() && !effect.isVisible() && !effect.showIcon()) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private static int countArmorBearingPieces(net.minecraft.world.entity.player.Player player) {
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
            Collection<AttributeModifier> armorModifiers = modifiers.get(Attributes.ARMOR);
            if (armorModifiers.stream().anyMatch(modifier -> modifier.getAmount() > 0.0D)) count++;
        }
        return count;
    }

    private static ItemStack findAlphaBadgeInHotbar(ServerPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.ALPHA_WEREWOLF_BADGE.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void processTotemExperience(ServerPlayer player, WerewolfData data) {
        if (!data.isWerewolf() || findAlphaBadgeInHotbar(player).isEmpty()) {
            data.setTotemExperienceTicks(0);
            return;
        }
        int ticks = data.getTotemExperienceTicks() + 1;
        if (ticks < TOTEM_XP_INTERVAL_TICKS) {
            data.setTotemExperienceTicks(ticks);
            return;
        }
        data.setTotemExperienceTicks(0);
        boolean gainedExperience = data.getLevel() < WerewolfData.getMaxLevel();
        int levels = data.addScaledExperience(10);
        if (gainedExperience) {
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.45F, 1.25F);
        }
        if (levels > 0) {
            player.sendSystemMessage(Component.translatable("message.howlingwerewolf.level_up",
                    data.getLevel())
                    .withStyle(ChatFormatting.GOLD));
        }
        ModNetwork.sync(player, data);
    }

    private static boolean canUseToolClaws(net.minecraft.world.entity.player.Player player, WerewolfData data) {
        return data.isTransformed() && data.hasAbility(WerewolfAbility.TOOL_CLAWS)
                && player.getMainHandItem().isEmpty();
    }

    private static float bestIronToolSpeed(BlockState state) {
        float speed = 1.0F;
        for (ItemStack tool : IRON_CLAW_SPEED_TOOLS) speed = Math.max(speed, tool.getDestroySpeed(state));
        return speed;
    }

    private static boolean isOwnedBy(LivingEntity entity, ServerPlayer player) {
        return entity instanceof TamableAnimal tameable && player.getUUID().equals(tameable.getOwnerUUID());
    }

    private record RawClawHit(int attackerId, int targetId, float amount) {}

    private static long calculateNextNight(long dayTime) {
        long timeOfDay = Math.floorMod(dayTime, 24000L);
        return timeOfDay < 13000L ? dayTime + (13000L - timeOfDay)
                : dayTime + (24000L - timeOfDay) + 13000L;
    }

    private static void removeResistedEffects(ServerPlayer player, int rank) {
        if (rank >= 1) player.removeEffect(MobEffects.WEAKNESS);
        if (rank >= 2) player.removeEffect(MobEffects.POISON);
        if (rank >= 3) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (rank >= 4) {
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            player.removeEffect(MobEffects.BLINDNESS);
        }
        if (rank >= 5) {
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.DARKNESS);
        }
    }

    private static void applyHumanWerewolfTransitionEffects(ServerPlayer player, WerewolfData data,
                                                             boolean enteringWerewolfForm) {
        if (enteringWerewolfForm) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                    HUMAN_TO_WEREWOLF_STRENGTH_TICKS, 0));
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                WEREWOLF_TO_HUMAN_WEAKNESS_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                WEREWOLF_TO_HUMAN_SLOWNESS_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
                WEREWOLF_TO_HUMAN_MINING_FATIGUE_TICKS, 0));
        int resistance = data.getTreeSkillRank(WerewolfTreeSkill.RESISTANCE);
        if (resistance >= 1) player.removeEffect(MobEffects.WEAKNESS);
        if (resistance >= 3) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (resistance >= 4) player.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    public static void playBeastTransformation(ServerPlayer player, boolean enteringBeastForm) {
        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY() + 1.35D;
        double z = player.getZ();
        if (enteringBeastForm) {
            DustParticleOptions bloodDust = new DustParticleOptions(
                    new Vector3f(0.34F, 0.025F, 0.015F), 1.35F);
            level.sendParticles(bloodDust, x, y, z, 48, 0.5D, 1.05D, 0.5D, 0.035D);
            level.sendParticles(ParticleTypes.POOF, x, y, z, 42, 0.65D, 0.95D, 0.65D, 0.09D);
            level.sendParticles(ParticleTypes.CRIT, x, y + 0.25D, z, 28,
                    0.55D, 0.8D, 0.55D, 0.16D);
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL,
                    SoundSource.PLAYERS, 1.35F, 0.55F);
            level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR,
                    SoundSource.PLAYERS, 0.95F, 0.72F);
        } else {
            level.sendParticles(ParticleTypes.CLOUD, x, y, z, 36,
                    0.55D, 0.9D, 0.55D, 0.045D);
            level.sendParticles(ParticleTypes.POOF, x, y, z, 24,
                    0.45D, 0.75D, 0.45D, 0.035D);
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_WHINE,
                    SoundSource.PLAYERS, 1.0F, 0.82F);
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_AMBIENT,
                    SoundSource.PLAYERS, 0.75F, 0.92F);
        }
    }

    public static void playQuadrupedTransformation(ServerPlayer player, boolean enteringQuadrupedForm) {
        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY() + 0.65D;
        double z = player.getZ();
        level.sendParticles(ParticleTypes.POOF, x, y, z, enteringQuadrupedForm ? 32 : 22,
                0.45D, 0.45D, 0.45D, 0.055D);
        level.sendParticles(enteringQuadrupedForm ? ParticleTypes.CRIT : ParticleTypes.CLOUD,
                x, y, z, enteringQuadrupedForm ? 18 : 14,
                0.35D, 0.35D, 0.35D, 0.07D);
        level.playSound(null, player.blockPosition(), enteringQuadrupedForm
                        ? SoundEvents.WOLF_GROWL : SoundEvents.WOLF_AMBIENT,
                SoundSource.PLAYERS, 1.0F, enteringQuadrupedForm ? 0.78F : 0.96F);
    }

    public static void playTransformation(ServerPlayer player, boolean transforming) {
        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY() + 1.0D;
        double z = player.getZ();
        if (transforming) {
            level.sendParticles(ParticleTypes.POOF, x, y, z, 36, 0.45D, 0.75D, 0.45D, 0.08D);
            level.sendParticles(ParticleTypes.CRIT, x, y + 0.25D, z, 18, 0.35D, 0.55D, 0.35D, 0.12D);
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL,
                    SoundSource.PLAYERS, 1.25F, 0.72F);
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_AMBIENT,
                    SoundSource.PLAYERS, 1.0F, 0.58F);
        } else {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 24, 0.45D, 0.7D, 0.45D, 0.04D);
            level.sendParticles(ParticleTypes.POOF, x, y, z, 18, 0.35D, 0.6D, 0.35D, 0.04D);
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_WHINE,
                    SoundSource.PLAYERS, 0.9F, 1.08F);
        }
    }

    private record FoodSnapshot(float exhaustion, float saturation, int food, float multiplier) {}
    private WerewolfGameplayEvents() {}
}
