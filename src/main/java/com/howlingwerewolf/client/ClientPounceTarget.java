package com.howlingwerewolf.client;

import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfForm;
import com.howlingwerewolf.ability.PounceAbility;
import com.howlingwerewolf.ability.PounceTargeting;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.network.PounceTargetPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ClientPounceTarget {
    private static LocalPlayer caster;
    private static WerewolfForm form;
    private static Vec3 point;
    private static boolean reachable;
    private static boolean consumeHeldAttack;

    static void toggle() {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (cancel() || player == null || minecraft.screen != null || !player.onGround() || PounceAbility.invalid(player)) return;
        var data = WerewolfApi.get(player).orElse(null);
        if (data == null || !data.hasAbility(WerewolfAbility.POUNCE) || !data.isTransformed()) return;
        long remaining = data.getPounceCooldownEnd() - player.level().getGameTime();
        if (remaining > 0) {
            minecraft.gui.setOverlayMessage(Component.translatable("message.howlingwerewolf.pounce_cooldown", (remaining + 19) / 20), false);
            return;
        }
        caster = player;
        form = data.getForm();
        update();
        minecraft.gui.setOverlayMessage(Component.translatable("message.howlingwerewolf.pounce_aiming",
                minecraft.options.keyAttack.getTranslatedKeyMessage(),
                minecraft.options.keyUse.getTranslatedKeyMessage()), false);
    }

    static void tick() {
        var minecraft = Minecraft.getInstance();
        if (!minecraft.options.keyAttack.isDown()) consumeHeldAttack = false;
        if (caster == null) return;
        if (minecraft.player != caster || minecraft.screen != null || !caster.onGround() || PounceAbility.invalid(caster)
                || !WerewolfApi.get(caster).map(data -> data.getForm() == form && data.hasAbility(WerewolfAbility.POUNCE)).orElse(false)) {
            cancel();
        } else {
            update();
        }
    }

    private static void update() {
        point = PounceTargeting.aim(caster, form);
        reachable = point != null && PounceTargeting.launchVelocity(caster, form, point) != null;
    }

    public static boolean confirm() {
        if (caster == null) return false;
        consumeHeldAttack = true;
        update();
        if (!reachable) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("message.howlingwerewolf.pounce_invalid"), false);
            return true;
        }
        ClientPacketDistributor.sendToServer(new PounceTargetPacket(point));
        cancel();
        return true;
    }

    public static boolean cancel() {
        if (caster == null) return false;
        caster = null;
        point = null;
        reachable = false;
        return true;
    }

    public static boolean consumesAttack() { return caster != null || consumeHeldAttack; }

    public static void draw(PoseStack poses, Camera camera, SubmitNodeCollector collector) {
        if (point == null || caster == null || poses == null) return;
        Vec3 center = point.subtract(camera.position()).add(0, 0.024, 0);
        float radius = (float) PounceTargeting.impactRadius(form);
        int tint = reachable ? 0xCFC8FF : 0xF1A667;
        poses.pushPose();
        poses.translate(center.x, center.y, center.z);
        collector.submitCustomGeometry(poses, RenderTypes.debugQuads(),
                (pose, vertices) -> drawRing(pose.pose(), vertices, radius, tint));
        poses.popPose();
    }

    private static void drawRing(Matrix4f matrix, VertexConsumer vertices, float radius, int tint) {
        for (int segment = 0; segment < 64; segment++) {
            double from = Math.TAU * segment / 64;
            double to = Math.TAU * (segment + 1) / 64;
            arc(vertices, matrix, from, to, 0, radius * 0.89F, tint, 0, 32);
            arc(vertices, matrix, from, to, radius * 0.89F, radius * 0.907F, tint, 136, 136);
            arc(vertices, matrix, from, to, radius * 0.96F, radius, tint, 210, 242);
            if (segment % 8 == 0) {
                arc(vertices, matrix, from - 0.024, from + 0.024, radius * 0.81F, radius * 0.958F, tint, 155, 225);
            }
        }
        // A small diamond makes the selected point readable even when the outer circle crosses uneven terrain.
        for (int side = 0; side < 4; side++) {
            double from = Math.TAU * side / 4;
            arc(vertices, matrix, from, from + Math.TAU / 4, 0.085F, 0.145F, tint, 160, 230);
        }
    }

    private static void arc(VertexConsumer out, Matrix4f matrix, double from, double to,
                            float inner, float outer, int tint, int insideAlpha, int rimAlpha) {
        vertex(out, matrix, from, inner, tint, insideAlpha);
        vertex(out, matrix, from, outer, tint, rimAlpha);
        vertex(out, matrix, to, outer, tint, rimAlpha);
        vertex(out, matrix, to, inner, tint, insideAlpha);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, double angle, float radius, int tint, int alpha) {
        out.addVertex(matrix, (float) Math.cos(angle) * radius, 0, (float) Math.sin(angle) * radius)
                .setColor(tint >> 16 & 255, tint >> 8 & 255, tint & 255, alpha);
    }

    private ClientPounceTarget() {}
}
