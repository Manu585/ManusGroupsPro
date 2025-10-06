package com.github.manu585.manusgroups.domain;

import com.github.manu585.manusgroups.util.DefaultGroup;

import java.util.UUID;
import java.util.function.Function;

public record GroupPlayer(UUID uuid, Group primaryGroup) {
    // GroupPlayer Factory
    public static GroupPlayer from(UUID uuid, String groupName, Function<String, Group> resolver) {
        Group resolved = (groupName == null) ? DefaultGroup.group() : resolver.apply(groupName);
        return new GroupPlayer(uuid, resolved);
    }

    public boolean hasGroup(String groupName) {
        return primaryGroup != null && primaryGroup.name().equalsIgnoreCase(groupName);
    }
}
