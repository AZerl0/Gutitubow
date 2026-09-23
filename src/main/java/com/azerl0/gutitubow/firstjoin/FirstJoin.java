package com.azerl0.gutitubow.firstjoin;

import com.azerl0.gutitubow.event.player.PlayerFirstJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FirstJoin implements Listener {
    private final Set<UUID> joinedPlayers = new HashSet<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();;
        if (joinedPlayers.contains(player.getUniqueId())) return;
        joinedPlayers.add(player.getUniqueId());
        PlayerFirstJoinEvent joinEvent = new PlayerFirstJoinEvent(player);
        Bukkit.getPluginManager().callEvent(joinEvent);
    }
}
