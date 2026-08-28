package com.howlingwerewolf.trial;

import com.howlingwerewolf.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class RitualAltarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    private final boolean central;

    public RitualAltarBlock(Properties properties, boolean central) {
        super(properties);
        this.central = central;
    }

    public boolean isCentral() {
        return central;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.isEmpty() && !altar.getOffering().isEmpty()) {
            if (!level.isClientSide) altar.returnOffering(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        boolean validOffering = central
                ? held.is(ModItems.ALPHA_WEREWOLF_BADGE.get())
                : held.is(ModItems.MOONBANE_PEARL.get());
        if (altar.getOffering().isEmpty() && validOffering) {
            if (!level.isClientSide) {
                altar.setOffering(held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.8F, central ? 0.72F : 1.15F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (central && held.isEmpty() && altar.hasExpectedOffering()
                && player instanceof ServerPlayer serverPlayer && !level.isClientSide) {
            AlphaTrialManager.tryStart(serverPlayer, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState,
                         boolean moving) {
        if (!oldState.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar) {
            if (!level.isClientSide && altar.isTrialActive()) {
                AlphaTrialManager.fail(altar, AlphaTrialManager.FailureReason.ALTAR_BROKEN);
            }
            altar.dropOffering();
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T>
    getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                com.howlingwerewolf.content.ModBlockEntities.RITUAL_ALTAR.get(),
                RitualAltarBlockEntity::serverTick);
    }
}
