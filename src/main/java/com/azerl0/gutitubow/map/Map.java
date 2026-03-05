package com.azerl0.gutitubow.map;

import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.util.Vector;

import java.util.Random;

public class Map {
    private Location center;
    private double size;

    public Map(Location center, double size) {
        this.center = center;
        this.size = size;
    }

    public Location getCenter() {
        return this.center;
    }

    public WorldBorder getBorder() {
        return this.center.getWorld().getWorldBorder();
    }

    public double getSize() {
        return this.size;
    }

    public void setCenter(Location location) {
        this.center = location;
    }

    public Location getRandomXZPosition(double borderOffset) {
        Random random = new Random();

        double angle = random.nextDouble() * 2 * Math.PI;
        double maxRadius = this.size / 2 - borderOffset;

        double radius = Math.sqrt(random.nextDouble()) * maxRadius;

        Vector vector = new Vector(
                Math.cos(angle) * radius,
                0.0,
                Math.sin(angle) * radius
        );
        return this.center.clone().add(vector);
    }


}
