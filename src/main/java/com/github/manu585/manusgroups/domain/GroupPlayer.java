package com.github.manu585.manusgroups.domain;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public final class GroupPlayer {
    private final UUID uuid;
    private final NavigableSet<Group> activeGroups;

    private Group primaryGroup;

    public GroupPlayer(final UUID uuid) {
        this.uuid = uuid;
        this.activeGroups = new ConcurrentSkipListSet<>(Group.ORDER);
    }


}
