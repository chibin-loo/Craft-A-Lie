package com.artlu.craftalie.game;

public final class GameSettings {
    public enum PickaxeMode { SILK_TOUCH, REGULAR, BOTH }

    // Timing
    public static int gatherSeconds = 30; // 300 for real games

    // Kit
    public static boolean clearInventoryOnGameStart = true;
    public static PickaxeMode pickaxeMode = PickaxeMode.BOTH;
    public static boolean giveSword = true;
    public static boolean giveAxe = true;
    public static boolean giveShovel = true;
    public static int efficiencyLevel = 3;      // 0 = none
    public static boolean unbreakableTools = true;
    public static boolean giveElytra = true;
    public static int rocketCount = 2;         // 0 = none, max 64
    public static int foodCount = 64;           // steak, 0 = none

    private GameSettings() {}
}