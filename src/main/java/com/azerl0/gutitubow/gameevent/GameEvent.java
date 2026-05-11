package com.azerl0.gutitubow.gameevent;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

public abstract class GameEvent {
    private boolean activated = false;

    public @NotNull
    abstract GameEventType getEventType();

    public abstract boolean canActivate();

    public final void activate() {
        activated = true;
        onActivate();
    }

    protected abstract void onActivate();

    public final void deactivate() {
        activated = false;
        onDeactivated();
    }

    protected abstract void onDeactivated();

    protected void onArrowHit(ProjectileHitEvent event) {}

    protected void onShoot(EntityShootBowEvent event) {}
}
