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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
    private static final Identifier SPEED_ID = SET.withSuffix("_speed");
    private static final AttributeModifier SPEED = new AttributeModifier(SPEED_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static final ArmorMaterial MATERIAL = new ArmorMaterial(
            ArmorMaterials.IRON.durability(), ArmorMaterials.IRON.defense(), ArmorMaterials.GOLD.enchantmentValue(),
            SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, TagKey.create(Registries.ITEM, SET.withSuffix("_repair")),
            ResourceKey.create(EquipmentAssets.ROOT_ID, SET));

    public static Item piece(Item.Properties properties, ArmorType type) {
        return new Item(properties.humanoidArmor(MATERIAL, type)) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                        Consumer<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, context, display, tooltip, flag);
                tooltip.accept(Component.translatable("item.howlingwerewolf.silverhide.set_bonus").withStyle(ChatFormatting.GRAY));
            }
        };
    }

    public static void refreshSpeed(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean active = player.isAlive() && wearingSet(player) && !player.hasEffect(MobEffects.SPEED);
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
