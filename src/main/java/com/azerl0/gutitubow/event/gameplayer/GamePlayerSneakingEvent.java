package com.azerl0.gutitubow.event.gameplayer;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamePlayerSneakingEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int sneakingTick;

    public GamePlayerSneakingEvent(Player player, int sneakingTick) {
        this.player = player;
        this.sneakingTick = sneakingTick;
    }

    public GamePlayerSneakingEvent(Player player) {
        this.player = player;
        this.sneakingTick = 1;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public int getSneakingTick() {
        return sneakingTick;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
