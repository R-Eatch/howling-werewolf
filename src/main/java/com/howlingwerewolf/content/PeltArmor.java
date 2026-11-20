package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.SettlementCombat;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.UUID;

public final class PeltArmor {
    private static final float SPECIAL_REDUCTION_PER_PIECE = 0.10F;

    public static ArmorMaterial material() {
        return new ArmorMaterial() {
            @Override public int getDurabilityForType(ArmorItem.Type type) { return ArmorMaterials.IRON.getDurabilityForType(type); }
            @Override public int getDefenseForType(ArmorItem.Type type) { return ArmorMaterials.IRON.getDefenseForType(type); }
            @Override public int getEnchantmentValue() { return ArmorMaterials.GOLD.getEnchantmentValue(); }
            @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
            @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.SILVERHIDE.get()); }
            @Override public String getName() { return HowlingWerewolf.MOD_ID + ":silverhide"; }
            @Override public float getToughness() { return 0; }
            @Override public float getKnockbackResistance() { return 0; }
        };
    }

    public static ArmorItem piece(ArmorMaterial material, ArmorItem.Type type) {
        EquipmentSlot slot = type.getSlot();
        UUID id = UUID.nameUUIDFromBytes(("howlingwerewolf:silverhide_speed_" + slot.getName())
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var speed = new AttributeModifier(id, "Silverhide speed", 0.0125, AttributeModifier.Operation.ADDITION);
        return new ArmorItem(material, type, new Item.Properties()) {
            @Override
            public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot query) {
                var defaults = super.getDefaultAttributeModifiers(query);
                if (query != slot) return defaults;
                return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                        .putAll(defaults).put(Attributes.MOVEMENT_SPEED, speed).build();
            }

        };
    }

    public static int countPieces(LivingEntity bearer) {
        int pieces = 0;
        if (bearer.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SILVERHIDE_HELMET.get())) pieces++;
        if (bearer.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SILVERHIDE_CHESTPLATE.get())) pieces++;
        if (bearer.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SILVERHIDE_LEGGINGS.get())) pieces++;
        if (bearer.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SILVERHIDE_BOOTS.get())) pieces++;
        return pieces;
    }

    public static boolean wearingSet(LivingEntity bearer) { return countPieces(bearer) == 4; }

    public static float reduce(LivingEntity bearer, DamageSource source, float damage) {
        if (damage <= 0 || source.is(DamageTypes.GENERIC_KILL)) return damage;
        int pieces = countPieces(bearer);
        if (pieces == 4) damage *= source.is(DamageTypes.FELL_OUT_OF_WORLD) ? 0.95F : 0.90F;
        if (pieces > 0 && (source.is(DamageTypeTags.IS_FREEZING) || SettlementCombat.isWerewolf(source.getEntity()))) {
            damage *= 1 - pieces * SPECIAL_REDUCTION_PER_PIECE;
        }
        return damage;
    }

    private PeltArmor() {}
}
