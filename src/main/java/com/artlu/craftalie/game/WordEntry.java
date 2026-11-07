package com.artlu.craftalie.game;

import java.util.List;
import java.util.Locale;

public record WordEntry(String word, String hint, WordCategory category,
        Difficulty difficulty, List<String> aliases) {

    /**
     * True if a guess matches the word or any alias, ignoring case, spaces and
     * underscores.
     */
    public boolean matches(String guess) {
        String g = normalize(guess);
        if (g.equals(normalize(word))) {
            return true;
        }
        for (String alias : aliases) {
            if (g.equals(normalize(alias))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "").trim();
    }
}