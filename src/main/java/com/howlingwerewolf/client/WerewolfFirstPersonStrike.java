/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.WerewolfModelSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The same strikes seen from behind the eyes. With both hands free, the two claws rake in turn, the
 * second rising from below the screen only for the blow. Held items keep their vanilla motion,
 * and a claw digging at a block keeps the plain arm, so neither hides what it works on.
 */
@EventBusSubscriber(modid = "howlingwerewolf", value = Dist.CLIENT)
public final class WerewolfFirstPersonStrike {
    private static final float FOLLOW_DELAY = 0.17F, BEAST_FOLLOW_DELAY = 0.05F;

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || !event.getItemStack().isEmpty() || player.isUsingItem()) return;
        var data = WerewolfRenderData.forEntity(player, event.getPartialTick());
        if (data.form() != WerewolfForm.WEREWOLF && data.form() != WerewolfForm.BEAST) return;
        var strike = data.strike();
        if (!strike.active() || strike.dig()) return;
        boolean bothFree = player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
        HumanoidArm main = player.getMainArm();
        boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
        HumanoidArm arm = mainHand ? main : main.getOpposite();
        boolean beast = data.form() == WerewolfForm.BEAST;
        // One arm is drawn once: a strike still settling simply adds to the one just begun.
        Stroke stroke = new Stroke();
        if (strike.time() > 0) stroke.add(strike.time(), arm == HumanoidArm.RIGHT == strike.leadRight(), strike.index(), beast, bothFree);
        if (strike.previousTime() > 0) stroke.add(strike.previousTime(), arm == HumanoidArm.RIGHT == strike.previousLeadRight(), strike.previousIndex(), beast, bothFree);
        event.setCanceled(true);
        // The off-hand claw waits below the screen until it is needed.
        float present = mainHand ? 1 : stroke.present;
        if (present <= 0) return;
        float invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(invert * (stroke.x + 0.64000005F), stroke.y - 0.6F + (mainHand ? event.getEquipProgress() * -0.6F : (1 - present) * -1.1F), stroke.z - 0.71999997F);
        pose.mulPose(Axis.XP.rotationDegrees(stroke.pitch));
        pose.mulPose(Axis.ZP.rotationDegrees(invert * stroke.roll));
        pose.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
        pose.translate(invert * -1.0F, 3.6F, 3.5F);
        pose.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
        pose.mulPose(Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
        pose.translate(invert * 5.6F, 0.0F, 0.0F);
        draw(pose, event.getSubmitNodeCollector(), event.getPackedLight(), arm, data);
        pose.popPose();
    }

    /**
     * Where one hand's strokes put it. The arm trails behind its hand, so moving the hand draws the
     * whole stroke; x runs toward the far side of the screen, and rotations only add a little life.
     */
    private static final class Stroke {
        float x, y, z, roll, pitch, present;

        void add(float time, boolean lead, int index, boolean beast, boolean bothFree) {
            float t = lead ? time : time - (beast ? BEAST_FOLLOW_DELAY : FOLLOW_DELAY);
            // Beside a weapon, the free claw only follows through.
            float power = lead || bothFree ? 1 : 0.5F;
            if (beast) {
                float up = beat(t, 0.20F, 0.18F, 0.32F), down = reach(t, 0.18F, 0.32F, 0.46F, 0.92F);
                if (index % 2 == 0) {
                    // Both claws come down from overhead.
                    x += (-0.04F * up - 0.40F * down) * power;
                    y += (1.0F * up - 0.05F * down) * power;
                    z += (0.10F * up - 0.25F * down) * power;
                    roll += (-6 * up + 8 * down) * power;
                    pitch += (14 * up - 24 * down) * power;
                } else {
                    // Both claws scythe in from the sides and pass each other.
                    x += (0.40F * up - 1.05F * down) * power;
                    y += (0.50F * up + 0.28F * down) * power;
                    z += (0.05F * up - 0.25F * down) * power;
                    roll += (-14 * up + 16 * down) * power;
                }
            } else {
                float up = beat(t, 0.14F, 0.12F, 0.28F), down = reach(t, 0.12F, 0.28F, 0.38F, 0.74F);
                x += (0.10F * up - 0.85F * down) * power;
                y += (0.58F * up + 0.18F * down) * power;
                z += (0.05F * up - 0.30F * down) * power;
                roll += (-12 * up + 14 * down) * power;
            }
            present = Math.max(present, Math.min(smooth(t / 0.10F), 1 - smooth((t - 0.55F) / 0.30F)));
        }
    }

    private static void draw(PoseStack pose, SubmitNodeCollector collector, int light, HumanoidArm arm, WerewolfRenderData data) {
        if (data.modelSet() == WerewolfModelSet.MODERN) ModernFirstPersonClawRenderer.draw(pose, collector, light, arm, data);
        else WerewolfFirstPersonClawRenderer.draw(pose, collector, light, arm, data);
    }

    /** Every form leans into its blow; a wolf has no arms to watch, so its bite is felt here alone. */
    @SubscribeEvent
    public static void nudgeCamera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson() || !minecraft.options.bobView().get()) return;
        var data = WerewolfRenderData.forEntity(player, (float) event.getPartialTick());
        if (data.form() == WerewolfForm.HUMAN || data.strike().dig() || data.strike().time() <= 0) return;
        float t = data.strike().time();
        float amount = switch (data.form()) {
            case BEAST -> 1.5F * reach(t, 0.18F, 0.32F, 0.46F, 0.92F) - 0.7F * beat(t, 0.20F, 0.18F, 0.32F);
            case QUADRUPED -> 2.2F * reach(t, 0.16F, 0.30F, 0.42F, 0.85F) - 1.0F * beat(t, 0.20F, 0.16F, 0.30F);
            default -> 0.5F * reach(t, 0.12F, 0.28F, 0.38F, 0.74F);
        };
        event.setPitch(event.getPitch() + amount);
    }

    private static float smooth(float value) {
        float clamped = Mth.clamp(value, 0, 1);
        return clamped * clamped * (3 - 2 * clamped);
    }
    private static float beat(float t, float rise, float fallStart, float fallEnd) {
        return smooth(t / rise) * (1 - smooth((t - fallStart) / (fallEnd - fallStart)));
    }
    private static float reach(float t, float whipStart, float whipEnd, float settleStart, float settleEnd) {
        return smooth((t - whipStart) / (whipEnd - whipStart)) * (1 - smooth((t - settleStart) / (settleEnd - settleStart)));
    }

    private WerewolfFirstPersonStrike() {}
}
