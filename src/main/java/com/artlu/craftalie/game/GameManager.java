package com.artlu.craftalie.game;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class GameManager {
    /** Set to 3 for real games. 1 lets you test alone. */
    public static final int MIN_PLAYERS = 1;

    private static final Random RANDOM = new Random();

    private static GamePhase phase = GamePhase.WAITING;
    private static int round = 0;
    private static WordEntry currentWord = null;
    private static UUID imposterId = null;

    private GameManager() {}

    public static GamePhase getPhase() { return phase; }
    public static int getRound() { return round; }
    public static boolean isRunning() { return phase != GamePhase.WAITING; }

    public static boolean isImposter(ServerPlayer player) {
        return player.getUUID().equals(imposterId);
    }

    public static void start(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        currentWord = WordList.random(RANDOM);
        imposterId = pickImposter(players);
        phase = GamePhase.GATHERING;
        round = 1;

        broadcast(server, "Craft-A-Lie has started! Round " + round + " — go find something!");
        for (ServerPlayer player : players) {
            revealRole(player);
        }
    }

    private static UUID pickImposter(List<ServerPlayer> players) {
        if (players.size() == 1) {
            // Solo testing: 50/50 so you can see both screens
            return RANDOM.nextBoolean() ? players.get(0).getUUID() : null;
        }
        return players.get(RANDOM.nextInt(players.size())).getUUID();
    }

    public static void revealRole(ServerPlayer player) {
        if (isImposter(player)) {
            ScreenText.title(player,
                Component.literal("IMPOSTER").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.literal("Hint: " + currentWord.hint()).withStyle(ChatFormatting.GOLD),
                10, 80, 20);
            player.sendSystemMessage(Component.literal(
                "You are the IMPOSTER. Hint: " + currentWord.hint() + ". Blend in!")
                .withStyle(ChatFormatting.RED));
        } else {
            ScreenText.title(player,
                Component.literal(currentWord.word()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                Component.literal("You are NOT the imposter").withStyle(ChatFormatting.GRAY),
                10, 80, 20);
            player.sendSystemMessage(Component.literal(
                "Your word is: " + currentWord.word() + ". Find something related!")
                .withStyle(ChatFormatting.GREEN));
        }
    }

    public static void stop(MinecraftServer server) {
        reset();
        broadcast(server, "Craft-A-Lie has been stopped.");
    }

    public static void reset() {
        phase = GamePhase.WAITING;
        round = 0;
        currentWord = null;
        imposterId = null;
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}