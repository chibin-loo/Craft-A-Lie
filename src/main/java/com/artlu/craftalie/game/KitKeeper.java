package com.artlu.craftalie.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Holds onto kit items while a player is dead, and gives them back after
 * respawn.
 */
public final class KitKeeper {
    /** slot = inventory slot (0–35), or equipment = armor/offhand slot. */
    private record Saved(int slot, EquipmentSlot equipment, ItemStack stack) {
    }

    private static final Map<UUID, List<Saved>> SAVED = new HashMap<>();

    private KitKeeper() {
    }

    /** Called right before a player dies: take kit items out so they don't drop. */
    public static void beforeDeath(ServerPlayer player) {
        if (!GameManager.isRunning() || !GameManager.isParticipant(player) || !GameSettings.keepKitOnDeath) {
            return;
        }
        List<Saved> saved = new ArrayList<>();

        // Main inventory + hotbar
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (Kit.isKit(stack)) {
                saved.add(new Saved(i, null, stack.copy()));
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        // Armor + offhand (skip MAINHAND: that's just a hotbar slot we already checked)
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (Kit.isKit(stack)) {
                saved.add(new Saved(-1, slot, stack.copy()));
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        }

        if (!saved.isEmpty()) {
            SAVED.put(player.getUUID(), saved);
        }
    }

    /**
     * Every tick: anyone who's alive again (respawned, or saved by a totem) gets
     * their kit back.
     */
    public static void tick(MinecraftServer server) {
        if (SAVED.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, List<Saved>>> it = SAVED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, List<Saved>> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.isDeadOrDying()) {
                continue; // still on the death screen, or offline: try again later
            }
            restore(player, entry.getValue());
            it.remove();
        }
    }

    private static void restore(ServerPlayer player, List<Saved> items) {
        for (Saved saved : items) {
            if (saved.equipment() != null && player.getItemBySlot(saved.equipment()).isEmpty()) {
                player.setItemSlot(saved.equipment(), saved.stack());
            } else if (saved.equipment() == null && player.getInventory().getItem(saved.slot()).isEmpty()) {
                player.getInventory().setItem(saved.slot(), saved.stack());
            } else {
                player.getInventory().placeItemBackInInventory(saved.stack(), Prediction.SERVER_ONLY);
            }
        }
    }

    public static void clear() {
        SAVED.clear();
    }
}