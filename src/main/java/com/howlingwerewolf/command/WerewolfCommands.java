package com.howlingwerewolf.command;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.WerewolfAbility;
import com.howlingwerewolf.WerewolfTreeSkill;
import com.howlingwerewolf.capability.WerewolfApi;
import com.howlingwerewolf.capability.WerewolfData;
import com.howlingwerewolf.capability.WerewolfPersistence;
import com.howlingwerewolf.event.WerewolfGameplayEvents;
import com.howlingwerewolf.network.ModNetwork;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HowlingWerewolf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WerewolfCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("werewolf").requires(source -> source.hasPermission(2))
                .then(Commands.literal("infect").then(target(ctx -> infect(ctx, player(ctx)))))
                .then(Commands.literal("awaken").then(target(ctx -> awaken(ctx, player(ctx)))))
                .then(Commands.literal("cure").then(target(ctx -> cure(ctx, player(ctx)))))
                .then(Commands.literal("transform").then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> transform(ctx, player(ctx), null))
                        .then(Commands.argument("state", BoolArgumentType.bool())
                                .executes(ctx -> transform(ctx, player(ctx), BoolArgumentType.getBool(ctx, "state"))))))
                .then(Commands.literal("setlevel").then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, WerewolfData.MAX_LEVEL_LIMIT))
                                .executes(ctx -> setLevel(ctx, player(ctx), IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("addxp").then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100000))
                                .executes(ctx -> addExperience(ctx, player(ctx), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("setskillpoints").then(pointsArgument(false)))
                .then(Commands.literal("settreepoints").then(pointsArgument(true)))
                .then(Commands.literal("settree").then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("skill", StringArgumentType.word())
                                .then(Commands.argument("rank", IntegerArgumentType.integer(0,
                                                WerewolfTreeSkill.maximumRank()))
                                        .executes(ctx -> setTree(ctx, player(ctx), StringArgumentType.getString(ctx, "skill"),
                                                IntegerArgumentType.getInteger(ctx, "rank")))))))
                .then(Commands.literal("setability").then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("ability", StringArgumentType.word())
                                .then(Commands.argument("unlocked", BoolArgumentType.bool())
                                        .executes(ctx -> setAbility(ctx, player(ctx), StringArgumentType.getString(ctx, "ability"),
                                                BoolArgumentType.getBool(ctx, "unlocked")))))))
                .then(Commands.literal("resettree").then(target(ctx -> mutate(ctx, player(ctx), WerewolfData::resetTreeSkills, "tree reset"))))
                .then(Commands.literal("resetabilities").then(target(ctx -> mutate(ctx, player(ctx), WerewolfData::resetAbilities, "abilities reset"))))
                .then(Commands.literal("reset").then(target(ctx -> reset(ctx, player(ctx)))))
                .then(Commands.literal("forcemoon").then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("state", BoolArgumentType.bool())
                                .executes(ctx -> forceMoon(ctx, player(ctx), BoolArgumentType.getBool(ctx, "state"))))))
                .then(Commands.literal("status").executes(ctx -> status(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> status(ctx, player(ctx))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ?> target(
            com.mojang.brigadier.Command<CommandSourceStack> command) {
        return Commands.argument("target", EntityArgument.player()).executes(command);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ?> pointsArgument(boolean tree) {
        return Commands.argument("target", EntityArgument.player())
                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setPoints(ctx, player(ctx), IntegerArgumentType.getInteger(ctx, "amount"), tree)));
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return EntityArgument.getPlayer(ctx, "target");
    }

    private static int infect(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        return mutate(ctx, player, data -> {
            WerewolfGameplayEvents.removeSpiritWolves(player, data);
            WerewolfGameplayEvents.infect(player, data);
        }, "infected");
    }

    private static int awaken(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        return mutate(ctx, player, data -> WerewolfGameplayEvents.awaken(player, data), "awakened");
    }

    private static int cure(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        return mutate(ctx, player, data -> {
            WerewolfGameplayEvents.removeSpiritWolves(player, data);
            WerewolfGameplayEvents.removeWerewolfModifiers(player);
            data.reset();
        }, "cured");
    }

    private static int transform(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Boolean desired) {
        return mutate(ctx, player, data -> {
            boolean transforming = desired == null ? !data.isTransformed() : desired;
            if (transforming) prepareWerewolf(data);
            data.setTransformed(transforming);
            data.setMoonForced(false);
        }, "transformation changed");
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx, ServerPlayer player, int level) {
        if (level > WerewolfData.getMaxLevel()) {
            ctx.getSource().sendFailure(Component.literal("Configured maximum werewolf level is "
                    + WerewolfData.getMaxLevel() + "."));
            return 0;
        }
        return mutate(ctx, player, data -> { prepareWerewolf(data); data.setLevel(level); }, "level=" + level);
    }

    private static int addExperience(CommandContext<CommandSourceStack> ctx, ServerPlayer player, int amount) {
        return mutate(ctx, player, data -> { prepareWerewolf(data); data.addExperience(amount); }, "xp+=" + amount);
    }

    private static int setPoints(CommandContext<CommandSourceStack> ctx, ServerPlayer player, int amount, boolean tree) {
        return mutate(ctx, player, data -> { if (tree) data.setAvailableTreePoints(amount); else data.setAvailableSkillPoints(amount); },
                (tree ? "tree points=" : "skill points=") + amount);
    }

    private static int setTree(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String name, int rank) {
        WerewolfTreeSkill skill = WerewolfTreeSkill.byName(name);
        if (skill == null) { ctx.getSource().sendFailure(Component.literal("Unknown tree skill.")); return 0; }
        if (rank > skill.maxRank()) {
            ctx.getSource().sendFailure(Component.literal(skill.id() + " maximum rank is "
                    + skill.maxRank() + "."));
            return 0;
        }
        return mutate(ctx, player, data -> data.setTreeSkillRank(skill, rank), skill.id() + "=" + rank);
    }

    private static int setAbility(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String name, boolean unlocked) {
        WerewolfAbility ability = WerewolfAbility.byName(name);
        if (ability == null) { ctx.getSource().sendFailure(Component.literal("Unknown ability.")); return 0; }
        return mutate(ctx, player, data -> data.setAbility(ability, unlocked), ability.id() + "=" + unlocked);
    }

    private static int reset(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        return mutate(ctx, player, data -> {
            WerewolfGameplayEvents.removeSpiritWolves(player, data);
            WerewolfGameplayEvents.removeWerewolfModifiers(player);
            data.reset();
        }, "all data reset");
    }

    private static int forceMoon(CommandContext<CommandSourceStack> ctx, ServerPlayer player, boolean state) {
        return mutate(ctx, player, data -> {
            if (state) {
                prepareWerewolf(data);
                data.setTransformed(true);
            }
            data.setMoonForced(state);
        }, "moonForced=" + state);
    }

    private static void prepareWerewolf(WerewolfData data) {
        data.setInfected(false);
        data.setAwakeningDayTime(-1L);
        data.setWerewolf(true);
    }

    private static int status(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        net.minecraftforge.common.util.LazyOptional<WerewolfData> optional = WerewolfApi.get(player);
        if (!optional.isPresent()) {
            ctx.getSource().sendFailure(Component.literal("Werewolf capability is unavailable for "
                    + player.getGameProfile().getName() + ". Reconnect and check the server log."));
            return 0;
        }
        optional.ifPresent(data -> {
            net.minecraft.nbt.CompoundTag mirror = WerewolfPersistence.readSnapshot(player);
            net.minecraft.nbt.CompoundTag liveForCompare = data.serializeNBT();
            if (mirror != null) mirror.remove("TotemExperienceTicks");
            liveForCompare.remove("TotemExperienceTicks");
            boolean mirrorMatches = mirror != null && mirror.equals(liveForCompare);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Werewolf " + player.getGameProfile().getName() + ": werewolf=" + data.isWerewolf()
                            + ", infected=" + data.isInfected() + ", form=" + data.getForm().id()
                            + ", alphaDefeated=" + data.hasDefeatedAlpha() + ", level=" + data.getLevel()
                            + ", xp=" + data.getExperience() + "/" + WerewolfData.experienceForNextLevel(data.getLevel())
                            + ", skillPoints=" + data.getAvailableSkillPoints() + ", treePoints=" + data.getAvailableTreePoints()
                            + ", dimension=" + player.level().dimension().location()
                            + ", persistedMirror=" + (mirror == null ? "missing" : mirrorMatches ? "match" : "DIFF")), false);
        });
        return 1;
    }

    private static int mutate(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                              java.util.function.Consumer<WerewolfData> action, String result) {
        net.minecraftforge.common.util.LazyOptional<WerewolfData> optional = WerewolfApi.get(player);
        if (!optional.isPresent()) {
            ctx.getSource().sendFailure(Component.literal("Werewolf capability is unavailable for "
                    + player.getGameProfile().getName() + ". Reconnect and check the server log."));
            return 0;
        }
        optional.ifPresent(data -> {
            action.accept(data);
            player.refreshDimensions();
            WerewolfGameplayEvents.refreshWerewolfModifiers(player, data);
            ModNetwork.sync(player, data);
        });
        ctx.getSource().sendSuccess(() -> Component.literal(player.getGameProfile().getName() + ": " + result), true);
        return 1;
    }

    private WerewolfCommands() {}
}
