package com.howlingwerewolf.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
public final class ArmRenderContext {
    private final AbstractClientPlayer player;
    private final HumanoidArm arm;
    private final PoseStack pose;
    private final MultiBufferSource buffers;
    private final int light;
    private boolean canceled;
    public ArmRenderContext(AbstractClientPlayer player,HumanoidArm arm,PoseStack pose,MultiBufferSource buffers,int light) {
        this.player=player;this.arm=arm;this.pose=pose;this.buffers=buffers;this.light=light;
    }
    public AbstractClientPlayer getPlayer() { return player; }
    public HumanoidArm getArm() { return arm; }
    public PoseStack getPoseStack() { return pose; }
    public MultiBufferSource getMultiBufferSource() { return buffers; }
    public int getPackedLight() { return light; }
    public void setCanceled(boolean value) { canceled=value; }
    public boolean isCanceled() { return canceled; }
}
