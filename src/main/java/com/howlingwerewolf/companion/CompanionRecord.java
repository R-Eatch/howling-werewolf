/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.HWConfig;
import com.howlingwerewolf.WerewolfModelSet;
import com.howlingwerewolf.WerewolfSkin;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The owner-bound source of truth; world entities never own a second inventory. */
public final class CompanionRecord {
    private static final Codec<List<ItemStack>> INVENTORY_CODEC = ItemStack.OPTIONAL_CODEC.listOf().validate(items ->
            items.size() == CompanionRules.INVENTORY_SIZE ? DataResult.success(items)
                    : DataResult.error(() -> "A companion inventory must have exactly nine slots"));
    /** The world tick at which the surge ends and the companion's unspent temporary hearts. */
    public record Moonblood(long surgeEnd, float absorption) {
        public static final Moonblood NONE = new Moonblood(0L, 0.0F);
        private static final Codec<Moonblood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("surge_end").forGetter(Moonblood::surgeEnd),
                Codec.FLOAT.optionalFieldOf("absorption", 0.0F).forGetter(Moonblood::absorption)
        ).apply(instance, Moonblood::new));

        public Moonblood {
            surgeEnd = Math.max(0L, surgeEnd);
            absorption = surgeEnd > 0L && Float.isFinite(absorption) ? Math.max(0.0F, absorption) : 0.0F;
        }
    }

    // Keep the existing keys at the top level and group them within the builder's sixteen fields.
    private record Extra(CompanionSortSettings sorting, boolean ignoreOwnerDamage, Moonblood moonblood) {
        private static final MapCodec<Extra> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CompanionSortSettings.CODEC.optionalFieldOf("sorting", CompanionSortSettings.EMPTY).forGetter(Extra::sorting),
                Codec.BOOL.optionalFieldOf("ignore_owner_damage", false).forGetter(Extra::ignoreOwnerDamage),
                Moonblood.CODEC.optionalFieldOf("moonblood", Moonblood.NONE).forGetter(Extra::moonblood)
        ).apply(instance, Extra::new));
    }

    public static final Codec<CompanionRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(record -> record.id),
            UUIDUtil.STRING_CODEC.fieldOf("owner").forGetter(record -> record.owner),
            Codec.STRING.fieldOf("classic_skin").forGetter(record -> record.classicSkin),
            Codec.STRING.fieldOf("modern_skin").forGetter(record -> record.modernSkin),
            Codec.STRING.optionalFieldOf("custom_name", "").forGetter(record -> record.customName),
            Codec.intRange(0, 3).fieldOf("tier").forGetter(record -> record.tier),
            CompanionMode.CODEC.fieldOf("mode").forGetter(record -> record.mode),
            Codec.FLOAT.fieldOf("health").forGetter(record -> record.health),
            Codec.LONG.optionalFieldOf("hunt_start", 0L).forGetter(record -> record.huntStart),
            Codec.LONG.optionalFieldOf("hunt_end", 0L).forGetter(record -> record.huntEnd),
            Codec.LONG.optionalFieldOf("hunt_cooldown_end", 0L).forGetter(record -> record.huntCooldownEnd),
            Codec.INT.optionalFieldOf("generation", 0).forGetter(record -> record.generation),
            Identifier.CODEC.fieldOf("dimension").forGetter(record -> record.dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(record -> record.position),
            INVENTORY_CODEC.fieldOf("inventory").forGetter(CompanionRecord::items),
            Extra.CODEC.forGetter(record -> new Extra(record.sorting, record.ignoreOwnerDamage, record.moonblood))
    ).apply(instance, CompanionRecord::new));

    public final UUID id;
    public final UUID owner;
    public String classicSkin;
    public String modernSkin;
    public String customName;
    public int tier;
    public CompanionMode mode;
    public float health;
    public long huntStart;
    public long huntEnd;
    public long huntCooldownEnd;
    public int generation;
    public Identifier dimension;
    public BlockPos position;
    public CompanionSortSettings sorting;
    /** When set, nothing the owner causes can hurt this companion. Off by default and in older saves. */
    public boolean ignoreOwnerDamage;
    public Moonblood moonblood;
    private Runnable inventoryChanged = () -> {};
    public final SimpleContainer inventory = new SimpleContainer(CompanionRules.INVENTORY_SIZE) {
        @Override public void setChanged() { super.setChanged(); inventoryChanged.run(); }
    };
    public void onInventoryChanged(Runnable callback) { inventoryChanged = callback; }

    private CompanionRecord(UUID id, UUID owner, String classicSkin, String modernSkin, String customName, int tier, CompanionMode mode, float health,
                            long huntStart, long huntEnd, long huntCooldownEnd, int generation, Identifier dimension,
                            BlockPos position, List<ItemStack> items, Extra extra) {
        this.id = id;
        this.owner = owner;
        this.classicSkin = "howlingwerewolf:" + WerewolfSkin.byId(classicSkin).getId();
        this.modernSkin = ModernSkin.bySkinId(modernSkin) == null ? ModernSkin.ASHEN.skinId() : modernSkin;
        this.customName = customName;
        this.tier = tier;
        this.mode = mode;
        this.health = Float.isFinite(health) ? Math.clamp(health, 0, (float) CompanionRules.maxHealth(tier)) : 0;
        this.huntStart = huntStart;
        this.huntEnd = huntEnd;
        this.huntCooldownEnd = Math.max(0, huntCooldownEnd);
        this.generation = generation;
        this.dimension = dimension;
        this.position = position.immutable();
        this.sorting = extra.sorting();
        this.ignoreOwnerDamage = extra.ignoreOwnerDamage();
        this.moonblood = extra.moonblood();
        for (int slot = 0; slot < items.size(); slot++) inventory.setItem(slot, items.get(slot).copy());
    }

    public CompanionRecord(UUID owner, String skin, Identifier dimension, BlockPos position) {
        this(UUID.randomUUID(), owner, "howlingwerewolf:" + WerewolfSkin.byId(skin).getId(),
                ModernSkin.bySkinId(skin) == null ? ModernSkin.ASHEN.skinId() : skin, "", 0, CompanionMode.FOLLOW,
                (float) CompanionRules.maxHealth(0), 0, 0, 0, 0, dimension, position,
                java.util.Collections.nCopies(CompanionRules.INVENTORY_SIZE, ItemStack.EMPTY),
                new Extra(CompanionSortSettings.EMPTY, false, Moonblood.NONE));
    }

    public static String normalizeSkin(String skin) {
        if (ModernSkin.bySkinId(skin) != null) return skin;
        return "howlingwerewolf:" + WerewolfSkin.byId(skin).getId();
    }

    public static String nameForSkin(String skin) {
        ModernSkin modern = ModernSkin.bySkinId(skin);
        if (modern != null) return modern.displayName();
        String classic = WerewolfSkin.byId(skin).getId();
        return Character.toUpperCase(classic.charAt(0)) + classic.substring(1);
    }

    public String skin() { return HWConfig.npcModelSet() == WerewolfModelSet.MODERN ? modernSkin : classicSkin; }
    public String name() { return customName.isEmpty() ? nameForSkin(skin()) : customName; }
    public boolean belongsTo(UUID playerId) { return owner.equals(playerId); }

    private List<ItemStack> items() {
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) result.add(inventory.getItem(slot).copy());
        return result;
    }
}
