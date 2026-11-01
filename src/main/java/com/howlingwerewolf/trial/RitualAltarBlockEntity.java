package com.howlingwerewolf.trial;

import com.howlingwerewolf.content.ModBlockEntities;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RitualAltarBlockEntity extends BlockEntity {
    public static final int PHASE_IDLE = 0;
    public static final int PHASE_ACTIVATION = 1;
    public static final int PHASE_HUNTERS = 2;
    public static final int PHASE_ALPHA = 3;

    private ItemStack offering = ItemStack.EMPTY;
    private UUID trialOwner;
    private int trialPhase;
    private int ritualTicks;
    private final List<UUID> hunterIds = new ArrayList<>();
    private UUID alphaId;

    public RitualAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUAL_ALTAR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  RitualAltarBlockEntity altar) {
        if (altar.isCentral() && altar.isTrialActive()) AlphaTrialManager.tick(altar);
    }

    public boolean isCentral() {
        return getBlockState().getBlock() instanceof RitualAltarBlock block && block.isCentral();
    }

    public ItemStack getOffering() {
        return offering;
    }

    public void setOffering(ItemStack stack) {
        offering = stack.copyWithCount(Math.min(1, stack.getCount()));
        changedAndSync();
    }

    public boolean hasExpectedOffering() {
        return isCentral() ? offering.is(ModItems.ALPHA_WEREWOLF_BADGE.get())
                : offering.is(ModItems.MOONBANE_PEARL.get());
    }

    public void consumeOffering() {
        offering = ItemStack.EMPTY;
        changedAndSync();
    }

    public void returnOffering(Player player) {
        if (offering.isEmpty()) return;
        ItemStack returning = offering;
        offering = ItemStack.EMPTY;
        if (!player.getInventory().add(returning)) player.drop(returning, false);
        changedAndSync();
    }

    public void dropOffering() {
        if (offering.isEmpty() || level == null || level.isClientSide()) return;
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.8D, worldPosition.getZ() + 0.5D, offering);
        offering = ItemStack.EMPTY;
    }

    public void startTrial(UUID owner) {
        trialOwner = owner;
        trialPhase = PHASE_ACTIVATION;
        ritualTicks = 0;
        hunterIds.clear();
        alphaId = null;
        changedAndSync();
    }

    public void resetTrial() {
        trialOwner = null;
        trialPhase = PHASE_IDLE;
        ritualTicks = 0;
        hunterIds.clear();
        alphaId = null;
        changedAndSync();
    }

    public boolean isTrialActive() { return trialPhase != PHASE_IDLE; }
    public UUID getTrialOwner() { return trialOwner; }
    public int getTrialPhase() { return trialPhase; }
    public int getRitualTicks() { return ritualTicks; }
    public List<UUID> getHunterIds() { return hunterIds; }
    public UUID getAlphaId() { return alphaId; }

    public void advanceRitualTick() {
        ritualTicks++;
        if (ritualTicks % 5 == 0) changedAndSync();
        else setChanged();
    }

    public void beginHunterPhase(List<UUID> hunters) {
        trialPhase = PHASE_HUNTERS;
        hunterIds.clear();
        hunterIds.addAll(hunters);
        changedAndSync();
    }

    public void beginAlphaPhase(UUID id) {
        trialPhase = PHASE_ALPHA;
        hunterIds.clear();
        alphaId = id;
        changedAndSync();
    }

    private void changedAndSync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            if (isTrialActive()) {
                AlphaTrialManager.fail(this, AlphaTrialManager.FailureReason.ALTAR_BROKEN);
            }
            dropOffering();
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!offering.isEmpty()) output.store("Offering", ItemStack.CODEC, offering);
        output.storeNullable("TrialOwner", UUIDUtil.CODEC, trialOwner);
        output.putInt("TrialPhase", trialPhase);
        output.putInt("RitualTicks", ritualTicks);
        var hunters = output.list("HunterIds", UUIDUtil.STRING_CODEC);
        hunterIds.forEach(hunters::add);
        output.storeNullable("AlphaId", UUIDUtil.CODEC, alphaId);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        offering = input.read("Offering", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        trialOwner = input.read("TrialOwner", UUIDUtil.CODEC).orElse(null);
        trialPhase = input.getIntOr("TrialPhase", PHASE_IDLE);
        ritualTicks = Math.max(0, input.getIntOr("RitualTicks", 0));
        hunterIds.clear();
        input.listOrEmpty("HunterIds", UUIDUtil.STRING_CODEC).forEach(hunterIds::add);
        alphaId = input.read("AlphaId", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        loadWithComponents(input);
    }
}
