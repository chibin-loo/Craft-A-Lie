package com.artlu.craftalie;

import static net.minecraft.commands.Commands.literal;

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
                .then(literal("start").executes(CraftALieCommands::start))
                .then(literal("stop").executes(CraftALieCommands::stop))
        );
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.getServer().getPlayerList().broadcastSystemMessage(
            Component.literal("Craft-A-Lie is starting!"), false
        );
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.getServer().getPlayerList().broadcastSystemMessage(
            Component.literal("Craft-A-Lie has been stopped."), false
        );
        return 1;
    }
}