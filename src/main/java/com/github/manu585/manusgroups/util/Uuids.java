package com.github.manu585.manusgroups.util;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class Uuids {
    private Uuids() {}

    public static byte @NotNull [] toBytes(final @NotNull UUID uuid) {
        final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static @NotNull UUID toUuid(final byte @NotNull [] bytes) {
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        final long msb = bb.getLong();
        final long lsb = bb.getLong();
        return new UUID(msb, lsb);
    }
}
