package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class GamePlayerDeathEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final GamePlayer gamePlayer;
    private final DamageSource damageSource;

    public GamePlayerDeathEvent(GamePlayer gamePlayer, DamageSource damageSource)  {
        this.gamePlayer = gamePlayer;
        this.damageSource = damageSource;
    }

    public @NotNull GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public @NotNull DamageSource getDamageSource() {
        return damageSource;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
