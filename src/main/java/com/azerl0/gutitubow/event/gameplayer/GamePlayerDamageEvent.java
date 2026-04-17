package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.data.damage.DamageSource;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GamePlayerDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private double amount;
    private final DamageSource damageSource;
    private @Nullable Component hitMessage;
    private boolean cancelled;

    public  GamePlayerDamageEvent(Player player, double amount, DamageSource damageSource) {
        this.player = player;
        this.amount = amount;
        this.damageSource = damageSource;
    }

    public GamePlayerDamageEvent(Player player, double amount, DamageSource damageSource, @Nullable Component hitMessage) {
        this.player = player;
        this.amount = amount;
        this.damageSource = damageSource;
        this.hitMessage = hitMessage;
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

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public @Nullable Component getHitMessage() {
        return hitMessage;
    }

    public void setHitMessage(@Nullable Component hitMessage) {
        this.hitMessage = hitMessage;
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
