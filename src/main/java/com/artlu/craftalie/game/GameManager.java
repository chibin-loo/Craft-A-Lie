package com.artlu.craftalie.game;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.levelgen.Heightmap;

public final class GameManager {

    private static final int MEETING_X = 0;
    private static final int MEETING_Z = 0;
    private static final double CIRCLE_RADIUS = 4.0;

    private static final Random RANDOM = new Random();
    private static final ServerBossEvent TIMER_BAR = new ServerBossEvent(
            UUID.randomUUID(),
            Component.literal("Gather time"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS);

    private static GamePhase phase = GamePhase.WAITING;
    private static int round = 0;
    private static WordEntry currentWord = null;
    private static UUID imposterId = null;

    private static int ticksLeft = 0;
    private static int totalTicks = 0;
    private static String timerLabel = "";

    private GameManager() {
    }

    public static GamePhase getPhase() {
        return phase;
    }

    public static int getRound() {
        return round;
    }

    public static boolean isRunning() {
        return phase != GamePhase.WAITING;
    }

    public static boolean isImposter(ServerPlayer player) {
        return player.getUUID().equals(imposterId);
    }

    // ---------- Starting ----------

    public static void start(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        currentWord = WordList.random(RANDOM);
        imposterId = pickImposter(players);
        round = 1;

        broadcast(server, "Craft-A-Lie has started! " + GameSettings.totalRounds + " rounds — go find something!");
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

    // ---------- Gather & meeting ----------

    private static void startGathering(MinecraftServer server) {
        phase = GamePhase.GATHERING;
        startTimer(server, "Round " + round + " — Gather", BossEvent.BossBarColor.GREEN, GameSettings.gatherSeconds);

        if (round > 1) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (GameSettings.refreshRocketsEachRound) {
                    Kit.refreshRockets(player);
                }
                ScreenText.title(player,
                        Component.literal("ROUND " + round).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                        Component.literal("Same word — go!"),
                        10, 50, 15);
            }
        }
    }

    private static void endGathering(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        teleportToMeeting(server.overworld(), players);

        for (ServerPlayer player : players) {
            ScreenText.title(player,
                    Component.literal("TIME'S UP!").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                    Component.literal("Show everyone what you brought"),
                    5, 60, 15);
        }
        broadcast(server, "Time's up! Hold up your item and explain how it relates.");
        startMeeting(server);
    }

    private static void startMeeting(MinecraftServer server) {
        phase = GamePhase.MEETING;
        startTimer(server, "Round " + round + " — Show & tell", BossEvent.BossBarColor.BLUE,
                GameSettings.meetingSeconds);
    }

    private static void endMeeting(MinecraftServer server) {
        if (round < GameSettings.totalRounds) {
            round++;
            broadcast(server, "Round " + round + " — same word, go find something new!");
            startGathering(server);
        } else {
            startVoting(server);
        }
    }

    // ---------- Voting ----------

    private static void startVoting(MinecraftServer server) {
        phase = GamePhase.VOTING;
        Voting.begin(onlinePlayerIds(server), false);
        startTimer(server, "Vote", BossEvent.BossBarColor.PURPLE, GameSettings.voteSeconds);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ScreenText.title(player,
                    Component.literal("VOTE!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    Component.literal("Who is the imposter? Click a name in chat"),
                    10, 60, 20);
        }
        sendBallot(server);
    }

