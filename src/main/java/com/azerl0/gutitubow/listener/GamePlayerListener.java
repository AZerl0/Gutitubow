package com.azerl0.gutitubow.listener;

import com.azerl0.gutitubow.manager.GamePlayerManager;
import com.azerl0.gutitubow.util.EntityUtil;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class GamePlayerListener implements Listener {
    private final GamePlayerManager gamePlayerManager;

    public GamePlayerListener(GamePlayerManager gamePlayerManager) {
        this.gamePlayerManager = gamePlayerManager;
    }


    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        Entity shooter = (Entity) event.getEntity().getShooter();
        Entity tar = event.getHitEntity();

        if (!(event.getEntity() instanceof AbstractArrow)) return;
        if (shooter == null || tar == null) return;

        if (shooter == tar) return;
        if (EntityUtil.isInvaildHitEntity(tar)) return;
        double distance = shooter.getLocation().distance(tar.getLocation());

        GamePlayerManager.GamePlayer gamePlayer = gamePlayerManager.getGamePlayer(tar);
        gamePlayer.damage(1, shooter, distance);

    }
}
