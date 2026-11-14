package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.howlingwerewolf.entity.TribeWerewolfEntity;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class PeltArmor {
    private static final ResourceLocation SET = ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "silverhide");
    private static final ResourceLocation SPEED_ID = SET.withSuffix("_speed");
    private static final AttributeModifier SPEED = new AttributeModifier(SPEED_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public static ArmorMaterial material() {
        return new ArmorMaterial(ArmorMaterials.IRON.value().defense(), ArmorMaterials.GOLD.value().enchantmentValue(),
                SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.SILVERHIDE.get()),
                List.of(new ArmorMaterial.Layer(SET)), 0, 0);
    }

    public static ArmorItem piece(Holder<ArmorMaterial> material, ArmorItem.Type type) {
        return new ArmorItem(material, type, new Item.Properties().durability(type.getDurability(15))) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, context, tooltip, flag);
                tooltip.add(Component.translatable("item.howlingwerewolf.silverhide.set_bonus").withStyle(ChatFormatting.GRAY));
            }
        };
    }

    public static void refreshSpeed(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean active = player.isAlive() && wearingSet(player) && !player.hasEffect(MobEffects.MOVEMENT_SPEED);
        if (active && !speed.hasModifier(SPEED_ID)) speed.addTransientModifier(SPEED);
        if (!active && speed.hasModifier(SPEED_ID)) speed.removeModifier(SPEED_ID);
    }

    public static float reduce(ServerPlayer player, DamageSource source, float damage) {
        return damage > 0 && wearingSet(player) && (source.is(DamageTypeTags.IS_FREEZING) || fromWerewolf(source.getEntity()))
                ? damage * 0.5F : damage;
    }

    private static boolean wearingSet(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SILVERHIDE_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SILVERHIDE_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SILVERHIDE_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SILVERHIDE_BOOTS.get());
    }

    private static boolean fromWerewolf(Entity source) {
        return source instanceof WerewolfEntity || source instanceof AlphaWerewolfEntity || source instanceof TribeWerewolfEntity
                || source instanceof WerewolfCompanionEntity || source instanceof SettlementLeaderEntity leader && !leader.isWarden()
                || source instanceof ServerPlayer player && WerewolfApi.get(player).map(data -> data.isTransformed()).orElse(false);
    }

    private PeltArmor() {}
}
