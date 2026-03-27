package com.azerl0.gutitubow.event.gameplayer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerFixLeftClickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final PlayerInteractEvent event;

    public PlayerFixLeftClickEvent(PlayerInteractEvent event) {
        this.event = event;
    }

    @NotNull
    public PlayerInteractEvent getEvent() {
        return event;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
