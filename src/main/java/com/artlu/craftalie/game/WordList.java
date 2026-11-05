package com.artlu.craftalie.game;

import java.util.List;
import java.util.Random;

public final class WordList {
    private static final List<WordEntry> WORDS = List.of(
        new WordEntry("Bread", "Food"),
        new WordEntry("Creeper", "Mob"),
        new WordEntry("Lava", "Nether"),
        new WordEntry("Redstone", "Mechanics"),
        new WordEntry("Ocean", "Biome"),
        new WordEntry("Diamond", "Ore")
    );

    private WordList() {}

    public static WordEntry random(Random random) {
        return WORDS.get(random.nextInt(WORDS.size()));
    }
}