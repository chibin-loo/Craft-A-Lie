package com.artlu.craftalie.game;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class GameEvents {
    private GameEvents() {
    }

    public static void register() {
        // Return false = cancel the damage
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> GameManager.allowDamage(entity, source));

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player) {
                KitKeeper.beforeDeath(player);
            }
            return true; // always let the death happen normally
        });

        ServerPlayConnectionEvents.JOIN.register(
                (listener, sender, server) -> GameManager.onJoin(listener.getPlayer(), server));
        ServerPlayConnectionEvents.DISCONNECT.register(
                (listener, server) -> GameManager.onLeave(listener.getPlayer(), server));
    }
}