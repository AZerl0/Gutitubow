package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.data.damage.DamageSource;
import net.kyori.adventure.text.Component;
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
    private Component deathMessage;

    public GamePlayerDeathEvent(Player player, DamageSource damageSource, Component deathMessage)  {
        this.player = player;
        this.damageSource = damageSource;
        this.deathMessage = deathMessage;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull DamageSource getDamageSource() {
        return damageSource;
    }

    public @NotNull Component getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(@NotNull Component deathMessage) {
        this.deathMessage = deathMessage;
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
