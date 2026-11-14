package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.howlingwerewolf.entity.TribeWerewolfEntity;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.UUID;

public final class PeltArmor {
    private static final AttributeModifier SPEED = new AttributeModifier(
            UUID.fromString("59ee0e44-36c0-4587-8801-a2a27d0363d3"), "Silverhide speed",
            0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);

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
        return new ArmorItem(material, type, new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, context, tooltip, flag);
                tooltip.add(Component.translatable("item.howlingwerewolf.silverhide.set_bonus").withStyle(ChatFormatting.GRAY));
            }
        };
    }

    public static void refreshSpeed(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean active = player.isAlive() && wearingSet(player) && !player.hasEffect(MobEffects.MOVEMENT_SPEED);
        if (active && !speed.hasModifier(SPEED)) speed.addTransientModifier(SPEED);
        if (!active && speed.hasModifier(SPEED)) speed.removeModifier(SPEED);
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
