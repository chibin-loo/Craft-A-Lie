package com.artlu.craftalie.game;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class Sounds {
    private Sounds() {
    }

    // ---------- Game moments ----------

    public static void gameStart(MinecraftServer server) {
        all(server, SoundEvents.RAID_HORN, 0.6f, 1.0f);
    }

    public static void roleReveal(ServerPlayer player, boolean imposter) {
        if (imposter) {
            play(player, SoundEvents.ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
        } else {
            play(player, SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    public static void newRound(MinecraftServer server) {
        all(server, SoundEvents.NOTE_BLOCK_CHIME, 1.0f, 1.0f);
    }

    /** Gets higher-pitched as the timer gets closer to zero. */
    public static void countdown(MinecraftServer server, int secondsLeft) {
        float pitch = 0.5f + (5 - secondsLeft) * 0.25f; // 0.5 at 5s up to 1.5 at 1s
        all(server, SoundEvents.NOTE_BLOCK_PLING, 1.0f, pitch);
    }

    public static void timesUp(MinecraftServer server) {
        all(server, SoundEvents.BELL_BLOCK, 1.0f, 1.0f);
    }

    public static void voteStart(MinecraftServer server) {
        all(server, SoundEvents.BELL_RESONATE, 1.0f, 1.0f);
    }

    public static void voteCast(ServerPlayer voter) {
        play(voter, SoundEvents.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    public static void caught(MinecraftServer server) {
        all(server, SoundEvents.ANVIL_LAND, 0.8f, 1.0f);
    }

    public static void gameOver(MinecraftServer server, boolean imposterWins) {
        if (imposterWins) {
            all(server, SoundEvents.ENDER_DRAGON_GROWL, 0.7f, 1.0f);
        } else {
            all(server, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public static void knockedOut(ServerPlayer player) {
        play(player, SoundEvents.TOTEM_USE, 0.8f, 1.0f);
    }

    // ---------- Plumbing ----------
    // Minecraft stores some sounds as SoundEvent and some as Holder<SoundEvent>.
    // Having both versions of each method means either kind just works.

    private static void all(MinecraftServer server, SoundEvent sound, float volume, float pitch) {
        all(server, Holder.direct(sound), volume, pitch);
    }

    private static void all(MinecraftServer server, Holder<SoundEvent> sound, float volume, float pitch) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            play(player, sound, volume, pitch);
        }
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        play(player, Holder.direct(sound), volume, pitch);
    }

    /** Plays a sound only this player hears, right at their position. */
    private static void play(ServerPlayer player, Holder<SoundEvent> sound, float volume, float pitch) {
        if (!GameSettings.soundsEnabled) {
            return;
        }
        player.connection.send(new ClientboundSoundPacket(
                sound, SoundSource.MASTER,
                player.getX(), player.getY(), player.getZ(),
                volume, pitch, player.getRandom().nextLong()));
    }
}