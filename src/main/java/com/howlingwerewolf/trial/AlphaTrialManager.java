package com.howlingwerewolf.trial;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.entity.AlphaMinionEntity;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import com.howlingwerewolf.event.WerewolfGuide;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlphaTrialManager {
    public static final double BARRIER_RADIUS = 18.0D;
    public static final BlockPos[] OUTER_OFFSETS = {
            new BlockPos(0, 0, -3), new BlockPos(3, 0, 0),
            new BlockPos(0, 0, 3), new BlockPos(-3, 0, 0)
    };
    private static final int ACTIVATION_TICKS = 20 * 12;
    private static final String PENDING_FAILURE_TAG = "HowlingWerewolfAlphaTrialFailed";
    private static final Map<UUID, TrialLocation> ACTIVE_TRIALS = new ConcurrentHashMap<>();

    public enum FailureReason { OWNER_MISSING, OWNER_LEFT, ALTAR_BROKEN, PEACEFUL }

    public static void tryStart(ServerPlayer player, BlockPos center) {
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) {
            reject(player, "message.howlingwerewolf.trial.overworld_only");
            return;
        }
        long time = Math.floorMod(level.getDayTime(), 24000L);
        if (time < 13000L || time >= 23000L) {
            reject(player, "message.howlingwerewolf.trial.night_only");
            return;
        }
        if (!(level.getBlockEntity(center) instanceof RitualAltarBlockEntity central)
                || !central.isCentral()) return;
        if (central.isTrialActive()) {
            reject(player, "message.howlingwerewolf.trial.already_active");
            return;
        }

        WerewolfData data = WerewolfApi.get(player).resolve().orElse(null);
        if (data == null || !data.isWerewolf() || data.getLevel() < 10) {
            reject(player, "message.howlingwerewolf.trial.level_required");
            return;
        }
        if (data.getForm() != WerewolfForm.WEREWOLF) {
            reject(player, "message.howlingwerewolf.trial.form_required");
            return;
        }
        if (hasOtherPlayerInside(level, center, player)) {
            reject(player, "message.howlingwerewolf.trial.single_player");
            return;
        }

        List<RitualAltarBlockEntity> outerAltars = findValidOuterAltars(level, center);
        if (!central.hasExpectedOffering() || outerAltars.size() != 4) {
            reject(player, "message.howlingwerewolf.trial.structure_invalid");
            return;
        }

        central.consumeOffering();
        outerAltars.forEach(RitualAltarBlockEntity::consumeOffering);
        central.startTrial(player.getUUID());
        ACTIVE_TRIALS.put(player.getUUID(), new TrialLocation(level.dimension(), center.immutable()));
        player.sendSystemMessage(Component.translatable("message.howlingwerewolf.trial.begin")
                .withStyle(ChatFormatting.DARK_PURPLE));
        level.playSound(null, center, SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS, 1.4F, 0.62F);
        level.sendParticles(ParticleTypes.SMOKE, center.getX() + 0.5D, center.getY() + 1.0D,
                center.getZ() + 0.5D, 64, 1.2D, 0.8D, 1.2D, 0.035D);
    }

    public static void tick(RitualAltarBlockEntity altar) {
        if (!(altar.getLevel() instanceof ServerLevel level)) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            fail(altar, FailureReason.PEACEFUL);
            return;
        }
        UUID ownerId = altar.getTrialOwner();
        if (ownerId != null) {
            ACTIVE_TRIALS.put(ownerId, new TrialLocation(level.dimension(), altar.getBlockPos().immutable()));
        }
        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || !owner.isAlive() || owner.serverLevel() != level) {
            fail(altar, FailureReason.OWNER_MISSING);
            return;
        }
        BlockPos center = altar.getBlockPos();
        if (!isStructureIntact(level, center)) {
            fail(altar, FailureReason.ALTAR_BROKEN);
            return;
        }
        double distance = horizontalDistance(owner.position(), Vec3.atCenterOf(center));
        if (distance > BARRIER_RADIUS + 0.5D) {
            fail(altar, FailureReason.OWNER_LEFT);
            return;
        }

        maintainBarrier(level, center, owner, altar);
        if (altar.getTrialPhase() == RitualAltarBlockEntity.PHASE_ACTIVATION) {
            altar.advanceRitualTick();
            renderActivationParticles(level, center, altar.getRitualTicks());
            if (altar.getRitualTicks() >= ACTIVATION_TICKS) spawnHunters(altar, owner);
        } else if (altar.getTrialPhase() == RitualAltarBlockEntity.PHASE_HUNTERS) {
            boolean anyAlive = false;
            for (UUID id : altar.getHunterIds()) {
                Entity entity = level.getEntity(id);
                if (entity instanceof HunterEntity hunter && hunter.isAlive()) {
                    anyAlive = true;
                    break;
                }
            }
            if (!anyAlive) spawnAlpha(altar, owner);
        }
    }

    private static List<RitualAltarBlockEntity> findValidOuterAltars(ServerLevel level, BlockPos center) {
        List<RitualAltarBlockEntity> result = new ArrayList<>();
        for (BlockPos offset : OUTER_OFFSETS) {
            BlockPos pos = center.offset(offset);
            if (!level.getBlockState(pos).is(ModBlocks.RITUAL_ALTAR.get())
                    || !(level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar)
                    || altar.isCentral() || !altar.hasExpectedOffering()) return List.of();
            result.add(altar);
        }
        return result;
    }

    private static boolean isStructureIntact(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center).is(ModBlocks.CENTRAL_RITUAL_ALTAR.get())) return false;
        for (BlockPos offset : OUTER_OFFSETS) {
            if (!level.getBlockState(center.offset(offset)).is(ModBlocks.RITUAL_ALTAR.get())) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasOtherPlayerInside(ServerLevel level, BlockPos center, ServerPlayer owner) {
        Vec3 middle = Vec3.atCenterOf(center);
        AABB arena = new AABB(center).inflate(BARRIER_RADIUS, 8.0D, BARRIER_RADIUS);
        return level.getEntitiesOfClass(ServerPlayer.class, arena, player -> player != owner
                && !player.isSpectator()
                && horizontalDistance(player.position(), middle) < BARRIER_RADIUS).size() > 0;
    }

    private static void renderActivationParticles(ServerLevel level, BlockPos center, int ticks) {
        double x = center.getX() + 0.5D;
        double y = center.getY() + 1.0D;
        double z = center.getZ() + 0.5D;
        if (ticks < 100) {
            if (ticks % 4 == 0) level.sendParticles(ParticleTypes.SMOKE, x, y, z,
                    10, 0.65D, 0.5D, 0.65D, 0.025D);
            return;
        }
        double progress = Math.min(1.0D, (ticks - 100.0D) / 140.0D);
        double radius = 3.0D * (1.0D - progress) + 0.35D;
        double speed = 0.05D + progress * 0.25D;
        for (int i = 0; i < 4; i++) {
            double angle = ticks * speed + i * Math.PI * 0.5D;
            level.sendParticles(ParticleTypes.END_ROD, x + Math.cos(angle) * radius,
                    y + 0.75D + progress * 1.5D, z + Math.sin(angle) * radius,
                    2, 0.05D, 0.05D, 0.05D, 0.01D);
        }
        if (ticks == ACTIVATION_TICKS) {
            for (int height = 0; height < 32; height++) {
                level.sendParticles(ParticleTypes.END_ROD, x, y + height, z,
                        4, 0.12D, 0.3D, 0.12D, 0.01D);
            }
            level.playSound(null, center, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.BLOCKS, 1.7F, 0.52F);
        }
    }

    private static void spawnHunters(RitualAltarBlockEntity altar, ServerPlayer owner) {
        ServerLevel level = (ServerLevel)altar.getLevel();
        BlockPos center = altar.getBlockPos();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            HunterEntity hunter = ModEntities.HUNTER.get().create(level);
            if (hunter == null) continue;
            double angle = Math.PI * 2.0D * i / 7.0D;
            double x = center.getX() + 0.5D + Math.cos(angle) * 8.0D;
            double z = center.getZ() + 0.5D + Math.sin(angle) * 8.0D;
            hunter.moveTo(x, center.getY() + 1.0D, z,
                    (float)Math.toDegrees(angle + Math.PI), 0.0F);
            hunter.configureForTrial();
            hunter.setTarget(owner);
            level.addFreshEntity(hunter);
            ids.add(hunter.getUUID());
            level.sendParticles(ParticleTypes.POOF, x, center.getY() + 1.6D, z,
                    24, 0.5D, 0.8D, 0.5D, 0.07D);
        }
        altar.beginHunterPhase(ids);
        owner.sendSystemMessage(Component.translatable("message.howlingwerewolf.trial.hunters_arrive")
                .withStyle(ChatFormatting.DARK_RED));
        level.playSound(null, center, SoundEvents.ILLUSIONER_PREPARE_MIRROR,
                SoundSource.HOSTILE, 1.5F, 0.65F);
    }

    private static void spawnAlpha(RitualAltarBlockEntity altar, ServerPlayer owner) {
        ServerLevel level = (ServerLevel)altar.getLevel();
        BlockPos center = altar.getBlockPos();
        AlphaWerewolfEntity alpha = ModEntities.ALPHA_WEREWOLF.get().create(level);
        if (alpha == null) {
            fail(altar, FailureReason.ALTAR_BROKEN);
            return;
        }
        alpha.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D,
                owner.getYRot() + 180.0F, 0.0F);
        alpha.configureForTrial(owner.getUUID(), center);
        alpha.setTarget(owner);
        level.addFreshEntity(alpha);
        altar.beginAlphaPhase(alpha.getUUID());
        owner.sendSystemMessage(Component.translatable("message.howlingwerewolf.trial.alpha_arrives")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        level.sendParticles(ParticleTypes.EXPLOSION, center.getX() + 0.5D,
                center.getY() + 2.0D, center.getZ() + 0.5D,
                6, 1.0D, 1.4D, 1.0D, 0.05D);
        level.playSound(null, center, SoundEvents.RAVAGER_ROAR,
                SoundSource.HOSTILE, 2.0F, 0.52F);
    }

    private static void maintainBarrier(ServerLevel level, BlockPos center, ServerPlayer owner,
                                        RitualAltarBlockEntity altar) {
        Vec3 middle = Vec3.atCenterOf(center);
        if (level.getGameTime() % 2L == 0L) {
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.52F, 0.04F, 0.10F), 1.2F);
            for (int i = 0; i < 48; i++) {
                double angle = Math.PI * 2.0D * i / 48.0D;
                double x = middle.x + Math.cos(angle) * BARRIER_RADIUS;
                double z = middle.z + Math.sin(angle) * BARRIER_RADIUS;
                level.sendParticles(dust, x, center.getY() + 0.4D + (i % 5) * 0.55D, z,
                        1, 0.03D, 0.12D, 0.03D, 0.0D);
            }
        }

        double ownerDistance = horizontalDistance(owner.position(), middle);
        if (ownerDistance > BARRIER_RADIUS - 1.25D) pushRadially(owner, middle, false, 1.1D);

        AABB boundaryArea = new AABB(center).inflate(BARRIER_RADIUS + 3.0D, 8.0D,
                BARRIER_RADIUS + 3.0D);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, boundaryArea,
                player -> player != owner && !player.isSpectator())) {
            double distance = horizontalDistance(player.position(), middle);
            if (distance < BARRIER_RADIUS + 1.5D) pushRadially(player, middle, true, 1.25D);
        }
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, boundaryArea,
                entity -> entity instanceof HunterEntity hunter && hunter.isTrialHunter()
                        || entity instanceof AlphaWerewolfEntity
                        || entity instanceof WerewolfEntity werewolf && werewolf.isSummonedByAlpha()
                        || entity instanceof AlphaMinionEntity)) {
            if (horizontalDistance(entity.position(), middle) > BARRIER_RADIUS - 1.0D) {
                pushRadially(entity, middle, false, 1.0D);
            }
        }
    }

    private static void pushRadially(LivingEntity entity, Vec3 center, boolean outward, double strength) {
        Vec3 radial = entity.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
        if (radial.lengthSqr() < 0.01D) radial = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 push = radial.normalize().scale(outward ? strength : -strength);
        entity.setDeltaMovement(push.x, 0.35D, push.z);
        entity.hurtMarked = true;
    }

    public static void fail(RitualAltarBlockEntity altar, FailureReason reason) {
        if (!(altar.getLevel() instanceof ServerLevel level) || !altar.isTrialActive()) return;
        UUID ownerId = altar.getTrialOwner();
        if (ownerId != null) ACTIVE_TRIALS.remove(ownerId);
        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner != null) {
            owner.sendSystemMessage(Component.translatable("message.howlingwerewolf.trial.failed")
                    .withStyle(ChatFormatting.DARK_RED));
        }
        // The four Moonbane Pearls remain consumed; only the central badge is returned on failure.
        // 四颗月厄珍珠仍会被消耗，失败时只把中央徽章作为掉落物返还。
        Block.popResource(level, altar.getBlockPos().above(),
                new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get()));
        discardTrialEntities(level, altar);
        level.playSound(null, altar.getBlockPos(), SoundEvents.BEACON_DEACTIVATE,
                SoundSource.BLOCKS, 1.2F, 0.55F);
        altar.resetTrial();
    }

    private static void discardTrialEntities(ServerLevel level, RitualAltarBlockEntity altar) {
        for (UUID id : altar.getHunterIds()) {
            Entity entity = level.getEntity(id);
            if (entity != null) entity.discard();
        }
        if (altar.getAlphaId() != null) {
            Entity alpha = level.getEntity(altar.getAlphaId());
            if (alpha != null) alpha.discard();
        }
        AABB arena = new AABB(altar.getBlockPos()).inflate(BARRIER_RADIUS + 4.0D);
        for (AlphaMinionEntity minion : level.getEntitiesOfClass(AlphaMinionEntity.class, arena)) {
            minion.discard();
        }
        for (WerewolfEntity werewolf : level.getEntitiesOfClass(WerewolfEntity.class, arena,
                WerewolfEntity::isSummonedByAlpha)) {
            werewolf.discard();
        }
    }

    public static void onAlphaDefeated(AlphaWerewolfEntity alpha) {
        if (!(alpha.level() instanceof ServerLevel level) || alpha.getTrialCenter() == null) return;
        if (!(level.getBlockEntity(alpha.getTrialCenter()) instanceof RitualAltarBlockEntity altar)
                || altar.getTrialPhase() != RitualAltarBlockEntity.PHASE_ALPHA
                || !alpha.getUUID().equals(altar.getAlphaId())) return;
        UUID ownerId = altar.getTrialOwner();
        if (ownerId != null) ACTIVE_TRIALS.remove(ownerId);
        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || !owner.isAlive()) {
            fail(altar, FailureReason.OWNER_MISSING);
            return;
        }
        WerewolfApi.get(owner).ifPresent(data -> {
            if (!data.hasDefeatedAlpha()) {
                data.setAlphaDefeated(true);
                giveOrDrop(owner, new ItemStack(ModItems.WEREWOLF_POTION.get()));
                giveOrDrop(owner, new ItemStack(ModItems.MOONBANE_PEARL.get()));
                giveOrDrop(owner, new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get()));
                WerewolfGuide.defeatAlpha(owner);
                owner.sendSystemMessage(Component.translatable(
                                "message.howlingwerewolf.trial.victory_first")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            } else {
                if (level.getDifficulty() == Difficulty.HARD) {
                    data.addBonusSkillPoints(1);
                    data.addBonusTreePoints(1);
                    giveOrDrop(owner, new ItemStack(ModItems.MOONBANE_PEARL.get(), 2));
                    owner.sendSystemMessage(Component.translatable(
                                    "message.howlingwerewolf.trial.victory_repeat_hard")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                } else {
                    giveOrDrop(owner, new ItemStack(ModItems.MOONBANE_PEARL.get()));
                    owner.sendSystemMessage(Component.translatable(
                                    "message.howlingwerewolf.trial.victory_repeat_standard")
                            .withStyle(ChatFormatting.GOLD));
                }
            }
            ModNetwork.sync(owner, data);
        });
        altar.resetTrial();
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, alpha.getX(), alpha.getY() + 1.5D,
                alpha.getZ(), 80, 1.1D, 1.4D, 1.1D, 0.12D);
        level.playSound(null, altar.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 1.3F, 0.82F);
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static double horizontalDistance(Vec3 first, Vec3 second) {
        double x = first.x - second.x;
        double z = first.z - second.z;
        return Math.sqrt(x * x + z * z);
    }

    private static void reject(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
    }

    /** Marks a running oath as failed before the owner leaves the server player list. */
    public static void onPlayerLogout(ServerPlayer player) {
        TrialLocation location = ACTIVE_TRIALS.get(player.getUUID());
        if (location == null) return;
        ServerLevel level = player.getServer().getLevel(location.dimension());
        if (level == null
                || !(level.getBlockEntity(location.center()) instanceof RitualAltarBlockEntity altar)
                || !altar.isTrialActive()
                || !player.getUUID().equals(altar.getTrialOwner())) {
            ACTIVE_TRIALS.remove(player.getUUID());
            return;
        }
        player.getPersistentData().putBoolean(PENDING_FAILURE_TAG, true);
        fail(altar, FailureReason.OWNER_MISSING);
    }

    /** A closed connection cannot display chat, so repeat the unified failure line on login. */
    public static void deliverPendingFailureMessage(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean(PENDING_FAILURE_TAG)) return;
        player.getPersistentData().remove(PENDING_FAILURE_TAG);
        player.sendSystemMessage(Component.translatable("message.howlingwerewolf.trial.failed")
                .withStyle(ChatFormatting.DARK_RED));
    }

    private record TrialLocation(ResourceKey<Level> dimension, BlockPos center) {}

    private AlphaTrialManager() {}
}
