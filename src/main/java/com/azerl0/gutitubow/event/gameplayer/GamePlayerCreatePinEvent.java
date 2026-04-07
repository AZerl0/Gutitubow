package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.pin.Pin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamePlayerCreatePinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Pin pin;

    public GamePlayerCreatePinEvent(@NotNull Player player, Pin pin) {
        this.player = player;
        this.pin = pin;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Pin getPin() {
        return pin;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
