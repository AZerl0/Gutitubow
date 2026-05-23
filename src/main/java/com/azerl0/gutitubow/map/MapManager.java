package com.azerl0.gutitubow.map;

import org.bukkit.Bukkit;

public class MapManager {

    public Map getTestMap() {
        return new Map(Bukkit.getWorld("world").getBlockAt(3599, 0, -11546).getLocation(), 300);
    }


}
