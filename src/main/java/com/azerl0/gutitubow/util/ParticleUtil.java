package com.azerl0.gutitubow.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class ParticleUtil {
    public static void spawnParticleLine(
            Location start,
            Location end,
            double spacing,
            Consumer<Location> particle
    ) {
        World world = start.getWorld();
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();

        direction.normalize();

        int particles = (int) (distance / spacing);

        double particleOffset = 0.05;
        for (int i = 0; i <= particles; i++) {
            Vector offset = direction.clone().multiply(spacing * i);
            Location point = start.clone().add(offset);

            particle.accept(point);

            world.spawnParticle(
                    Particle.DUST,
                    point,
                    1,
                    particleOffset, particleOffset, particleOffset,
                    0,
                    new Particle.DustOptions(
                            Color.fromRGB(214, 139, 4),
                            1.0f
                    ),
                    true
            );
        }
    }
}
