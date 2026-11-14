package com.howlingwerewolf.world;

import com.howlingwerewolf.ModernSkin;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks only the new sites. Unloaded entities retain their slots. */
public final class SettlementData extends SavedData {
    static final int TRIBE_MEMBERS = 4;
    public static final int DAY = 24000;
    private static final Factory<SettlementData> FACTORY = new Factory<>(SettlementData::new, SettlementData::load, null);
    private final Map<BlockPos, Site> sites = new HashMap<>();

    static final class Site {
        final ModernSkin coat;
        final UUID[] members = new UUID[1 + TRIBE_MEMBERS];
        final long[] replacementAt = new long[members.length];
        CompoundTag tradeStock = new CompoundTag();

        Site(ModernSkin coat) { this.coat = coat; }
    }

    public static SettlementData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "howlingwerewolf_settlements");
    }

    Site getOrCreate(BlockPos position, boolean tribe, RandomSource random) {
        return sites.computeIfAbsent(position.immutable(), ignored -> {
            setDirty();
            ModernSkin[] coats = ModernSkin.values();
            return new Site(tribe ? coats[random.nextInt(coats.length)] : ModernSkin.ASHEN);
        });
    }

    public void onDeath(ServerLevel level, BlockPos position, UUID member, SettlementLeaderEntity leader) {
        Site site = sites.get(position);
        if (site == null) return;
        for (int slot = 0; slot < site.members.length; slot++) {
            if (!member.equals(site.members[slot])) continue;
            site.members[slot] = null;
            int days = leader == null ? 1 : leader.isWarden() ? 2 : 3;
            site.replacementAt[slot] = level.getGameTime() + (long) DAY * days;
            if (leader != null) {
                site.tradeStock = leader.saveTradeStock();
                MerchantOffers.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), leader.getOffers())
                        .result().ifPresent(offers -> site.tradeStock.put("Offers", offers));
            }
            setDirty();
            return;
        }
    }

    private static SettlementData load(CompoundTag tag, HolderLookup.Provider registries) {
        SettlementData data = new SettlementData();
        for (Tag entry : tag.getList("Sites", Tag.TAG_COMPOUND)) {
            CompoundTag stored = (CompoundTag) entry;
            Site site = new Site(ModernSkin.byId(stored.getString("Coat")));
            ListTag members = stored.getList("Members", Tag.TAG_COMPOUND);
            for (int slot = 0; slot < Math.min(members.size(), site.members.length); slot++) {
                CompoundTag member = members.getCompound(slot);
                if (member.hasUUID("Id")) site.members[slot] = member.getUUID("Id");
                site.replacementAt[slot] = member.getLong("ReplacementAt");
            }
            site.tradeStock = stored.getCompound("TradeStock");
            data.sites.put(BlockPos.of(stored.getLong("Pos")), site);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        sites.forEach((position, site) -> {
            CompoundTag stored = new CompoundTag();
            stored.putLong("Pos", position.asLong());
            stored.putString("Coat", site.coat.id());
            ListTag members = new ListTag();
            for (int slot = 0; slot < site.members.length; slot++) {
                CompoundTag member = new CompoundTag();
                if (site.members[slot] != null) member.putUUID("Id", site.members[slot]);
                member.putLong("ReplacementAt", site.replacementAt[slot]);
                members.add(member);
            }
            stored.put("Members", members);
            stored.put("TradeStock", site.tradeStock.copy());
            entries.add(stored);
        });
        tag.put("Sites", entries);
        return tag;
    }
}
