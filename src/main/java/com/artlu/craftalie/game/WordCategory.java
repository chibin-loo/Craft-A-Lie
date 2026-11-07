package com.artlu.craftalie.game;

public enum WordCategory {
    ITEM("Item"),
    BLOCK("Block"),
    MOB("Mob"),
    MECHANIC("Mechanic");

    private final String displayName;

    WordCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}