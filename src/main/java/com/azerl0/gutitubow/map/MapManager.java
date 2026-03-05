package com.azerl0.gutitubow.map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MapManager {
    private final JavaPlugin plugin;

    public MapManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Map getTestMap() {
        return new Map(Bukkit.getWorld("world").getBlockAt(0, 0, 0).getLocation(), 300);
    }


}
