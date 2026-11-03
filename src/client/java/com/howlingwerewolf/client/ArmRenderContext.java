package com.howlingwerewolf.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
public final class ArmRenderContext {
    private final AbstractClientPlayer player;
    private final HumanoidArm arm;
    private final PoseStack pose;
    private final SubmitNodeCollector collector;
    private final int light;
    private boolean canceled;
    public ArmRenderContext(AbstractClientPlayer player,HumanoidArm arm,PoseStack pose,SubmitNodeCollector collector,int light) {
        this.player=player;this.arm=arm;this.pose=pose;this.collector=collector;this.light=light;
    }
    public AbstractClientPlayer getPlayer() { return player; }
    public HumanoidArm getArm() { return arm; }
    public PoseStack getPoseStack() { return pose; }
    public SubmitNodeCollector getSubmitNodeCollector() { return collector; }
    public int getPackedLight() { return light; }
    public void setCanceled(boolean value) { canceled=value; }
    public boolean isCanceled() { return canceled; }
}
