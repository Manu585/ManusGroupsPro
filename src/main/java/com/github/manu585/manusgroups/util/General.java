package com.github.manu585.manusgroups.util;

import org.bukkit.plugin.java.JavaPlugin;

public final class General {
    private General() {}

    public static void runSync(final JavaPlugin plugin, final Runnable r) {
        plugin.getServer().getScheduler().runTask(plugin, r);
    }
}
