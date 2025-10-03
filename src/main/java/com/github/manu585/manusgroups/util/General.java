package com.github.manu585.manusgroups.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class General {
    private General() {}

    public static void runSync(final JavaPlugin plugin, final Runnable r) {
        plugin.getServer().getScheduler().runTask(plugin, r);
    }

    public static byte[] uuidToBytes(final UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID bytesToUuid(final byte[] bytes) {
        if (bytes == null || bytes.length != 16) throw new IllegalStateException("UUID byte[] must be length 16!");
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long msb = bb.getLong();
        long lsb = bb.getLong();
        return new UUID(msb, lsb);
    }
}
