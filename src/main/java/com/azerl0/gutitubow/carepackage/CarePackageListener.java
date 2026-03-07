package com.azerl0.gutitubow.carepackage;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class CarePackageListener implements Listener {

    private final CarePackageManager carePackageManager;

    public CarePackageListener(CarePackageManager cpm) {
        this.carePackageManager = cpm;
    }

    @EventHandler
    public void onMove(EntityMoveEvent event) {
        Entity entity = event.getEntity();

        if (CarePackageManager.isCarePackageCoreEntity(entity)) {
            CarePackage carePackage = carePackageManager.getCarePackage(entity.getUniqueId());
            if (carePackage != null) carePackage.onMove();
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity instanceof Interaction) {

            if (CarePackageManager.isCarePackageEntity(entity)) {
                Entity vehicle = entity.getVehicle();
                if (vehicle == null) return;
                CarePackage carePackage = carePackageManager.getCarePackage(vehicle.getUniqueId());
                if (carePackage != null) carePackage.onInteract(event.getPlayer());
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
