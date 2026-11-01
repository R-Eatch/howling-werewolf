package com.howlingwerewolf.client;

import com.howlingwerewolf.content.ModItems;
import com.howlingwerewolf.trial.RitualAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Renders offerings and the original badge/pearl convergence using extracted item states. */
public final class RitualAltarRenderer implements BlockEntityRenderer<RitualAltarBlockEntity, RitualAltarRenderer.State> {
    private final ItemModelResolver itemModelResolver;
    public RitualAltarRenderer(BlockEntityRendererProvider.Context context) { itemModelResolver = context.itemModelResolver(); }
    public static final class State extends BlockEntityRenderState {
        boolean activation;
        float ritualTicks;
        float time;
        final ItemStackRenderState offering = new ItemStackRenderState();
        final ItemStackRenderState badge = new ItemStackRenderState();
        final ItemStackRenderState pearl = new ItemStackRenderState();
    }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(RitualAltarBlockEntity altar, State state, float partialTick,
                                              Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(altar, state, partialTick, cameraPosition, breaking);
        state.activation = altar.isCentral() && altar.getTrialPhase() == RitualAltarBlockEntity.PHASE_ACTIVATION;
        state.ritualTicks = altar.getRitualTicks() + partialTick;
        state.time = altar.getLevel() == null ? 0.0F : altar.getLevel().getGameTime() + partialTick;
        itemModelResolver.updateForTopItem(state.offering, altar.getOffering(), ItemDisplayContext.GROUND,
                altar.getLevel(), null, 0);
        itemModelResolver.updateForTopItem(state.badge, new ItemStack(ModItems.ALPHA_WEREWOLF_BADGE.get()),
                ItemDisplayContext.GROUND, altar.getLevel(), null, 0);
        itemModelResolver.updateForTopItem(state.pearl, new ItemStack(ModItems.MOONBANE_PEARL.get()),
                ItemDisplayContext.GROUND, altar.getLevel(), null, 0);
    }
    // The activation pearls orbit three blocks away from the altar's own block bounds.
    @Override public boolean shouldRenderOffScreen() { return true; }
    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.activation) {
            float ticks = state.ritualTicks;
            float badgeRise = Math.min(1.0F, ticks / 100.0F);
            renderStack(state.badge, pose, collector, state.lightCoords,
                    0.5D, 1.0D + badgeRise * 1.35D, 0.5D, ticks * 2.4F, 0.9F);
            if (ticks < 100.0F) return;
            float progress = Math.min(1.0F, (ticks - 100.0F) / 140.0F);
            double radius = 3.0D * (1.0D - progress) + 0.35D;
            float angularSpeed = 0.05F + progress * 0.25F;
            for (int i = 0; i < 4; i++) {
                double angle = ticks * angularSpeed + i * Math.PI * 0.5D;
                renderStack(state.pearl, pose, collector, state.lightCoords,
                        0.5D + Math.cos(angle) * radius, 1.4D + progress * 1.25D,
                        0.5D + Math.sin(angle) * radius, (float) Math.toDegrees(-angle), 0.65F);
            }
        } else if (!state.offering.isEmpty()) {
            renderStack(state.offering, pose, collector, state.lightCoords,
                    0.5D, 1.0D + Math.sin(state.time * 0.08D) * 0.08D, 0.5D, state.time * 2.5F, 0.8F);
        }
    }
    private void renderStack(ItemStackRenderState item, PoseStack pose, SubmitNodeCollector collector,
                             int light, double x, double y, double z, float rotation, float scale) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.scale(scale, scale, scale);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
