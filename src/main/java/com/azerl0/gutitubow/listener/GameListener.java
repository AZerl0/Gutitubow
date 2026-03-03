package com.azerl0.gutitubow.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

public class GameListener implements Listener {
    @EventHandler
    public void onLootDrop(BlockDropItemEvent event) {
        event.setCancelled(true);
    }
}
