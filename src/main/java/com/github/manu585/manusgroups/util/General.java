package com.github.manu585.manusgroups.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
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
     */
    public static CompletableFuture<Void> allDone(final Collection<? extends CompletableFuture<?>> futures) {
        if (futures == null || futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public static boolean isSign(Block block) {
        if (block == null) return false;
        if (block.getState() instanceof Sign) return true;

        final Material type = block.getType();
        return Tag.SIGNS.isTagged(type) || Tag.WALL_SIGNS.isTagged(type) || Tag.WALL_HANGING_SIGNS.isTagged(type);
    }
}
