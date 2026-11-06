/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModEntities;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.trial.AlphaTrialManager;
import com.howlingwerewolf.trial.RitualAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Four wolfsbane offerings converge into a rising silver moon ring, then a visitor answers. */
public final class CompanionRitual {
    public static final int DURATION = 80;
    private static final DustParticleOptions MOON = new DustParticleOptions(0xBCDFFF, 1.0F);
    private static final DustParticleOptions FLOWER = new DustParticleOptions(0x8267CE, 0.9F);

    public static void tryStart(ServerPlayer player, BlockPos center) {
        if (!com.howlingwerewolf.HWConfig.ENABLE_COMPANIONS.get()) { CompanionManager.message(player, "disabled"); return; }
        ServerLevel level = player.level();
        if (!(level.getBlockEntity(center) instanceof RitualAltarBlockEntity altar) || !altar.isCentral()) return;
        if (altar.isTrialActive() || altar.isCompanionRitualActive()) { CompanionManager.message(player, "ritual_busy"); return; }
        List<RitualAltarBlockEntity> outer = new ArrayList<>();
        for (BlockPos offset : AlphaTrialManager.OUTER_OFFSETS) {
            BlockPos pos = center.offset(offset);
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(ModBlocks.RITUAL_ALTAR.get())
                    || !(level.getBlockEntity(pos) instanceof RitualAltarBlockEntity other)
                    || !other.getOffering().is(ModBlocks.WOLFSBANE.get().asItem())) {
                CompanionManager.message(player, "ritual_invalid"); return;
            }
            outer.add(other);
        }
        if (!altar.getOffering().is(ModItems.MOONBANE_PEARL.get())) return;
        altar.consumeOffering();
        outer.forEach(RitualAltarBlockEntity::consumeOffering);
        altar.startCompanionRitual(player.getUUID());
        level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 0.7F);
    }

    public static void tick(RitualAltarBlockEntity altar) {
        if (!com.howlingwerewolf.HWConfig.ENABLE_COMPANIONS.get()) { cancel(altar); return; }
        if (!(altar.getLevel() instanceof ServerLevel level)) return;
        BlockPos center = altar.getBlockPos();
        for (BlockPos offset : AlphaTrialManager.OUTER_OFFSETS) {
            if (!level.hasChunkAt(center.offset(offset))) return; // Pause across chunk boundaries without loading terrain.
            if (!level.getBlockState(center.offset(offset)).is(ModBlocks.RITUAL_ALTAR.get())) { cancel(altar); return; }
        }
        int tick = altar.advanceCompanionRitual();
        double progress = Math.min(1, tick / (double) DURATION);
        if (tick % 2 == 0) {
            for (BlockPos offset : AlphaTrialManager.OUTER_OFFSETS) {
                double spiral = tick * 0.16;
                double x = center.getX() + 0.5 + offset.getX() * (1 - progress);
                double z = center.getZ() + 0.5 + offset.getZ() * (1 - progress);
                level.sendParticles(FLOWER, x, center.getY() + 1 + progress * 1.8, z, 2, 0.06, 0.08, 0.06, 0);
                level.sendParticles(ParticleTypes.ENCHANT, x, center.getY() + 1.1, z, 1,
                        Math.cos(spiral) * 0.2, 0.2, Math.sin(spiral) * 0.2, 0.02);
            }
            for (int ray = 0; ray < 12; ray++) {
                double angle = ray * Math.PI / 6 + tick * 0.06;
                double radius = 0.6 + Math.sin(progress * Math.PI) * 0.8;
                level.sendParticles(MOON, center.getX() + 0.5 + Math.cos(angle) * radius,
                        center.getY() + 0.9 + progress * 2, center.getZ() + 0.5 + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0);
            }
        }
        if (tick == 40) level.playSound(null, center, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.7F, 1.5F);
        if (tick < DURATION) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(altar.getCompanionSummoner());
        if (player == null || player.level() != level || !player.isAlive()) { cancel(altar); return; }
        WerewolfCompanionEntity npc = ModEntities.WEREWOLF_COMPANION.get().create(level, EntitySpawnReason.EVENT);
        if (npc == null) { cancel(altar); return; }
        npc.setSummoner(player);
        boolean walking = false;
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            int radius = 18 + level.getRandom().nextInt(7);
            BlockPos candidate = center.offset((int) Math.round(Math.cos(angle) * radius), 1, (int) Math.round(Math.sin(angle) * radius));
            BlockPos spot = CompanionManager.findSafeSpot(level, npc, candidate, 1);
            if (spot == null || Math.hypot(spot.getX() - center.getX(), spot.getZ() - center.getZ()) < 16) continue;
            npc.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0, 0);
            npc.setOnGround(true);
            if (npc.beginArrival(player, center)) { walking = true; break; }
        }
        if (!walking) {
            npc.arriveAtAltar(center);
            if (!level.noCollision(npc)) {
                BlockPos nearby = CompanionManager.findSafeSpot(level, npc, center.above(), 2);
                if (nearby == null) { cancel(altar); CompanionManager.message(player, "no_space"); return; }
                npc.snapTo(nearby.getX() + 0.5, nearby.getY(), nearby.getZ() + 0.5, 0, 0);
            }
        }
        if (!level.addFreshEntity(npc)) { cancel(altar); return; }
        altar.resetCompanionRitual();
        npc.sayTo(player, "greeting");
        npc.sayTo(player, "stay_notice");
        level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 2, center.getZ() + 0.5, 32, 0.7, 0.7, 0.7, 0.03);
        level.playSound(null, center, SoundEvents.WOLF_SOUNDS.get(net.minecraft.world.entity.animal.wolf.WolfSoundVariants.SoundSet.CLASSIC)
                .ambientSound().value(), SoundSource.NEUTRAL, 0.8F, 0.9F);
    }

    public static void cancel(RitualAltarBlockEntity altar) {
        if (!altar.isCompanionRitualActive() || !(altar.getLevel() instanceof ServerLevel level)) return;
        altar.resetCompanionRitual();
        BlockPos pos = altar.getBlockPos();
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(ModItems.MOONBANE_PEARL.get()));
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(ModBlocks.WOLFSBANE.get().asItem(), 4));
    }
    private CompanionRitual() {}
}