    /** Sends a chat line where each candidate's name is a clickable vote button. */
    private static void sendBallot(MinecraftServer server) {
        MutableComponent line = Component.literal("Vote: ").withStyle(ChatFormatting.GOLD);
        for (UUID id : Voting.candidates()) {
            String name = nameOf(server, id);
            line.append(Component.literal("[" + name + "]").withStyle(style -> style
                    .withColor(ChatFormatting.AQUA)
                    .withClickEvent(new ClickEvent.RunCommand("/craftalie vote " + name))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Vote for " + name)))));
            line.append(Component.literal(" "));
        }
        server.getPlayerList().broadcastSystemMessage(line, false);
    }

    /**
     * Called by /craftalie vote. Returns an error message, or null if it worked.
     */
    public static String vote(MinecraftServer server, ServerPlayer voter, ServerPlayer target) {
        if (phase != GamePhase.VOTING) {
            return "It's not voting time.";
        }
        if (!Voting.isCandidate(target.getUUID())) {
            return target.getName().getString() + " can't be voted for right now.";
        }
        boolean solo = server.getPlayerList().getPlayers().size() == 1;
        if (!solo && voter.getUUID().equals(target.getUUID())) {
            return "You can't vote for yourself.";
        }

        boolean changedVote = Voting.hasVoted(voter.getUUID());
        Voting.cast(voter.getUUID(), target.getUUID());
        voter.sendSystemMessage(Component.literal(
                (changedVote ? "Changed your vote to " : "You voted for ") + target.getName().getString())
                .withStyle(ChatFormatting.GRAY));
        if (!changedVote) {
            broadcast(server, voter.getName().getString() + " has voted.");
        }

        if (Voting.everyoneVoted(onlinePlayerIds(server))) {
            finishVoting(server);
        }
        return null;
    }

    private static void finishVoting(MinecraftServer server) {
        broadcast(server, "Votes: " + tallyText(server));
        List<UUID> top = Voting.mostVoted();

        if (top.isEmpty()) {
            endGame(server, true, "Nobody voted — the imposter escapes!");
            return;
        }

        if (top.size() > 1) {
            if (!Voting.isRevote()) {
                broadcast(server, "It's a tie! Revote between the tied players.");
                Voting.begin(top, true);
                startTimer(server, "Revote", BossEvent.BossBarColor.PURPLE, GameSettings.voteSeconds);
                sendBallot(server);
            } else {
                endGame(server, true, "Tied again — the imposter escapes!");
            }
            return;
        }

        UUID votedOut = top.get(0);
        String votedName = nameOf(server, votedOut);
        if (votedOut.equals(imposterId)) {
            startGuessing(server, votedName);
        } else {
            endGame(server, true, votedName + " was NOT the imposter!");
        }
    }

    private static String tallyText(MinecraftServer server) {
        Map<UUID, Integer> tally = Voting.tally();
        if (tally.isEmpty()) {
            return "none";
        }
        StringBuilder text = new StringBuilder();
        for (Map.Entry<UUID, Integer> entry : tally.entrySet()) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(nameOf(server, entry.getKey())).append(" ").append(entry.getValue());
        }
        return text.toString();
    }

    // ---------- Imposter's guess ----------

    private static void startGuessing(MinecraftServer server, String imposterName) {
        phase = GamePhase.GUESSING;
        startTimer(server, "Imposter's guess", BossEvent.BossBarColor.YELLOW, GameSettings.guessSeconds);
        broadcast(server, imposterName + " WAS the imposter! They get one guess at the word to steal the win...");

        ServerPlayer imposter = server.getPlayerList().getPlayer(imposterId);
        if (imposter != null) {
            ScreenText.title(imposter,
                    Component.literal("CAUGHT!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    Component.literal("Type /craftalie guess <word>"),
                    5, 80, 15);
        }
    }

    /**
     * Called by /craftalie guess. Returns an error message, or null if it worked.
     */
    public static String guess(MinecraftServer server, ServerPlayer player, String text) {
        if (phase != GamePhase.GUESSING) {
            return "You can't guess right now.";
        }
        if (!isImposter(player)) {
            return "Only the imposter can guess.";
        }
        String attempt = text.trim();
        String name = player.getName().getString();
        if (currentWord.matches(attempt)) {
            endGame(server, true, name + " guessed \"" + attempt + "\" — correct! They steal the win!");
        } else {
            endGame(server, false, name + " guessed \"" + attempt + "\" — wrong!");
        }
        return null;
    }

    // ---------- Ending ----------

    private static void endGame(MinecraftServer server, boolean imposterWins, String reason) {
        String imposterName = nameOf(server, imposterId);
        String word = currentWord.word();

        Component title = imposterWins
                ? Component.literal("IMPOSTER WINS").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                : Component.literal("CREW WINS").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        Component subtitle = Component.literal("Imposter: " + imposterName + "  |  Word: " + word);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ScreenText.title(player, title, subtitle, 10, 100, 30);
        }
        broadcast(server, reason);
        broadcast(server, "The imposter was " + imposterName + ". The word was " + word + ".");
        reset();
    }

    // ---------- Timer ----------

    private static void startTimer(MinecraftServer server, String label, BossEvent.BossBarColor color, int seconds) {
        timerLabel = label;
        totalTicks = Math.max(1, seconds * 20);
        ticksLeft = totalTicks;

        TIMER_BAR.setColor(color);
        TIMER_BAR.setProgress(1.0f);
        TIMER_BAR.setName(Component.literal(label + ": " + formatTime(seconds)));
        TIMER_BAR.removeAllPlayers();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            TIMER_BAR.addPlayer(player);
        }
    }

