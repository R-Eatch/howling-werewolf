package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.SettlementCombat;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class PeltArmor {
    private static final ResourceLocation SET = ResourceLocation.fromNamespaceAndPath(HowlingWerewolf.MOD_ID, "silverhide");
    private static final float SPECIAL_REDUCTION_PER_PIECE = 0.10F;

    public static ArmorMaterial material() {
        return new ArmorMaterial(ArmorMaterials.IRON.value().defense(), ArmorMaterials.GOLD.value().enchantmentValue(),
                SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.SILVERHIDE.get()),
                List.of(new ArmorMaterial.Layer(SET)), 0, 0);
    }

    public static ArmorItem piece(Holder<ArmorMaterial> material, ArmorItem.Type type) {
        return new ArmorItem(material, type, new Item.Properties().durability(type.getDurability(15))) {
            @Override
            public ItemAttributeModifiers getDefaultAttributeModifiers() {
                return super.getDefaultAttributeModifiers().withModifierAdded(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(SET.withSuffix("_speed_" + type.getName()), 0.0125, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(type.getSlot()));
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
