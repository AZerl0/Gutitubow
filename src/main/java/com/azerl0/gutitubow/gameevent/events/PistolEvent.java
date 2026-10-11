package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameevent.GameEventType;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.NotNull;

public class PistolEvent extends GameEvent {
    public PistolEvent(GameEventContext context) {
        super(context);
    }

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.PISTOL;
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivated() {

    }

    @Override
    protected void onShoot(EntityShootBowEvent event) {
        event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(10));
    }
}
