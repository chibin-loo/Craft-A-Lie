package com.artlu.craftalie.game;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.levelgen.Heightmap;

public final class GameManager {
    /** Set to 3 for real games. 1 lets you test alone. */
    public static final int MIN_PLAYERS = 1;
    /** Gather time in seconds. 30 for testing, 300 (5 min) for real games. */
    /** Where everyone meets up, and how far apart they stand. */
    private static final int MEETING_X = 0;
    private static final int MEETING_Z = 0;
    private static final double CIRCLE_RADIUS = 4.0;

    private static final Random RANDOM = new Random();
    private static final ServerBossEvent TIMER_BAR = new ServerBossEvent(
        UUID.randomUUID(),
        Component.literal("Gather time"),
        BossEvent.BossBarColor.GREEN,
        BossEvent.BossBarOverlay.PROGRESS
    );

    private static GamePhase phase = GamePhase.WAITING;
    private static int round = 0;
    private static WordEntry currentWord = null;
    private static UUID imposterId = null;
    private static int ticksLeft = 0;

    private GameManager() {}

    public static GamePhase getPhase() { return phase; }
    public static int getRound() { return round; }
    public static boolean isRunning() { return phase != GamePhase.WAITING; }

    public static boolean isImposter(ServerPlayer player) {
        return player.getUUID().equals(imposterId);
    }

    // ---------- Starting ----------

    public static void start(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        currentWord = WordList.random(RANDOM);
        imposterId = pickImposter(players);
        round = 1;

        broadcast(server, "Craft-A-Lie has started! Round " + round + " — go find something!");
        for (ServerPlayer player : players) {
            revealRole(player);
            Kit.give(player);
        }
        startGathering(server);
    }

    private static UUID pickImposter(List<ServerPlayer> players) {
        if (players.size() == 1) {
            return RANDOM.nextBoolean() ? players.get(0).getUUID() : null;
        }
        return players.get(RANDOM.nextInt(players.size())).getUUID();
    }

    // ---------- Gathering phase ----------

    private static void startGathering(MinecraftServer server) {
        phase = GamePhase.GATHERING;
        ticksLeft = GameSettings.gatherSeconds * 20;

        TIMER_BAR.setProgress(1.0f);
        TIMER_BAR.setName(Component.literal("Gather time: " + formatTime(GameSettings.gatherSeconds)));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            TIMER_BAR.addPlayer(player);
        }
    }

    /** Called 20 times per second by Fabric. */
    public static void tick(MinecraftServer server) {
        if (phase != GamePhase.GATHERING) {
            return;
        }

        ticksLeft--;

        // Only update the bar once per second; no need to do it every tick
        if (ticksLeft % 20 == 0) {
            int secondsLeft = ticksLeft / 20;
            TIMER_BAR.setProgress((float) ticksLeft / (GameSettings.gatherSeconds * 20));
            TIMER_BAR.setName(Component.literal("Gather time: " + formatTime(secondsLeft)));
            if (secondsLeft == 10) {
                TIMER_BAR.setColor(BossEvent.BossBarColor.RED);
            }
        }

        if (ticksLeft <= 0) {
            endGathering(server);
        }
    }

    private static void endGathering(MinecraftServer server) {
        phase = GamePhase.MEETING;
        TIMER_BAR.removeAllPlayers();
        TIMER_BAR.setColor(BossEvent.BossBarColor.GREEN);

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        teleportToMeeting(server.overworld(), players);

        for (ServerPlayer player : players) {
            ScreenText.title(player,
                Component.literal("TIME'S UP!").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                Component.literal("Show everyone what you brought"),
                5, 60, 15);
        }
        broadcast(server, "Time's up! Hold up your item and explain how it relates.");
    }

    /** Spreads players in a circle around the meeting point, all facing the middle. */
    private static void teleportToMeeting(ServerLevel level, List<ServerPlayer> players) {
        int count = players.size();
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = MEETING_X + 0.5 + Math.cos(angle) * CIRCLE_RADIUS;
            double z = MEETING_Z + 0.5 + Math.sin(angle) * CIRCLE_RADIUS;
            int blockX = (int) Math.floor(x);
            int blockZ = (int) Math.floor(z);
            level.getChunk(blockX >> 4, blockZ >> 4); // load the chunk so the height is real
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);

            // Turn to face the center
            double dx = (MEETING_X + 0.5) - x;
            double dz = (MEETING_Z + 0.5) - z;
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;

            players.get(i).teleportTo(level, x, y, z, Set.of(), yaw, 0f, true);
        }
    }

    // ---------- Roles ----------

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

    // ---------- Stopping ----------

    public static void stop(MinecraftServer server) {
        reset();
        broadcast(server, "Craft-A-Lie has been stopped.");
    }

    public static void reset() {
        phase = GamePhase.WAITING;
        round = 0;
        currentWord = null;
        imposterId = null;
        ticksLeft = 0;
        TIMER_BAR.removeAllPlayers();
        TIMER_BAR.setColor(BossEvent.BossBarColor.GREEN);
    }

    // ---------- Helpers ----------

    private static String formatTime(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}