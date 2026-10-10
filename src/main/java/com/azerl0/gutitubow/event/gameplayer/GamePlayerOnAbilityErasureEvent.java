package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.gameplayer.GamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamePlayerOnAbilityErasureEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final GamePlayer gamePlayer;

    public GamePlayerOnAbilityErasureEvent(@NotNull GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public @NotNull GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
