package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import net.minecraft.ChatFormatting;
import com.howlingwerewolf.entity.SettlementCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Consumer;

public final class PeltArmor {
    private static final Identifier SET = Identifier.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "silverhide");
    private static final float SPECIAL_REDUCTION_PER_PIECE = 0.125F;

    private static final ArmorMaterial MATERIAL = new ArmorMaterial(
            ArmorMaterials.IRON.durability(), ArmorMaterials.IRON.defense(), ArmorMaterials.GOLD.enchantmentValue(),
            SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, TagKey.create(Registries.ITEM, SET.withSuffix("_repair")),
            ResourceKey.create(EquipmentAssets.ROOT_ID, SET));

    public static Item piece(Item.Properties properties, ArmorType type) {
        return new Item(properties.humanoidArmor(MATERIAL, type).attributes(MATERIAL.createAttributes(type)
                .withModifierAdded(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(SET.withSuffix("_speed_" + type.getName()), 0.0125, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(type.getSlot())))) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                        Consumer<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, context, display, tooltip, flag);
                tooltip.accept(Component.translatable("item.howlingwerewolf.silverhide.set_bonus").withStyle(ChatFormatting.GRAY));
            }
        };
    }

    public static float reduce(LivingEntity bearer, DamageSource source, float damage) {
        if (damage <= 0 || source.is(DamageTypes.GENERIC_KILL)) return damage;
        int pieces = countPieces(bearer);
        if (pieces == 4) damage *= source.is(DamageTypes.FELL_OUT_OF_WORLD) ? 0.95F : 0.90F;
        if (pieces > 0 && (source.is(DamageTypeTags.IS_FREEZING) || SettlementCombat.isWerewolf(source.getEntity())))
            damage *= 1 - pieces * SPECIAL_REDUCTION_PER_PIECE;
        return damage;
    }

    public static boolean wearingSet(LivingEntity bearer) { return countPieces(bearer) == 4; }

    private static int countPieces(LivingEntity bearer) {
        return (bearer.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SILVERHIDE_HELMET.get()) ? 1 : 0)
                + (bearer.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SILVERHIDE_CHESTPLATE.get()) ? 1 : 0)
                + (bearer.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SILVERHIDE_LEGGINGS.get()) ? 1 : 0)
                + (bearer.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SILVERHIDE_BOOTS.get()) ? 1 : 0);
    }

    private PeltArmor() {}
}
