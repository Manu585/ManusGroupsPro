package com.github.manu585.manusgroups.events;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.util.DefaultGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GroupChangeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID uuid;
    private final Group newGroup;

    public GroupChangeEvent(UUID uuid, @Nullable Group newGroup) {
        this.uuid = uuid;
        this.newGroup = newGroup;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public UUID getUuid() {
        return uuid;
    }

    public @Nullable Group getNewGroup() {
        return (newGroup == null) ? DefaultGroup.group() : newGroup;
    }
}
