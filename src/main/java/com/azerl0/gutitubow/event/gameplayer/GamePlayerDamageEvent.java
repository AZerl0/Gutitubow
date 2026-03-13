package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.data.GutitubowDamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GamePlayerDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private Player attacker;
    private final GutitubowDamageType damageType;
    private double amount;
    private boolean cancelled;

    public  GamePlayerDamageEvent(Player player, double amount, GutitubowDamageType damageType) {
        this.player = player;
        this.amount = amount;
        this.damageType = damageType;
    }

    public GamePlayerDamageEvent(Player player, double amount, Player attacker, GutitubowDamageType damageType) {
        this.player = player;
        this.amount = amount;
        this.attacker = attacker;
        this.damageType = damageType;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public GutitubowDamageType getDamageType() {
        return damageType;
    }

    @Nullable
    public Player getAttacker() {
        return attacker;
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
