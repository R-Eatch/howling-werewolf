package com.howlingwerewolf.client;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.player.AbstractClientPlayer;
public final class RenderStateHooks {
    public static void extract(LivingEntity entity,LivingEntityRenderState state,float delta) {
        if (!(state instanceof HumanoidRenderState humanoid)) return;
        var data=WerewolfRenderData.forEntity(entity,delta);
        if (entity instanceof AbstractClientPlayer player) ClientEvents.extractMouthItem(player,data.mouthItem());
        humanoid.setData(WerewolfRenderData.KEY,data);
        if (data.preview() && state instanceof AvatarRenderState avatar) { avatar.nameTag=null;avatar.scoreText=null; }
    }
    private RenderStateHooks() { }
}
