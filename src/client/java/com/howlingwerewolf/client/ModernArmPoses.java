package com.howlingwerewolf.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Keep both arms in one native HumanoidModel pose, including the off hand. */
final class ModernArmPoses {
    static <T extends LivingEntity> void apply(HumanoidModel<T> model, T entity) {
        HumanoidModel.ArmPose main = pose(entity, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose off = pose(entity, InteractionHand.OFF_HAND);
        if (main.isTwoHanded()) off = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        model.rightArmPose = entity.getMainArm() == HumanoidArm.RIGHT ? main : off;
        model.leftArmPose = entity.getMainArm() == HumanoidArm.RIGHT ? off : main;
    }

    private static HumanoidModel.ArmPose pose(LivingEntity entity, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        if (stack.isEmpty()) return HumanoidModel.ArmPose.EMPTY;
        if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
            switch (stack.getUseAnimation()) {
                case BLOCK: return HumanoidModel.ArmPose.BLOCK;
                case BOW: return HumanoidModel.ArmPose.BOW_AND_ARROW;
                case SPEAR: return HumanoidModel.ArmPose.THROW_SPEAR;
                case CROSSBOW: return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                case SPYGLASS: return HumanoidModel.ArmPose.SPYGLASS;
                case TOOT_HORN: return HumanoidModel.ArmPose.TOOT_HORN;
                case BRUSH: return HumanoidModel.ArmPose.BRUSH;
                default: break;
            }
        }
        if (!entity.swinging && stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        return HumanoidModel.ArmPose.ITEM;
    }
}
