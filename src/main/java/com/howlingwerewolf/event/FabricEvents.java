package com.howlingwerewolf.event;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.network.ModNetwork;
import net.fabricmc.fabric.api.entity.event.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
public final class FabricEvents {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server) -> {
            WerewolfCapabilityEvents.restoreAndSync(handler.player);
            WerewolfGuide.companions(handler.player);
        });
        EntitySleepEvents.ALLOW_SLEEPING.register((player,pos) -> WerewolfGameplayEvents.fullMoonSleep(player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> WerewolfCapabilityEvents.logout(handler.player));
        var lifecyclePhase=com.howlingwerewolf.content.ModRegistry.id("after_attachment_transfer");
        ServerPlayerEvents.AFTER_RESPAWN.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE,lifecyclePhase);
        ServerPlayerEvents.AFTER_RESPAWN.register(lifecyclePhase,(oldPlayer,player,alive) -> {
            WerewolfCapabilityEvents.copy(oldPlayer,player,alive);
            WerewolfCapabilityEvents.restoreAndSync(player);
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player,origin,destination) -> WerewolfCapabilityEvents.restoreAndSync(player));
        EntityTrackingEvents.START_TRACKING.register((entity,observer) -> {
            if (entity instanceof ServerPlayer target) WerewolfApi.get(target).ifPresent(data ->
                ModNetwork.sendToPlayer(observer,ModNetwork.createSyncPacket(target.getId(),data)));
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity,source,amount) -> {
            var context=new GameplayContext.Damage(entity,source,amount);
            WerewolfGameplayEvents.preventDeathWithBadge(context);
            return !context.isCanceled();
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity,source) ->
            WerewolfGameplayEvents.livingDeathExperience(new GameplayContext.Damage(entity,source,0)));
        AttackEntityCallback.EVENT.register((player,world,hand,target,hit) -> {
            if (!player.isSpectator()) WerewolfGameplayEvents.prepareClawAttack(new GameplayContext.Attack(player));
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player,world,hand,hit) -> {
            var context=new GameplayContext.Interaction(player,hit.getBlockPos());
            WerewolfGameplayEvents.restrictCake(context);
            return context.isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
    }
    private FabricEvents() { }
}
