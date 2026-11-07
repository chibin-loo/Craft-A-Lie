package com.artlu.craftalie;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.artlu.craftalie.game.Difficulty;
import com.artlu.craftalie.game.GameManager;
import com.artlu.craftalie.game.GameSettings;
import com.artlu.craftalie.game.WordCategory;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/** Builds "/craftalie set ..." and "/craftalie settings". */
public final class SettingsCommand {
    /** One line per setting, used by /craftalie settings. */
    private static final List<Supplier<String>> DESCRIPTIONS = new ArrayList<>();

    private SettingsCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildSet() {
        DESCRIPTIONS.clear(); // in case commands get registered again (e.g. /reload)
        LiteralArgumentBuilder<CommandSourceStack> set = literal("set");

        // Players & timing
        set.then(intSetting("minplayers", 1, 16, () -> GameSettings.minPlayers, v -> GameSettings.minPlayers = v));
        set.then(intSetting("gather", 10, 3600, () -> GameSettings.gatherSeconds, v -> GameSettings.gatherSeconds = v));
        set.then(intSetting("meeting", 10, 600, () -> GameSettings.meetingSeconds,
                v -> GameSettings.meetingSeconds = v));
        set.then(intSetting("vote", 10, 300, () -> GameSettings.voteSeconds, v -> GameSettings.voteSeconds = v));
        set.then(intSetting("guess", 10, 120, () -> GameSettings.guessSeconds, v -> GameSettings.guessSeconds = v));
        set.then(intSetting("rounds", 1, 10, () -> GameSettings.totalRounds, v -> GameSettings.totalRounds = v));

        // Words
        set.then(enumSetting("clue", GameSettings.ImposterClue.values(),
                () -> GameSettings.imposterClue, v -> GameSettings.imposterClue = v));
        set.then(toggleSet("category", WordCategory.values(), GameSettings.enabledCategories));
        set.then(toggleSet("difficulty", Difficulty.values(), GameSettings.enabledDifficulties));

        // Kit
        set.then(boolSetting("clearinventory", () -> GameSettings.clearInventoryOnGameStart,
                v -> GameSettings.clearInventoryOnGameStart = v));
        set.then(enumSetting("pickaxe", GameSettings.PickaxeMode.values(),
                () -> GameSettings.pickaxeMode, v -> GameSettings.pickaxeMode = v));
        set.then(boolSetting("sword", () -> GameSettings.giveSword, v -> GameSettings.giveSword = v));
        set.then(boolSetting("axe", () -> GameSettings.giveAxe, v -> GameSettings.giveAxe = v));
        set.then(boolSetting("shovel", () -> GameSettings.giveShovel, v -> GameSettings.giveShovel = v));
        set.then(intSetting("efficiency", 0, 5, () -> GameSettings.efficiencyLevel,
                v -> GameSettings.efficiencyLevel = v));
        set.then(boolSetting("unbreakable", () -> GameSettings.unbreakableTools,
                v -> GameSettings.unbreakableTools = v));
        set.then(boolSetting("elytra", () -> GameSettings.giveElytra, v -> GameSettings.giveElytra = v));
        set.then(intSetting("rockets", 0, 64, () -> GameSettings.rocketCount, v -> GameSettings.rocketCount = v));
        set.then(boolSetting("refreshrockets", () -> GameSettings.refreshRocketsEachRound,
                v -> GameSettings.refreshRocketsEachRound = v));
        set.then(intSetting("food", 0, 64, () -> GameSettings.foodCount, v -> GameSettings.foodCount = v));

        return set;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildList() {
        return literal("settings").executes(context -> {
            context.getSource().sendSuccess(
                    () -> Component.literal("Craft-A-Lie settings:").withStyle(ChatFormatting.GOLD), false);
            for (Supplier<String> line : DESCRIPTIONS) {
                context.getSource().sendSuccess(() -> Component.literal("  " + line.get()), false);
            }
            return 1;
        });
    }

    // ---------- Builders for each kind of setting ----------

    private static LiteralArgumentBuilder<CommandSourceStack> intSetting(
            String name, int min, int max, IntSupplier get, IntConsumer set) {
        DESCRIPTIONS.add(() -> name + ": " + get.getAsInt());
        return literal(name)
                .executes(ctx -> show(ctx, name, String.valueOf(get.getAsInt())))
                .then(argument("value", IntegerArgumentType.integer(min, max)).executes(ctx -> {
                    if (lockedDuringGame(ctx))
                        return 0;
                    int value = IntegerArgumentType.getInteger(ctx, "value");
                    set.accept(value);
                    return announce(ctx, name, String.valueOf(value));
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> boolSetting(
            String name, BooleanSupplier get, Consumer<Boolean> set) {
        DESCRIPTIONS.add(() -> name + ": " + get.getAsBoolean());
        return literal(name)
                .executes(ctx -> show(ctx, name, String.valueOf(get.getAsBoolean())))
                .then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                    if (lockedDuringGame(ctx))
                        return 0;
                    boolean value = BoolArgumentType.getBool(ctx, "value");
                    set.accept(value);
                    return announce(ctx, name, String.valueOf(value));
                }));
    }

    private static <E extends Enum<E>> LiteralArgumentBuilder<CommandSourceStack> enumSetting(
            String name, E[] values, Supplier<E> get, Consumer<E> set) {
        DESCRIPTIONS.add(() -> name + ": " + lower(get.get()));
        LiteralArgumentBuilder<CommandSourceStack> node = literal(name)
                .executes(ctx -> show(ctx, name, lower(get.get())));
        for (E value : values) {
            node.then(literal(lower(value)).executes(ctx -> {
                if (lockedDuringGame(ctx))
                    return 0;
                set.accept(value);
                return announce(ctx, name, lower(value));
            }));
        }
        return node;
    }

    /** For on/off lists like categories: /craftalie set category mob off */
    private static <E extends Enum<E>> LiteralArgumentBuilder<CommandSourceStack> toggleSet(
            String name, E[] values, Set<E> enabled) {
        DESCRIPTIONS.add(() -> name + ": " + describeSet(enabled));
        LiteralArgumentBuilder<CommandSourceStack> node = literal(name)
                .executes(ctx -> show(ctx, name, describeSet(enabled)));
        for (E value : values) {
            node.then(literal(lower(value))
                    .then(argument("on", BoolArgumentType.bool()).executes(ctx -> {
                        if (lockedDuringGame(ctx))
                            return 0;
                        boolean on = BoolArgumentType.getBool(ctx, "on");
                        if (on) {
                            enabled.add(value);
                        } else {
                            enabled.remove(value);
                        }
                        return announce(ctx, name + " " + lower(value), on ? "on" : "off");
                    })));
        }
        return node;
    }

    // ---------- Helpers ----------

    private static boolean lockedDuringGame(CommandContext<CommandSourceStack> ctx) {
        if (GameManager.isRunning()) {
            ctx.getSource().sendFailure(Component.literal("Change settings between games."));
            return true;
        }
        return false;
    }

    private static int show(CommandContext<CommandSourceStack> ctx, String name, String value) {
        ctx.getSource().sendSuccess(() -> Component.literal(name + " is " + value), false);
        return 1;
    }

    private static int announce(CommandContext<CommandSourceStack> ctx, String name, String value) {
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("[Craft-A-Lie] " + name + " set to " + value).withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static String describeSet(Set<? extends Enum<?>> set) {
        if (set.isEmpty())
            return "(none — any word allowed)";
        List<String> names = new ArrayList<>();
        for (Enum<?> e : set)
            names.add(lower(e));
        return String.join(", ", names);
    }

    private static String lower(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}