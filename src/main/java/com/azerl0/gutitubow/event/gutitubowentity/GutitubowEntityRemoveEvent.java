package com.azerl0.gutitubow.event.gutitubowentity;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GutitubowEntityRemoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GutitubowEntity gutitubowEntity;

    public GutitubowEntityRemoveEvent(@NotNull GutitubowEntity gutitubowEntity) {
        this.gutitubowEntity = gutitubowEntity;
    }

    public GutitubowEntity getGutitubowEntity() {
        return gutitubowEntity;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
