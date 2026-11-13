package com.howlingwerewolf.content;

import com.howlingwerewolf.capability.WerewolfApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wolfsbane is an irritant to lycanthropes. Contact causes short poisoning and
 * mining fatigue, with a stronger reaction while transformed.
 */
public final class WolfsbaneFlowerBlock extends FlowerBlock {
    public WolfsbaneFlowerBlock(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player && player.tickCount % 20 == 0) {
            WerewolfApi.get(player).ifPresent(data -> {
                if (!data.isWerewolf()) return;
                boolean transformed = data.isTransformed();
                player.hurt(level.damageSources().magic(), transformed ? 1.0F : 0.5F);
                player.addEffect(new MobEffectInstance(MobEffects.POISON,
                        transformed ? 80 : 50, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
                        transformed ? 100 : 60, 0, false, true, true));
            });
        }
        super.entityInside(state, level, pos, entity);
    }
}
