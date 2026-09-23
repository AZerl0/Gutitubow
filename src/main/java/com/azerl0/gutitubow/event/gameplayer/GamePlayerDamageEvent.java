package com.azerl0.gutitubow.event.gameplayer;

import com.azerl0.gutitubow.damage.DamageSource;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GamePlayerDamageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private double amount;
    private final DamageSource damageSource;
    private @Nullable Component hitMessage;

    public GamePlayerDamageEvent(
            final @NotNull Player player,
            final double amount,
            final @NotNull DamageSource damageSource,
            final @Nullable Component hitMessage
    ) {
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


    public DamageSource getDamageSource() {
        return damageSource;
    }

    public @Nullable Component getHitMessage() {
        return hitMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
