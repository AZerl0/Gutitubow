package com.azerl0.gutitubow.pin;

import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PinListener implements Listener {
    private final PinManager pinManager;

    public PinListener(PinManager pinManager) {
        this.pinManager = pinManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Material mainHandItem = event.getMaterial();
        if (mainHandItem == Material.SPYGLASS) {
            Action action = event.getAction();
            if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
                pinManager.tryCreatePin(event.getPlayer());
            }
        }
    }
}
