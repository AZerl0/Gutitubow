package com.azerl0.gutitubow.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class GutitubowEntityListener implements Listener {
    private final GutitubowEntityManager gutitubowEntityManager;

    public GutitubowEntityListener(GutitubowEntityManager gutitubowEntityManager) {
        this.gutitubowEntityManager = gutitubowEntityManager;
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveEvent event) {
        gutitubowEntityManager.onEntityRemove(event.getEntity());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        for (GutitubowEntity gutitubowEntity : gutitubowEntityManager.getGutitubowEntities()) {
            gutitubowEntity.onAnyProjectileHit(event);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        for (GutitubowEntity gutitubowEntity : gutitubowEntityManager.getGutitubowEntities()) {
            gutitubowEntity.onInteractEntity(event);
        }
    }
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        for (GutitubowEntity gutitubowEntity : gutitubowEntityManager.getGutitubowEntities()) {
            gutitubowEntity.onHangingBreak(event);
        }
    }
    @EventHandler
    public void onUnleash(EntityUnleashEvent event) {
        if (event.getEntity().getScoreboardTags().contains("gutitubow.cancelUnleash")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        for (GutitubowEntity gutitubowEntity : gutitubowEntityManager.getGutitubowEntities()) {
            gutitubowEntity.onDamage(event);
        }
    }
}
