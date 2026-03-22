package com.azerl0.gutitubow.packet;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class PacketListener implements Listener, com.github.retrooper.packetevents.event.PacketListener {
    private final EntityFlagManager flagManager;

    public PacketListener(EntityFlagManager flagManager) {
        this.flagManager = flagManager;
    }

    @EventHandler
    public void onEntityRemove(EntityDeathEvent event) {
        flagManager.removeEntity(event.getEntity());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            flagManager.removeEntity(entity);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        flagManager.resendFlags(player);
    }
}
