package com.azerl0.gutitubow.map;

import org.bukkit.Bukkit;

public class MapManager {

    public Map getTestMap() {
        return new Map(Bukkit.getWorld("world").getBlockAt(0, 0, 0).getLocation(), 300);
    }


}
