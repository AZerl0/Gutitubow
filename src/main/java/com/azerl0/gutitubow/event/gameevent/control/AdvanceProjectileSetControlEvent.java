package com.azerl0.gutitubow.event.gameevent.control;

import com.azerl0.gutitubow.entity.advancearrow.AdvanceProjectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AdvanceProjectileSetControlEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final AdvanceProjectile advanceProjectile;
    private boolean overrideGlowColor;

    public AdvanceProjectileSetControlEvent(AdvanceProjectile advanceProjectile, boolean overrideGlowColor) {
        this.advanceProjectile = advanceProjectile;
        this.overrideGlowColor = overrideGlowColor;
    }

    public AdvanceProjectile getAdvanceProjectile() {
        return advanceProjectile;
    }

    public boolean isOverrideGlowColor() {
        return overrideGlowColor;
    }

    public void setOverrideGlowColor(boolean overrideGlowColor) {
        this.overrideGlowColor = overrideGlowColor;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
