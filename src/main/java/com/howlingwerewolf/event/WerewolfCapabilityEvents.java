package com.howlingwerewolf.event;
import com.howlingwerewolf.capability.*;
import com.howlingwerewolf.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;

public final class WerewolfCapabilityEvents {
    public static void copy(ServerPlayer oldPlayer,ServerPlayer player,boolean alive) {
        // Copy the value, never share a mutable progression object with the replaced player.
        var data=new WerewolfData();
        data.copyFrom(WerewolfApi.get(oldPlayer).orElseThrow());
        player.setAttached(ModAttachments.WEREWOLF,data);
        player.setAttached(ModAttachments.LEGACY,oldPlayer.getAttachedOrCreate(ModAttachments.LEGACY).copy());
        WerewolfPersistence.copyPersistentRoot(oldPlayer,player);
        if (!alive) {
            WerewolfGameplayEvents.removeSpiritWolves(player,data);
            data.prepareAfterDeath();
        }
        WerewolfPersistence.save(player,data);
        player.refreshDimensions();
    }
    public static void restoreAndSync(ServerPlayer player) {
        var data=WerewolfApi.get(player).orElseThrow();
        WerewolfPersistence.recoverIfFresh(player,data);
        WerewolfGameplayEvents.refreshWerewolfModifiers(player,data);
        player.refreshDimensions();
        ModNetwork.sync(player,data);
        com.howlingwerewolf.trial.AlphaTrialManager.deliverPendingFailureMessage(player);
    }
    public static void logout(ServerPlayer player) {
        com.howlingwerewolf.trial.AlphaTrialManager.onPlayerLogout(player);
        var data=WerewolfApi.get(player).orElseThrow();
        WerewolfGameplayEvents.removeSpiritWolves(player,data);
        WerewolfPersistence.save(player,data);
        WerewolfGameplayEvents.clearTransientTracking(player);
    }
    private WerewolfCapabilityEvents() { }
}
