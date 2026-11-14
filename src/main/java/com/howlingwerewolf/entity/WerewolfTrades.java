package com.howlingwerewolf.entity;

import com.howlingwerewolf.content.ModBlocks;
import com.howlingwerewolf.content.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class WerewolfTrades {
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

    private record Trade(Category category, Supplier<? extends ItemLike> input, int inputCount,
                         Supplier<? extends ItemLike> extra, int extraCount, Supplier<? extends ItemLike> output,
                         int outputCount, MerchantRole specialty) {
        MerchantOffer offer(MerchantRole role) {
            int uses = switch (category) {
                case RESOURCE -> role.resourceUses;
                case BUY_PELT, SELL_PELT -> role.peltUses;
                case PEARL -> role.pearlUses;
                case POTION -> role.potionUses;
            };
            ItemStack second = extra == null ? ItemStack.EMPTY : new ItemStack(extra.get(), extraCount);
            return new MerchantOffer(new ItemStack(input.get(), inputCount), second, new ItemStack(output.get(), outputCount), uses, 0, 0.0F);
        }
    }

    private static final List<Trade> TRADES = List.of(
            trade(Category.RESOURCE, () -> Items.EMERALD, 6, ModItems.SILVER_INGOT, 4, MerchantRole.WARDEN),
            trade(Category.RESOURCE, () -> Items.IRON_INGOT, 16, ModItems.SILVER_INGOT, 2, MerchantRole.WARDEN),
            trade(Category.RESOURCE, ModBlocks.WOLFSBANE, 4, ModItems.SILVER_INGOT, 2, MerchantRole.WARDEN),
            trade(Category.RESOURCE, ModItems.SILVER_INGOT, 4, ModBlocks.WOLFSBANE, 2, MerchantRole.CHIEF),
            trade(Category.RESOURCE, () -> Items.EMERALD, 4, ModBlocks.WOLFSBANE, 4, MerchantRole.CHIEF),
            trade(Category.BUY_PELT, ModBlocks.WOLFSBANE, 4, ModItems.WEREWOLF_PELT, 1, null),
            trade(Category.BUY_PELT, ModItems.SILVER_INGOT, 4, ModItems.WEREWOLF_PELT, 1, null),
            trade(Category.SELL_PELT, ModItems.WEREWOLF_PELT, 1, ModBlocks.WOLFSBANE, 2, null),
            trade(Category.SELL_PELT, ModItems.WEREWOLF_PELT, 1, ModItems.SILVER_INGOT, 2, null),
            new Trade(Category.PEARL, () -> Items.EMERALD, 12, ModBlocks.WOLFSBANE, 4, ModItems.MOONBANE_PEARL, 1, null),
            new Trade(Category.PEARL, ModBlocks.WOLFSBANE, 6, () -> Items.DIAMOND, 1, ModItems.MOONBANE_PEARL, 1, MerchantRole.CHIEF),
            new Trade(Category.POTION, ModBlocks.WOLFSBANE, 4, () -> Items.EMERALD, 2, ModItems.WEREWOLF_POTION, 1, MerchantRole.CHIEF),
            new Trade(Category.POTION, ModItems.SILVER_INGOT, 4, () -> Items.EMERALD, 2, ModItems.WOLFSBANE_POTION, 1, MerchantRole.WARDEN));

    static void draw(MerchantOffers offers, MerchantRole role, RandomSource random) {
        List<Trade> resources = new ArrayList<>();
        List<Trade> buys = new ArrayList<>();
        List<Trade> sells = new ArrayList<>();
        List<Trade> specials = new ArrayList<>();
        for (Trade trade : TRADES) {
            switch (trade.category()) {
                case RESOURCE -> resources.add(trade);
                case BUY_PELT -> buys.add(trade);
                case SELL_PELT -> sells.add(trade);
                case PEARL, POTION -> {
                    if (trade.specialty() == null || trade.specialty() == role || role == MerchantRole.BROTHER) specials.add(trade);
                }
            }
        }
        for (int i = 0; i < role.resources; i++) offers.add(take(resources, role, random, i == 0));
        offers.add(take(buys, role, random, false));
        offers.add(take(sells, role, random, false));
        if (role.pelts == 3) offers.add(take(random.nextBoolean() ? buys : sells, role, random, false));
        for (int i = 0; i < role.specials; i++) offers.add(take(specials, role, random, i == 0));
    }

    private static MerchantOffer take(List<Trade> pool, MerchantRole role, RandomSource random, boolean preferSpecialty) {
        if (preferSpecialty) {
            List<Trade> preferred = pool.stream().filter(trade -> trade.specialty() == role).toList();
            if (!preferred.isEmpty()) {
                Trade chosen = preferred.get(random.nextInt(preferred.size()));
                pool.remove(chosen);
                return chosen.offer(role);
            }
        }
        return pool.remove(random.nextInt(pool.size())).offer(role);
    }

    private static Trade trade(Category category, Supplier<? extends ItemLike> input, int inputCount,
                               Supplier<? extends ItemLike> output, int outputCount, MerchantRole specialty) {
        return new Trade(category, input, inputCount, null, 0, output, outputCount, specialty);
    }

    private WerewolfTrades() {}
}
