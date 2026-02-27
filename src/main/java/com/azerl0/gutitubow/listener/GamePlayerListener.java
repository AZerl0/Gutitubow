package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.manager.GamePlayerManager;
import com.azerl0.gutitubow.util.EntityUtil;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class GamePlayerListener implements Listener {
    private final GamePlayerManager gamePlayerManager;

    public GamePlayerListener(GamePlayerManager gamePlayerManager) {
        this.gamePlayerManager = gamePlayerManager;
    }


    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;

        Entity shooter = (Entity) event.getEntity().getShooter();
        Entity tar = event.getHitEntity();

        if (!(event.getEntity() instanceof AbstractArrow)) return;
        if (shooter == null || tar == null) return;

        if (shooter.getUniqueId().equals(tar.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (EntityUtil.isInvalidHitEntity(tar)) return;
        double distance = shooter.getLocation().distance(tar.getLocation());

        GamePlayerManager.GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(tar);
        gamePlayer.damage(1, shooter, distance);
        event.setCancelled(true);
        event.getEntity().remove();

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Ability ability = AbilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onMove(event);
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        Ability ability = AbilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onToggleSprint(event);
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Ability ability = AbilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onJump(event);
    }

    @EventHandler
    public void onPressJumpKey(PlayerInputEvent event) {
        if (!event.getInput().isJump()) return;
        Ability ability = AbilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onPressJumpKey(event);

    }
}
