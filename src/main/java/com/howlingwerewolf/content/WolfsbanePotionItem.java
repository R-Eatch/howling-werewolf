package com.howlingwerewolf.content;

import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WolfsbanePotionItem extends Item {
    public WolfsbanePotionItem() {
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
        tooltip.add(Component.translatable("item.howlingwerewolf.wolfsbane_potion.desc")
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.howlingwerewolf.wolfsbane_potion.desc2")
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
                    WerewolfGameplayEvents.removeSpiritWolves(player, data);
                    WerewolfGameplayEvents.removeWerewolfModifiers(player);
                    data.reset();
                    player.refreshDimensions();
                    ModNetwork.sync(player, data);
                    player.sendSystemMessage(Component.translatable("message.howlingwerewolf.cured")
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 60, 1));
                    player.sendSystemMessage(Component.translatable("message.howlingwerewolf.wolfsbane_poisoned")
                            .withStyle(ChatFormatting.DARK_GREEN));
                }
            });
        }
        return consumeAndReturnBottle(stack, living);
    }

    static ItemStack consumeAndReturnBottle(ItemStack stack, LivingEntity living) {
        if (living instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) {
                return bottle;
            }
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
        return stack;
    }
}
