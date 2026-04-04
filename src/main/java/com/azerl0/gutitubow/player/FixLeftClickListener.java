package com.azerl0.gutitubow.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class FixLeftClickListener implements Listener {
    private final FixLeftClickManager fixLeftClickManager;

    public FixLeftClickListener(FixLeftClickManager fixLeftClickManager) {
        this.fixLeftClickManager = fixLeftClickManager;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        fixLeftClickManager.addDropFlag(event.getPlayer());
    }

    @EventHandler
    public void onInventoryDrop(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryAction action = event.getAction();
        if (
                action == InventoryAction.DROP_ALL_CURSOR ||
                action == InventoryAction.DROP_ALL_SLOT ||
                action == InventoryAction.DROP_ONE_CURSOR ||
                action == InventoryAction.DROP_ONE_SLOT
        ) fixLeftClickManager.addDropFlag(player);
    }

    @EventHandler
    public void onRide(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player) {
            fixLeftClickManager.addRideFlag(player);
        }
    }

    @EventHandler
    public void onSuspiciousLeftClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            fixLeftClickManager.onSuspiciousLeftClick(event);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            fixLeftClickManager.addInteractFlag(event.getPlayer());
        }
    }
}
