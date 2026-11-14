package com.howlingwerewolf.entity;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.entity.WerewolfTrades.MerchantRole;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

final class WerewolfTrades {
    private static final ResourceKey<TradeSet> POOL = ResourceKey.create(Registries.TRADE_SET,
            Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "werewolf_brother"));

    enum MerchantRole {
        WARDEN(2, 2, 1, 4, 3, 2, 1),
        CHIEF(2, 2, 2, 5, 4, 2, 1),
        BROTHER(3, 3, 2, 12, 8, 4, 3);

        final int resources, pelts, specials;
        final int resourceUses, peltUses, pearlUses, potionUses;

        MerchantRole(int resources, int pelts, int specials, int resourceUses, int peltUses, int pearlUses, int potionUses) {
            this.resources = resources;
            this.pelts = pelts;
            this.specials = specials;
            this.resourceUses = resourceUses;
            this.peltUses = peltUses;
            this.pearlUses = pearlUses;
            this.potionUses = potionUses;
        }
    }

    private enum Category { RESOURCE, BUY_PELT, SELL_PELT, PEARL, POTION }

    private record Trade(MerchantOffer offer, Category category, MerchantRole specialty) {
        MerchantOffer forRole(MerchantRole role) {
            if (role == MerchantRole.BROTHER) return offer.copy();
            int maxUses = switch (category) {
                case RESOURCE -> role.resourceUses;
                case BUY_PELT, SELL_PELT -> role.peltUses;
                case PEARL -> role.pearlUses;
                case POTION -> role.potionUses;
            };
            return new MerchantOffer(offer.getItemCostA(), offer.getItemCostB(), offer.getResult().copy(),
                    maxUses, offer.getXp(), offer.getPriceMultiplier());
        }
    }

    static void draw(AbstractVillager merchant, ServerLevel level, MerchantOffers offers, MerchantRole role) {
        var pool = level.registryAccess().lookupOrThrow(Registries.TRADE_SET).getOptional(POOL).orElse(null);
        if (pool == null) return;
        var context = new LootContext.Builder(new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, merchant.position())
                .withParameter(LootContextParams.THIS_ENTITY, merchant)
                .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
                .create(LootContextParamSets.VILLAGER_TRADE)).create(pool.randomSequence());
        int limit = role == MerchantRole.BROTHER ? pool.calculateNumberOfTrades(context) : role.resources + role.pelts + role.specials;
        if (limit <= 0) return;

        List<Trade> resources = new ArrayList<>();
        List<Trade> buys = new ArrayList<>();
        List<Trade> sells = new ArrayList<>();
        List<Trade> specials = new ArrayList<>();
        for (var entry : pool.getTrades()) {
            var offer = entry.value().getOffer(context);
            if (offer == null) continue;
            Trade trade = classify(offer);
            switch (trade.category()) {
                case RESOURCE -> resources.add(trade);
                case BUY_PELT -> buys.add(trade);
                case SELL_PELT -> sells.add(trade);
                case PEARL, POTION -> {
                    if (trade.specialty() == null || trade.specialty() == role || role == MerchantRole.BROTHER) specials.add(trade);
                }
            }
        }

        RandomSource random = context.getRandom();
        List<Trade> all = new ArrayList<>();
        if (pool.allowDuplicates() && role == MerchantRole.BROTHER) {
            all.addAll(resources);
            all.addAll(buys);
            all.addAll(sells);
            all.addAll(specials);
        }
        // Reserve both directions even when an external trade set reduces the requested count.
        add(offers, buys, role, random, false, limit);
        add(offers, sells, role, random, false, limit);
        for (int i = 0; i < role.resources; i++) add(offers, resources, role, random, i == 0, limit);
        if (role.pelts == 3) {
            List<Trade> extra = (buys.isEmpty() || (!sells.isEmpty() && random.nextBoolean())) ? sells : buys;
            add(offers, extra, role, random, false, limit);
        }
        for (int i = 0; i < role.specials; i++) add(offers, specials, role, random, i == 0, limit);
        if (offers.size() >= limit) return;

        List<Trade> remaining = new ArrayList<>(resources);
        remaining.addAll(buys);
        remaining.addAll(sells);
        remaining.addAll(specials);
        while (!remaining.isEmpty() && offers.size() < limit) add(offers, remaining, role, random, false, limit);
        if (!all.isEmpty()) {
            while (offers.size() < limit) offers.add(all.get(random.nextInt(all.size())).forRole(role));
        }
    }

    private static Trade classify(MerchantOffer offer) {
        var output = offer.getResult();
        var input = offer.getBaseCostA();
        if (output.is(ModItems.WEREWOLF_PELT.get())) return new Trade(offer, Category.BUY_PELT, null);
        if (input.is(ModItems.WEREWOLF_PELT.get())) return new Trade(offer, Category.SELL_PELT, null);
        if (output.is(ModItems.MOONBANE_PEARL.get())) {
            return new Trade(offer, Category.PEARL, input.is(ModBlocks.WOLFSBANE.asItem()) ? MerchantRole.CHIEF : null);
        }
        if (output.is(ModItems.WEREWOLF_POTION.get())) return new Trade(offer, Category.POTION, MerchantRole.CHIEF);
        if (output.is(ModItems.WOLFSBANE_POTION.get())) return new Trade(offer, Category.POTION, MerchantRole.WARDEN);
        if (output.is(ModItems.SILVER_INGOT.get())) return new Trade(offer, Category.RESOURCE, MerchantRole.WARDEN);
        if (output.is(ModBlocks.WOLFSBANE.asItem())) return new Trade(offer, Category.RESOURCE, MerchantRole.CHIEF);
        return new Trade(offer, Category.RESOURCE, null);
    }

    private static void add(MerchantOffers offers, List<Trade> pool, MerchantRole role, RandomSource random,
                            boolean preferSpecialty, int limit) {
        if (offers.size() >= limit || pool.isEmpty()) return;
        if (preferSpecialty) {
            List<Trade> preferred = pool.stream().filter(trade -> trade.specialty() == role).toList();
            if (!preferred.isEmpty()) {
                Trade chosen = preferred.get(random.nextInt(preferred.size()));
                pool.remove(chosen);
                offers.add(chosen.forRole(role));
                return;
            }
        }
        offers.add(pool.remove(random.nextInt(pool.size())).forRole(role));
    }

    private WerewolfTrades() {}
}
