package com.howlingwerewolf.content;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WerewolfPotionItem extends Item {
    public WerewolfPotionItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.howlingwerewolf.werewolf_potion.desc")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.howlingwerewolf.werewolf_potion.desc2")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!level.isClientSide && living instanceof ServerPlayer player) {
            net.minecraftforge.common.util.LazyOptional<com.howlingwerewolf.capability.WerewolfData> optional = WerewolfApi.get(player);
            if (!optional.isPresent()) {
                player.sendSystemMessage(Component.translatable("message.howlingwerewolf.data_unavailable")
                        .withStyle(ChatFormatting.RED));
                return stack;
            }
            optional.ifPresent(data -> {
                if (data.isWerewolf()) {
                    player.sendSystemMessage(Component.translatable("message.howlingwerewolf.already_werewolf")
                            .withStyle(ChatFormatting.GRAY));
                } else {
                    WerewolfGameplayEvents.removeWerewolfModifiers(player);
                    data.reset();
                    WerewolfGameplayEvents.awaken(player, data);
                    ModNetwork.sync(player, data);
                    player.sendSystemMessage(Component.translatable("message.howlingwerewolf.potion_awakened")
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            });
        }
        return WolfsbanePotionItem.consumeAndReturnBottle(stack, living);
    }
}
