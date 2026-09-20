package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.data.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class GamePlayerDeathEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private final DamageSource damageSource;

    public GamePlayerDeathEvent(Player player, DamageSource damageSource)  {
        this.player = player;
        this.damageSource = damageSource;
    }

    public @NotNull Player getPlayer() {
        return player;
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
