package com.artlu.craftalie.game;

public final class GameSettings {
    public enum ToolEnchant { NONE, SILK_TOUCH, FORTUNE }

    // Timing
    public static int gatherSeconds = 30; // 300 for real games

    // Kit
    public static boolean clearInventoryEachRound = true;
    public static boolean giveDiamondTools = true;
    public static ToolEnchant toolEnchant = ToolEnchant.SILK_TOUCH;
    public static int efficiencyLevel = 3; // 0 = none
    public static boolean giveElytra = true;
    public static int rocketCount = 16;   // 0 = none, max 64
    public static int foodCount = 16;     // steak

    private GameSettings() {}
}