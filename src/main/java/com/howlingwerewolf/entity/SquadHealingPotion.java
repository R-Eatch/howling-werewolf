package com.howlingwerewolf.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class SquadHealingPotion extends ThrowableItemProjectile {
    public SquadHealingPotion(EntityType<? extends SquadHealingPotion> type, Level level) { super(type, level); }

    @Override protected Item getDefaultItem() { return Items.SPLASH_POTION; }
    @Override protected double getDefaultGravity() { return 0.05; }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level() instanceof ServerLevel server)) return;
        server.levelEvent(2007, blockPosition(), 0xF82423);
        if (getOwner() instanceof SettlementLeaderEntity leader) {
            for (Mob ally : server.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(3, 1.5, 3),
                    mob -> mob.isAlive() && (mob == leader || leader.supports(mob)) && distanceToSqr(mob) < 9)) {
                double distance = distanceToSqr(ally);
                boolean direct = (ally == leader && distance < 1)
                        || hit instanceof EntityHitResult entityHit && entityHit.getEntity() == ally;
                float healing = direct ? 8 : Math.round(8 * (1 - Math.sqrt(distance) / 3));
                if (healing > 0) {
                    ally.heal(healing);
                    server.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getEyeY() + 0.25, ally.getZ(), 1, 0.18, 0.1, 0.18, 0);
                }
            }
        }
        discard();
    }
}
