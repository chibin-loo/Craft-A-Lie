package com.artlu.craftalie;

import net.minecraft.server.level.ServerPlayer;
import static net.minecraft.commands.Commands.literal;

import com.artlu.craftalie.game.GameManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CraftALieCommands {
    private CraftALieCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            registerCraftALie(dispatcher);
        });
    }

    private static void registerCraftALie(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("craftalie")
                .executes(CraftALieCommands::help)
                .then(literal("start").executes(CraftALieCommands::start))
                .then(literal("stop").executes(CraftALieCommands::stop))
                .then(literal("status").executes(CraftALieCommands::status))
                .then(literal("role").executes(CraftALieCommands::role))
        );
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
            () -> Component.literal("Craft-A-Lie commands: /craftalie start, stop, status"), false
        );
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (GameManager.isRunning()) {
            source.sendFailure(Component.literal("A game is already running. Use /craftalie stop first."));
            return 0;
        }

        int playerCount = source.getServer().getPlayerList().getPlayers().size();
        if (playerCount < GameManager.MIN_PLAYERS) {
            source.sendFailure(Component.literal("Need at least " + GameManager.MIN_PLAYERS + " players to start."));
            return 0;
        }

        GameManager.start(source.getServer());
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!GameManager.isRunning()) {
            source.sendFailure(Component.literal("No game is running."));
            return 0;
        }
        GameManager.stop(source.getServer());
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
            () -> Component.literal("Phase: " + GameManager.getPhase() + " | Round: " + GameManager.getRound()),
            false
        );
        return 1;
    }

    private static int role(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can use this."));
            return 0;
        }
        if (!GameManager.isRunning()) {
            source.sendFailure(Component.literal("No game is running."));
            return 0;
        }
        GameManager.revealRole(player);
        return 1;
    }
}