package com.artlu.craftalie.game;

import static com.artlu.craftalie.game.Difficulty.EASY;
import static com.artlu.craftalie.game.Difficulty.HARD;
import static com.artlu.craftalie.game.Difficulty.MEDIUM;
import static com.artlu.craftalie.game.WordCategory.BLOCK;
import static com.artlu.craftalie.game.WordCategory.ITEM;
import static com.artlu.craftalie.game.WordCategory.MECHANIC;
import static com.artlu.craftalie.game.WordCategory.MOB;

import java.util.List;
import java.util.Random;

public final class WordList {
    private static final List<WordEntry> WORDS = List.of(
            // ---------- Items ----------
            w("Bread", "Food", ITEM, EASY),
            w("Bow", "Combat", ITEM, EASY),
            w("Map", "Exploration", ITEM, EASY),
            w("Book", "Library", ITEM, EASY, "books"),
            w("Fishing Rod", "Water", ITEM, EASY, "rod"),
            w("Bucket", "Tools", ITEM, EASY),
            w("Compass", "Exploration", ITEM, MEDIUM),
            w("Clock", "Time", ITEM, MEDIUM, "watch"),
            w("Saddle", "Riding", ITEM, MEDIUM),
            w("Shears", "Farming", ITEM, MEDIUM),
            w("Golden Apple", "Food", ITEM, MEDIUM, "gapple", "golden apples"),
            w("Ender Pearl", "The End", ITEM, MEDIUM, "pearl", "ender pearls"),
            w("Totem of Undying", "Raids", ITEM, HARD, "totem"),
            w("Trident", "Ocean", ITEM, HARD),
            w("Name Tag", "Mobs", ITEM, HARD, "nametag"),
            w("Spyglass", "Exploration", ITEM, HARD, "telescope"),
            w("Brush", "Archaeology", ITEM, HARD),

            // ---------- Blocks ----------
            w("Crafting Table", "Wood", BLOCK, EASY, "workbench", "table"),
            w("Furnace", "Cooking", BLOCK, EASY),
            w("Chest", "Storage", BLOCK, EASY),
            w("TNT", "Explosions", BLOCK, EASY, "dynamite"),
            w("Bed", "Night", BLOCK, EASY),
            w("Cake", "Food", BLOCK, EASY),
            w("Glass", "Building", BLOCK, EASY),
            w("Cactus", "Desert", BLOCK, EASY),
            w("Pumpkin", "Farming", BLOCK, EASY),
            w("Ladder", "Building", BLOCK, EASY),
            w("Torch", "Light", BLOCK, EASY, "torches"),
            w("Ice", "Cold", BLOCK, EASY),
            w("Anvil", "Repair", BLOCK, MEDIUM),
            w("Note Block", "Music", BLOCK, MEDIUM, "noteblock"),
            w("Jukebox", "Music", BLOCK, MEDIUM),
            w("Obsidian", "Nether", BLOCK, MEDIUM),
            w("Bookshelf", "Enchanting", BLOCK, MEDIUM, "bookshelves"),
            w("Hay Bale", "Farming", BLOCK, MEDIUM, "hay", "hay block"),
            w("Lantern", "Light", BLOCK, MEDIUM),
            w("Beacon", "Light", BLOCK, HARD),
            w("Sponge", "Ocean", BLOCK, HARD),
            w("Slime Block", "Sticky", BLOCK, HARD, "slime"),

            // ---------- Mobs ----------
            w("Creeper", "Explosions", MOB, EASY),
            w("Cow", "Farm animals", MOB, EASY),
            w("Chicken", "Farm animals", MOB, EASY),
            w("Pig", "Farm animals", MOB, EASY),
            w("Sheep", "Farm animals", MOB, EASY),
            w("Villager", "Trading", MOB, EASY),
            w("Skeleton", "Night", MOB, EASY),
            w("Zombie", "Night", MOB, EASY),
            w("Spider", "Night", MOB, EASY),
            w("Wolf", "Pets", MOB, EASY, "dog"),
            w("Cat", "Pets", MOB, EASY),
            w("Horse", "Riding", MOB, EASY),
            w("Enderman", "The End", MOB, MEDIUM),
            w("Blaze", "Nether", MOB, MEDIUM),
            w("Ghast", "Nether", MOB, MEDIUM),
            w("Bee", "Flowers", MOB, MEDIUM, "bees"),
            w("Iron Golem", "Village", MOB, MEDIUM, "golem"),
            w("Turtle", "Ocean", MOB, MEDIUM),
            w("Axolotl", "Lush caves", MOB, HARD),
            w("Warden", "Deep dark", MOB, HARD),

            // ---------- Mechanics ----------
            w("Farming", "Food", MECHANIC, EASY, "farm"),
            w("Mining", "Caves", MECHANIC, EASY, "mine"),
            w("Fishing", "Water", MECHANIC, EASY, "fish"),
            w("Explosion", "Destruction", MECHANIC, EASY, "explosions"),
            w("Redstone", "Circuits", MECHANIC, MEDIUM),
            w("Enchanting", "Magic", MECHANIC, MEDIUM, "enchant", "enchantment", "enchantments"),
            w("Brewing", "Magic", MECHANIC, MEDIUM, "potions", "brewing stand"),
            w("Trading", "Village", MECHANIC, MEDIUM, "trade"),
            w("Smelting", "Fire", MECHANIC, MEDIUM, "smelt"),
            w("Nether Portal", "Travel", MECHANIC, MEDIUM, "portal"),
            w("Sleeping", "Night", MECHANIC, MEDIUM, "sleep"),
            w("Breeding", "Animals", MECHANIC, MEDIUM, "breed"),
            w("Raid", "Village", MECHANIC, HARD, "raids"));

    private WordList() {
    }

    /**
     * Shortcut so each word fits on one line. Extra strings are accepted guess
     * aliases.
     */
    private static WordEntry w(String word, String hint, WordCategory category,
            Difficulty difficulty, String... aliases) {
        return new WordEntry(word, hint, category, difficulty, List.of(aliases));
    }

    /**
     * Picks a random word allowed by the current settings (or any word, if the
     * filters allow none).
     */
    public static WordEntry random(Random random) {
        List<WordEntry> allowed = WORDS.stream()
                .filter(e -> GameSettings.enabledCategories.contains(e.category()))
                .filter(e -> GameSettings.enabledDifficulties.contains(e.difficulty()))
                .toList();
        List<WordEntry> pool = allowed.isEmpty() ? WORDS : allowed;
        return pool.get(random.nextInt(pool.size()));
    }
}