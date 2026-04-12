package com.azerl0.gutitubow.ability.zipline;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class ZiplineListener implements Listener {
    private final ZiplineManager ziplineManager;

    public ZiplineListener(ZiplineManager ziplineManager) {
        this.ziplineManager = ziplineManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof LeashHitch leashHitch && leashHitch.getScoreboardTags().contains("gutitubow.ziplineEntity")) {
            Zipline zipline = ziplineManager.getZipline(leashHitch);
            if (zipline == null) return;
            zipline.addPassenger(event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler
    public void onDestroy(HangingBreakEvent event) {
        if (event.getEntity().getScoreboardTags().contains("gutitubow.ziplineEntity")) event.setCancelled(true);
    }

    @EventHandler
    public void onUnleash(EntityUnleashEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity().getScoreboardTags().contains("gutitubow.cancelUnleash")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        Entity vehicle = event.getVehicle().getVehicle();
        if (vehicle != null && vehicle.getScoreboardTags().contains("gutitubow.ziplineEntity")) {
            vehicle.remove();
        }
    }
}
