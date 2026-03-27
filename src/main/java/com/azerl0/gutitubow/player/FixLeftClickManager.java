package com.azerl0.gutitubow.player;

import com.azerl0.gutitubow.event.gameplayer.PlayerFixLeftClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FixLeftClickManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> dropFlag = new HashMap<>();
    private final Map<UUID, Boolean> rideFlag = new HashMap<>();

    public FixLeftClickManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addDropFlag(Player player) {
        dropFlag.put(player.getUniqueId(), true);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                dropFlag.remove(player.getUniqueId()), 1L);
    }

    public void addRideFlag(Player player) {
        rideFlag.put(player.getUniqueId(), true);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                rideFlag.remove(player.getUniqueId()), 1L);
    }

    private boolean isValidLeftClick(Player player) {
        UUID uuid = player.getUniqueId();
        return !dropFlag.getOrDefault(uuid, false) && !rideFlag.getOrDefault(uuid, false);
    }

    public void onSuspiciousLeftClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;
        if (isValidLeftClick(event.getPlayer())) {
            PlayerFixLeftClickEvent clickEvent = new PlayerFixLeftClickEvent(event);
            Bukkit.getPluginManager().callEvent(clickEvent);
        }
    }
}