    /** Called 20 times per second by Fabric. */
    public static void tick(MinecraftServer server) {
        if (phase == GamePhase.WAITING) {
            return;
        }

        ticksLeft--;

        if (ticksLeft % 20 == 0) {
            int secondsLeft = ticksLeft / 20;
            TIMER_BAR.setProgress((float) ticksLeft / totalTicks);
            TIMER_BAR.setName(Component.literal(timerLabel + ": " + formatTime(secondsLeft)));
            if (secondsLeft <= 10) {
                TIMER_BAR.setColor(BossEvent.BossBarColor.RED);
            }
        }

        if (ticksLeft <= 0) {
            switch (phase) {
                case GATHERING -> endGathering(server);
                case MEETING -> endMeeting(server);
                case VOTING -> finishVoting(server);
                case GUESSING -> endGame(server, false, "The imposter ran out of time to guess!");
                case WAITING -> {
                }
            }
        }
    }

    // ---------- Teleport ----------

    private static void teleportToMeeting(ServerLevel level, List<ServerPlayer> players) {
        int count = players.size();
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = MEETING_X + 0.5 + Math.cos(angle) * CIRCLE_RADIUS;
            double z = MEETING_Z + 0.5 + Math.sin(angle) * CIRCLE_RADIUS;

            int blockX = (int) Math.floor(x);
            int blockZ = (int) Math.floor(z);
            level.getChunk(blockX >> 4, blockZ >> 4);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);

            double dx = (MEETING_X + 0.5) - x;
            double dz = (MEETING_Z + 0.5) - z;
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;

            players.get(i).teleportTo(level, x, y, z, Set.of(), yaw, 0f, true);
        }
    }

    // ---------- Roles ----------

    public static void revealRole(ServerPlayer player) {
        if (isImposter(player)) {
            String clue = imposterClueText();
            ScreenText.title(player,
                    Component.literal("IMPOSTER").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    Component.literal(clue).withStyle(ChatFormatting.GOLD),
                    10, 80, 20);
            player.sendSystemMessage(Component.literal(
                    "You are the IMPOSTER. " + clue + ". Blend in!")
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
        totalTicks = 0;
        timerLabel = "";
        Voting.clear();
        TIMER_BAR.removeAllPlayers();
    }

    // ---------- Helpers ----------

    private static List<UUID> onlinePlayerIds(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList();
    }

    private static String nameOf(MinecraftServer server, UUID id) {
        if (id == null) {
            return "nobody";
        }
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        return player != null ? player.getName().getString() : "someone who left";
    }

    private static String formatTime(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    private static String imposterClueText() {
        return switch (GameSettings.imposterClue) {
            case HINT -> "Hint: " + currentWord.hint();
            case CATEGORY -> "Category: " + currentWord.category().displayName();
            case BOTH -> currentWord.category().displayName() + " — Hint: " + currentWord.hint();
        };
    }
}