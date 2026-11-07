package com.azerl0.gutitubow.event.entity.mannequin;

import com.azerl0.gutitubow.damage.DamageSource;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MannequinGameDamageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Mannequin mannequin;
    private double amount;
    private final DamageSource damageSource;

    public MannequinGameDamageEvent(
            final @NotNull Mannequin mannequin,
            final double amount,
            final @NotNull DamageSource damageSource
    ) {
        this.mannequin = mannequin;
        this.amount = amount;
        this.damageSource = damageSource;
    }

    @NotNull
    public Mannequin getMannequin() {
        return mannequin;
    }

    public double getAmount() {
        return amount;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
