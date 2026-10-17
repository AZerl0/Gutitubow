package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.ability.abilities.ControlAbility;
import com.azerl0.gutitubow.entity.advanceprojectile.AdvanceProjectile;
import com.azerl0.gutitubow.event.gameevent.control.AdvanceProjectileSetControlEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameevent.GameEventType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
    protected void onGamePlayerShoot(GamePlayerShootEvent event) {
        if (event.isCancelled()) return;
        if (event.getShootReason() == GamePlayerShootEvent.ShootReason.CONTROL_EVENT) return;

        Ability ability = context.gutitubow().getGutitubowManagers().getAbilityManager().getAbility(event.getPlayer());
        if (ability != null && ability.getType() == AbilityType.CONTROL) return;

        if (event.getProjectile() == null) return;
        AdvanceProjectile advanceProjectile = context.gutitubow().getGutitubowEntityManager().getAdvanceProjectile(event.getProjectile());
        AdvanceProjectileSetControlEvent advanceProjectileSetControlEvent = new AdvanceProjectileSetControlEvent(advanceProjectile, true);
        Bukkit.getPluginManager().callEvent(advanceProjectileSetControlEvent);
        if (event.isCancelled()) return;

        Player player = event.getPlayer();


        advanceProjectile.setControlling(true);
        advanceProjectile.setControlSpeed(ControlAbility.getDefaultControlSpeed(event.getForce()));
        advanceProjectile.setControlEffect(advanceProjectileSetControlEvent.isOverrideGlowColor());
        player.playSound(player, Sound.ENTITY_EVOKER_CAST_SPELL, 0.5f, 1.0f);

//        ControlArrow controlArrow = ControlArrow.spawnFromGamePlayerShootEvent(event, context.gutitubow().getGutitubowEntityManager());
//        boolean result = event.getGamePlayer().shoot(GamePlayerShootEvent.ShootReason.CONTROL_EVENT, event.getProjectileLocation(), event.getDirection(), event.getForce(), controlArrow.getArrow());
//        if (!result) {
//            controlArrow.remove();
//        } else {
//            event.setCancelled(true);
//        }
    }
}
