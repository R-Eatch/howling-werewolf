package com.howlingwerewolf.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;


public final class CentralRitualAltarBlockItem extends BlockItem {
    public CentralRitualAltarBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.howlingwerewolf.central_ritual_altar.desc")
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.howlingwerewolf.central_ritual_altar.desc2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.howlingwerewolf.central_ritual_altar.desc3")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("item.howlingwerewolf.central_ritual_altar.desc4")
                .withStyle(ChatFormatting.RED));
    }
}
