package com.howlingwerewolf.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class AlphaWerewolfBadgeItem extends Item {
    public AlphaWerewolfBadgeItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.howlingwerewolf.alpha_werewolf_badge.desc")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.howlingwerewolf.alpha_werewolf_badge.desc2")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.howlingwerewolf.alpha_werewolf_badge.desc3")
                .withStyle(ChatFormatting.GRAY));
    }
}
