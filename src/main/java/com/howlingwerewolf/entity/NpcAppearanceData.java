package com.howlingwerewolf.entity;

import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.WerewolfModelSet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A persistent coat and a live, server-controlled model family, sent by vanilla entity tracking. */
public final class NpcAppearanceData {
    public record Keys(EntityDataAccessor<Boolean> modern, EntityDataAccessor<Integer> coat) {}

    public static void define(SynchedEntityData.Builder builder, Keys keys) {
        builder.define(keys.modern(), true);
        builder.define(keys.coat(), 0);
    }

    public static void initialize(Entity entity, Keys keys, boolean randomCoat) {
        if (entity.level().isClientSide()) return;
        if (randomCoat) setCoat(entity.getEntityData(), keys,
                ModernSkin.values()[entity.getRandom().nextInt(ModernSkin.values().length)]);
        refresh(entity, keys);
    }

    public static void refresh(Entity entity, Keys keys) {
        // Never consult the local config on the observing client.
        if (!entity.level().isClientSide()) setModelSet(entity.getEntityData(), keys, HWConfig.npcModelSet());
    }

    public static void setModelSet(SynchedEntityData data, Keys keys, WerewolfModelSet set) {
        data.set(keys.modern(), set == WerewolfModelSet.MODERN);
    }

    public static WerewolfModelSet modelSet(SynchedEntityData data, Keys keys) {
        return data.get(keys.modern()) ? WerewolfModelSet.MODERN : WerewolfModelSet.CLASSIC;
    }

    public static void setCoat(SynchedEntityData data, Keys keys, ModernSkin coat) { data.set(keys.coat(), coat.ordinal()); }

    public static ModernSkin coat(SynchedEntityData data, Keys keys) {
        int value = data.get(keys.coat());
        return value >= 0 && value < ModernSkin.values().length ? ModernSkin.values()[value] : ModernSkin.ASHEN;
    }

    public static void save(Entity entity, Keys keys, ValueOutput output) {
        output.putString("ModernCoat", coat(entity.getEntityData(), keys).id());
    }

    public static void load(Entity entity, Keys keys, ValueInput input, boolean fixedAshen) {
        if (entity.level().isClientSide()) return;
        String saved = input.getStringOr("ModernCoat", "");
        if (fixedAshen || !saved.isEmpty()) {
            setCoat(entity.getEntityData(), keys, fixedAshen ? ModernSkin.ASHEN : ModernSkin.byId(saved));
        }
        // The saved coat survives Classic/Modern switches; the current server config owns the family.
        refresh(entity, keys);
    }

    private NpcAppearanceData() {}
}
