package com.github.manu585.manusgroups.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

public final class General {
    private General() {}

    public static void runSync(final JavaPlugin plugin, final Runnable r) {
        plugin.getServer().getScheduler().runTask(plugin, r);
    }

    public static boolean isSign(Block block) {
        if (block == null) return false;
        final Material type = block.getType();
        return block.getState() instanceof Sign ||
                type == Material.OAK_SIGN || type == Material.OAK_WALL_SIGN ||
                type == Material.SPRUCE_SIGN || type == Material.SPRUCE_WALL_SIGN ||
                type == Material.BIRCH_SIGN || type == Material.BIRCH_WALL_SIGN ||
                type == Material.JUNGLE_SIGN || type == Material.JUNGLE_WALL_SIGN ||
                type == Material.ACACIA_SIGN || type == Material.ACACIA_WALL_SIGN ||
                type == Material.DARK_OAK_SIGN || type == Material.DARK_OAK_WALL_SIGN ||
                type == Material.CRIMSON_SIGN || type == Material.CRIMSON_WALL_SIGN ||
                type == Material.WARPED_SIGN || type == Material.WARPED_WALL_SIGN ||
                type == Material.MANGROVE_SIGN || type == Material.MANGROVE_WALL_SIGN ||
                type == Material.BAMBOO_SIGN || type == Material.BAMBOO_WALL_SIGN ||
                type == Material.CHERRY_SIGN || type == Material.CHERRY_WALL_SIGN;
    }
}
