package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.util.ArrowUtil;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

public class RicochetEvent extends GameEvent {


    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.RICOCHET;
    }

    @Override
    public boolean canActivate() {
        return true;
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivated() {

    }

    @Override
    protected void onArrowHit(ProjectileHitEvent event) {
        ArrowUtil.RicochetEvent(Gutitubow.get(), event);
    }
}
