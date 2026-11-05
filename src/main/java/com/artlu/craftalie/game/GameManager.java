package com.artlu.craftalie.game;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class GameManager {
    private static GamePhase phase = GamePhase.WAITING;
    private static int round = 0;

    private GameManager() {}

    public static GamePhase getPhase() {
        return phase;
    }

    public static int getRound() {
        return round;
    }

    public static boolean isRunning() {
        return phase != GamePhase.WAITING;
    }

    public static void start(MinecraftServer server) {
        phase = GamePhase.GATHERING;
        round = 1;
        broadcast(server, "Craft-A-Lie has started! Round " + round + " — go find something!");
    }

    public static void stop(MinecraftServer server) {
        reset();
        broadcast(server, "Craft-A-Lie has been stopped.");
    }

    public static void reset() {
        phase = GamePhase.WAITING;
        round = 0;
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}