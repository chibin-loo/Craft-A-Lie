package com.artlu.craftalie.game;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class Kit {
    private Kit() {
    }

    public static void give(ServerPlayer player) {
        if (GameSettings.clearInventoryOnGameStart) {
            player.getInventory().clearContent();
        }

        // Tools, in hotbar order from the left
        List<ItemStack> tools = new ArrayList<>();
        GameSettings.PickaxeMode mode = GameSettings.pickaxeMode;

        if (GameSettings.giveAxe) {
            tools.add(tool(player, Items.DIAMOND_AXE));
        }
        if (mode == GameSettings.PickaxeMode.SILK_TOUCH || mode == GameSettings.PickaxeMode.BOTH) {
            ItemStack silkPick = tool(player, Items.DIAMOND_PICKAXE);
            silkPick.enchant(enchantment(player, Enchantments.SILK_TOUCH), 1);
            tools.add(silkPick);
        }
        if (mode == GameSettings.PickaxeMode.REGULAR || mode == GameSettings.PickaxeMode.BOTH) {
            tools.add(tool(player, Items.DIAMOND_PICKAXE));
        }
        if (GameSettings.giveShovel) {
            tools.add(tool(player, Items.DIAMOND_SHOVEL));
        }
        if (GameSettings.giveSword) {
            tools.add(makeUnbreakable(new ItemStack(Items.DIAMOND_SWORD)));
        }

        int slot = 0;
        for (ItemStack tool : tools) {
            put(player, slot++, tool);
        }

        // Rockets and steak at the end of the hotbar
        if (GameSettings.rocketCount > 0) {
            put(player, 7, new ItemStack(Items.FIREWORK_ROCKET, Math.min(GameSettings.rocketCount, 64)));
        }
        if (GameSettings.foodCount > 0) {
            put(player, 8, new ItemStack(Items.COOKED_BEEF, Math.min(GameSettings.foodCount, 64)));
        }

        if (GameSettings.giveElytra) {
            ItemStack current = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!current.isEmpty()) {
                player.getInventory().placeItemBackInInventory(current.copy(), Prediction.SERVER_ONLY);
            }
            player.setItemSlot(EquipmentSlot.CHEST, makeUnbreakable(new ItemStack(Items.ELYTRA)));
        }
    }

    /** Puts an item in a specific slot, or anywhere free if that slot is taken. */
    private static void put(ServerPlayer player, int slot, ItemStack stack) {
        if (player.getInventory().getItem(slot).isEmpty()) {
            player.getInventory().setItem(slot, stack);
        } else {
            player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
        }
    }

    /** Tops rockets back up to the configured amount (never gives extra). */
    public static void refreshRockets(ServerPlayer player) {
        int target = Math.min(GameSettings.rocketCount, 64);
        int have = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.FIREWORK_ROCKET)) {
                have += stack.getCount();
            }
        }
        if (have < target) {
            player.getInventory().placeItemBackInInventory(
                    new ItemStack(Items.FIREWORK_ROCKET, target - have), Prediction.SERVER_ONLY);
        }
    }

    /** A diamond tool with the chosen efficiency, unbreakable if enabled. */
    private static ItemStack tool(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        if (GameSettings.efficiencyLevel > 0) {
            stack.enchant(enchantment(player, Enchantments.EFFICIENCY), GameSettings.efficiencyLevel);
        }
        return makeUnbreakable(stack);
    }

    private static ItemStack makeUnbreakable(ItemStack stack) {
        if (GameSettings.unbreakableTools) {
            stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        }
        return stack;
    }

    /**
     * Enchantments are data-driven, so we look them up from the world's registry.
     */
    private static Holder<Enchantment> enchantment(ServerPlayer player, ResourceKey<Enchantment> key) {
        return player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }
}