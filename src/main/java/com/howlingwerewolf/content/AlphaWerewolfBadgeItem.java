package com.howlingwerewolf.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public final class AlphaWerewolfBadgeItem extends Item {
    public AlphaWerewolfBadgeItem(Item.Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.howlingwerewolf.alpha_werewolf_badge.desc")
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.howlingwerewolf.alpha_werewolf_badge.desc2")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("item.howlingwerewolf.alpha_werewolf_badge.desc3")
                .withStyle(ChatFormatting.GRAY));
    }
}
