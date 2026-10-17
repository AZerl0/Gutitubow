package com.azerl0.gutitubow.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class ParticleUtil {

    public static void spawnParticleLine(
            Location start,
            Location end,
            double spacing,
            Consumer<Location> particle
    ) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();

        direction.normalize();

        int particles = (int) (distance / spacing);

        for (int i = 0; i <= particles; i++) {
            Vector offset = direction.clone().multiply(spacing * i);
            Location point = start.clone().add(offset);

            particle.accept(point);
        }
    }
}
