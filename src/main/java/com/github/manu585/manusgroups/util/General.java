package com.github.manu585.manusgroups.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class General {
    private General() {}

    public static void runSync(final JavaPlugin plugin, final Runnable r) {
        plugin.getServer().getScheduler().runTask(plugin, r);
    }

    public static CompletableFuture<Void> runMain(final JavaPlugin plugin, final Runnable r) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        runSync(plugin, () -> {
            try {
                r.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Wait for all futures to complete without result needed.
     * Includes fail safes (CompleteExceptionally)
     */
    public static CompletableFuture<Void> allDone(final Collection<? extends CompletableFuture<?>> futures) {
        if (futures == null || futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * allOf wrapper with empty safe behaviour
     */
    public static CompletableFuture<Void> all(List<CompletableFuture<?>>tasks) {
        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
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
