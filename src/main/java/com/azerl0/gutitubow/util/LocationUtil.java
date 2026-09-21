package com.azerl0.gutitubow.util;

import org.bukkit.Location;

public class LocationUtil {

    public static boolean isIn3x3x3(Location center, Location target) {
        if (!center.getWorld().equals(target.getWorld())) {
            return false;
        }

        int dx = Math.abs(center.getBlockX() - target.getBlockX());
        int dy = Math.abs(center.getBlockY() - target.getBlockY());
        int dz = Math.abs(center.getBlockZ() - target.getBlockZ());

        return dx <= 1 && dy <= 1 && dz <= 1;
    }
}
