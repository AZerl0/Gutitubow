package com.azerl0.gutitubow.event.gutitubowentity.shield;

import com.azerl0.gutitubow.entity.shield.Shield;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbstractShieldEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    protected Shield shield;

    public @NotNull Shield getShield() {
        return shield;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
