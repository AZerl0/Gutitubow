package com.azerl0.gutitubow.packet;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class PacketListener implements Listener {
    private final EntityFlagManager flagManager;

    public PacketListener(EntityFlagManager flagManager) {
        this.flagManager = flagManager;
    }

    @EventHandler
    public void onEntityRemove(EntityDeathEvent event) {
        flagManager.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            flagManager.remove(entity.getUniqueId());
        }
    }
}
