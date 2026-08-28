package com.howlingwerewolf.capability;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Player werewolf capability provider.
 *
 * Important Forge 1.20.1 lifecycle detail:
 * ServerPlayer dimension travel invalidates attached capability listeners and later
 * revives only the outer Entity CapabilityProvider. A LazyOptional owned by this
 * attached provider therefore has to be recreatable after invalidation. Keeping a
 * final, one-shot LazyOptional makes the capability permanently unavailable after
 * the first dimension change.
 *
 * The WerewolfData instance itself is intentionally stable. Recreating the
 * LazyOptional only restores the capability handle; it does not reset or replace
 * the player's werewolf state.
 */
public final class WerewolfProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(HowlingWerewolf.MOD_ID, "werewolf_data");

    private final WerewolfData data = new WerewolfData();
    private LazyOptional<WerewolfData> optional = createOptional();

    private LazyOptional<WerewolfData> createOptional() {
        return LazyOptional.of(() -> data);
    }

    /**
     * Return a live capability handle. Forge may have invalidated the previous
     * LazyOptional during CHANGED_DIMENSION while retaining this provider/data
     * instance on the same ServerPlayer. In that case create a new handle lazily.
     */
    private LazyOptional<WerewolfData> liveOptional() {
        if (!optional.isPresent()) {
            optional = createOptional();
        }
        return optional;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == WerewolfCapabilities.WEREWOLF ? liveOptional().cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }

    /** Called by AttachCapabilitiesEvent's invalidation listener. */
    public void invalidate() {
        optional.invalidate();
    }
}
