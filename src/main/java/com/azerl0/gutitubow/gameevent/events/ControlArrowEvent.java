package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.controlarrow.ControlArrow;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameevent.GameEventType;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.NotNull;

public class ControlArrowEvent extends GameEvent {
    public ControlArrowEvent(GameEventContext context) {
        super(context);
    }

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.CONTROL;
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivated() {

    }

    @Override
    protected void onShoot(EntityShootBowEvent event) {
        if (event.isCancelled()) return;

        Ability ability = context.gutitubow().getGutitubowManagers().getAbilityManager().getAbility(event.getEntity().getUniqueId());
        if (ability != null && ability.getType() == AbilityType.CONTROL) return;
        ControlArrow.spawnFromShootEvent(event, context.gutitubow().getGutitubowEntityManager());
        event.getProjectile().remove();
    }
}
