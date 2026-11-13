/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

/**
 * The moonlit screens draw through this small surface, so the same drawing code serves every
 * Minecraft version: here it forwards to the 1.21.1 {@link GuiGraphics}. Positions may be
 * fractional; the 2D pose keeps shapes smooth between pixels.
 */
final class MoonGraphics {
    private final GuiGraphics raw;
    private final Pose pose;

    MoonGraphics(GuiGraphics raw) {
        this.raw = raw;
        this.pose = new Pose(raw.pose());
    }
    GuiGraphics raw() { return raw; }
    Pose pose() { return pose; }

    /** A 2D view of the pose stack. */
    static final class Pose {
        private final PoseStack stack;
        Pose(PoseStack stack) { this.stack = stack; }
        void pushMatrix() { stack.pushPose(); }
        void popMatrix() { stack.popPose(); }
        void translate(float x, float y) { stack.translate(x, y, 0); }
        void scale(float x, float y) { stack.scale(x, y, 1); }
        void rotate(float radians) { stack.mulPose(Axis.ZP.rotation(radians)); }
    }

    void fill(int x0, int y0, int x1, int y1, int color) { raw.fill(x0, y0, x1, y1, color); }
    void fillGradient(int x0, int y0, int x1, int y1, int top, int bottom) { raw.fillGradient(x0, y0, x1, y1, top, bottom); }
    void text(Font font, String text, int x, int y, int color, boolean shadow) { raw.drawString(font, text, x, y, color, shadow); }
    void text(Font font, Component text, int x, int y, int color, boolean shadow) { raw.drawString(font, text, x, y, color, shadow); }
    void text(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) { raw.drawString(font, text, x, y, color, shadow); }
    void item(ItemStack stack, int x, int y) { raw.renderItem(stack, x, y); }
    void outline(int x, int y, int width, int height, int color) { raw.renderOutline(x, y, width, height, color); }
    /** Clips to a rectangle given in the current pose's units, as the moonlit pages expect. */
    void enableScissor(int x0, int y0, int x1, int y1) {
        Matrix4f matrix = raw.pose().last().pose();
        org.joml.Vector3f a = matrix.transformPosition(x0, y0, 0, new org.joml.Vector3f());
        org.joml.Vector3f b = matrix.transformPosition(x1, y1, 0, new org.joml.Vector3f());
        raw.enableScissor((int) Math.floor(Math.min(a.x, b.x)), (int) Math.floor(Math.min(a.y, b.y)),
                (int) Math.ceil(Math.max(a.x, b.x)), (int) Math.ceil(Math.max(a.y, b.y)));
    }
    void disableScissor() { raw.disableScissor(); }
    void setTooltipForNextFrame(Font font, List<FormattedCharSequence> lines, int x, int y) { raw.renderTooltip(font, lines, x, y); }

    /** A tinted textured rectangle; {@code color} carries the alpha. */
    void blit(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight,
              int textureWidth, int textureHeight, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, (color >>> 24) / 255F);
        raw.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    /**
     * Draws a living entity standing at ({@code x}, {@code feet}) in GUI units, {@code scale}
     * pixels to a block. This is the vanilla inventory figure with fractional position and scale.
     */
    void entity(LivingEntity entity, float scale, float x, float feet, Quaternionf pose, Quaternionf camera) {
        PoseStack stack = raw.pose();
        stack.pushPose();
        stack.translate(x, feet, 50.0);
        stack.mulPose(new Matrix4f().scaling(scale, scale, -scale));
        stack.mulPose(pose);
        Lighting.setupForEntityInInventory();
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (camera != null) {
            Quaternionf conjugate = new Quaternionf(camera).conjugate();
            dispatcher.overrideCameraOrientation(conjugate);
        }
        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, stack, raw.bufferSource(), 15728880));
        raw.flush();
        dispatcher.setRenderShadow(true);
        stack.popPose();
        Lighting.setupFor3DItems();
    }
}
