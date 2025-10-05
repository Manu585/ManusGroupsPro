package com.github.manu585.manusgroups.util;

import com.github.manu585.manusgroups.domain.Group;

public class DefaultGroup {
    private static volatile Group group;

    private DefaultGroup() {}

    public static void initialize(Group g) {
        if (group != null) return;
        group = g;
    }

    public static Group group() {
        ensureInit();
        return group;
    }

    public static String name() {
        ensureInit();
        return group.name();
    }

    private static void ensureInit() {
        if (group == null) {
            throw new IllegalStateException("DefaultGroup not initialized.");
        }
    }

    public static void set(Group newDefault) {
        group = newDefault;
        ensureInit();
    }
}
