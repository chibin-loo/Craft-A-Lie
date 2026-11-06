package com.artlu.craftalie.game;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public final class Kit {
    private Kit() {}

    public static void give(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        if (GameSettings.clearInventoryEachRound) {
            inventory.clearContent();
        }

        if (GameSettings.giveDiamondTools) {
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
            ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
            ItemStack shovel = new ItemStack(Items.DIAMOND_SHOVEL);

            // Enchantments are data-driven, so we look them up from the world's registry
            var enchantments = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

            for (ItemStack tool : List.of(pickaxe, axe, shovel)) {
                switch (GameSettings.toolEnchant) {
                    case SILK_TOUCH -> tool.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
                    case FORTUNE -> tool.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), 3);
                    case NONE -> { }
                }
                if (GameSettings.efficiencyLevel > 0) {
                    tool.enchant(enchantments.getOrThrow(Enchantments.EFFICIENCY), GameSettings.efficiencyLevel);
                }
            }

            inventory.setItem(0, sword);
            inventory.setItem(1, pickaxe);
            inventory.setItem(2, axe);
            inventory.setItem(3, shovel);
        }

        if (GameSettings.giveElytra) {
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
        }

        if (GameSettings.rocketCount > 0) {
            int rockets = Math.min(GameSettings.rocketCount, 64);
            inventory.setItem(4, new ItemStack(Items.FIREWORK_ROCKET, rockets));
        }

        if (GameSettings.foodCount > 0) {
            inventory.setItem(8, new ItemStack(Items.COOKED_BEEF, Math.min(GameSettings.foodCount, 64)));
        }
    }
}