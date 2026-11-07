package com.artlu.craftalie.game;

import java.util.EnumSet;
import java.util.Set;

public final class GameSettings {
    public enum PickaxeMode {
        SILK_TOUCH, REGULAR, BOTH
    }

    public enum ImposterClue {
        HINT, CATEGORY, BOTH
    }

    // Players & timing
    public static int minPlayers = 3;
    public static int gatherSeconds = 300;
    public static int meetingSeconds = 60;
    public static int voteSeconds = 45;
    public static int guessSeconds = 30;
    public static int totalRounds = 2;

    // Words
    public static ImposterClue imposterClue = ImposterClue.HINT;
    public static Set<WordCategory> enabledCategories = EnumSet.allOf(WordCategory.class);
    public static Set<Difficulty> enabledDifficulties = EnumSet.allOf(Difficulty.class);

    // Kit
    public static boolean clearInventoryOnGameStart = true;
    public static PickaxeMode pickaxeMode = PickaxeMode.BOTH;
    public static boolean giveSword = true;
    public static boolean giveAxe = true;
    public static boolean giveShovel = true;
    public static int efficiencyLevel = 3;
    public static boolean unbreakableTools = true;
    public static boolean giveElytra = true;
    public static int rocketCount = 2;
    public static int foodCount = 64;
    public static boolean refreshRocketsEachRound = true;
    public static boolean allowPvp = false;

    private GameSettings() {
    }
}