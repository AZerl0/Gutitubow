package com.azerl0.gutitubow.entity.carepackage;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Optional;

public class CarePackageListener implements Listener {

    private final GutitubowEntityManager entityManager;

    public CarePackageListener(GutitubowEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventHandler
    public void onMove(EntityMoveEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getScoreboardTags().contains("gutitubow.carePackageCoreEntity")) return;

        Optional<GutitubowEntity> gutitubowEntity = entityManager.getGutitubowEntity(entity);
        if (gutitubowEntity.isPresent() && gutitubowEntity.get() instanceof CarePackage carePackage) {
            carePackage.onMove();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity instanceof Interaction) {

            Optional<GutitubowEntity> gutitubowEntity = entityManager.getGutitubowEntity(entity);

            if (gutitubowEntity.isPresent() && gutitubowEntity.get() instanceof CarePackage carePackage) {
                carePackage.onInteract(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onDrop(EntityDropItemEvent event) {
        if (event.getEntity() instanceof Chicken &&
                event.getItemDrop().getItemStack().getType() == Material.EGG) {
            event.setCancelled(true);
        }
    }
}
