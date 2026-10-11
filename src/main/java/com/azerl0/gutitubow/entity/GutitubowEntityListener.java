package com.azerl0.gutitubow.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

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
}
