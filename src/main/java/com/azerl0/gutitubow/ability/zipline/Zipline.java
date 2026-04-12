package com.azerl0.gutitubow.ability.zipline;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Zipline {
    private final JavaPlugin plugin;
    private final ZiplineManager ziplineManager;
    private final double ZIPLINE_SPEED = 1.0;
    private int life = 120 * 20;
    private final LeashHitch point1entity;
    private final LeashHitch point2entity;
    private Location failedHitLocation;
    private final BukkitTask updateTask;
    private final Map<Entity, ZiplineVehicle> vehicles = new HashMap<>();

    protected Zipline(JavaPlugin plugin, ZiplineManager ziplineManager, Location point1, Location point2) {
        this.plugin = plugin;
        this.ziplineManager = ziplineManager;
        World world = point1.getWorld();
        point1entity = world.spawn(point1, LeashHitch.class);
        point2entity = world.spawn(point2, LeashHitch.class);
        setPointEntityDefaultMetadata(point1entity);
        setPointEntityDefaultMetadata(point2entity);

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean isThisZiplineEntity(@NotNull Entity entity) {
        return entity == point1entity || entity == point2entity;
    }

    private void setPointEntityDefaultMetadata(LeashHitch entity) {
        entity.setGlowing(true);
        entity.setInvulnerable(true);
        entity.addScoreboardTag("gutitubow.ziplineEntity");
    }

    protected void remove() {
        updateTask.cancel();
        point1entity.remove();
        point2entity.remove();
        for (ZiplineVehicle vehicle : new HashSet<>(vehicles.values())) {
            vehicle.remove();
            vehicles.clear();
        }
    }

    protected void update() {
        life--;
        if (life <= 0) {
            ziplineManager.removeZipline(this);
        } else {
            if (life % 15 == 0) {
                spawnParticleLine(point1entity.getLocation(), point2entity.getLocation(), 0.15);
            }
        }
    }

    public void addPassenger(@NotNull LivingEntity livingEntity, Entity interactedZiplineEntity) {
        Location start;
        Location end;
        if (interactedZiplineEntity == point1entity) {
            start = point1entity.getLocation();
            end = point2entity.getLocation();
        } else {
            start = point2entity.getLocation();
            end = point1entity.getLocation();
        }
        ZiplineVehicle vehicle = new ZiplineVehicle(start, end, livingEntity);
        vehicles.put(vehicle.getVehicleEntity(), vehicle);

    }

    private void spawnParticleLine(
            Location start,
            Location end,
            double spacing
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

    private class ZiplineVehicle {
        private final ArmorStand vehicle;
        private int progress = 0;
        private int MAX_PROGRESS;
        private final BukkitTask updateTask;

        private ZiplineVehicle(Location start, Location end, LivingEntity passenger) {
            vehicle = start.getWorld().spawn(start, ArmorStand.class);
            vehicle.setMarker(true);
            vehicle.setInvisible(true);
            Objects.requireNonNull(vehicle.getAttribute(Attribute.SCALE)).setBaseValue(0.0);
            vehicle.addScoreboardTag("gutitubow.ziplineEntity");
            vehicle.addPassenger(passenger);
            Vector dir = end.clone().subtract(start.clone()).toVector();
            Location location = vehicle.getLocation().setDirection(dir);
            vehicle.teleport(location);
            start.getWorld().playSound(start, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1.0f, 1.0f);
            double distance = start.clone().distance(end.clone());
            MAX_PROGRESS = (int) ((distance / ZIPLINE_SPEED) + 1);

            Location rayTraceStartLocation = location.clone().add(dir.multiply(0.3)).add(0.0, 0.2, 0.0);
            if (dir.length() > 0) {
                RayTraceResult blockHit = start.getWorld().rayTraceBlocks(
                        rayTraceStartLocation,
                        dir,
                        rayTraceStartLocation.distance(end.clone()),
                        FluidCollisionMode.NEVER,
                        true
                );

                if (blockHit != null) {
                    Location hitLocation = blockHit.getHitPosition().toLocation(start.getWorld());
                    double hitDistance = rayTraceStartLocation.clone().distance(hitLocation);
                    MAX_PROGRESS = (int) (hitDistance / ZIPLINE_SPEED);
                    failedHitLocation = hitLocation;
                }

            }


            updateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    update();
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        private Entity getVehicleEntity() {
            return vehicle;
        }

        private void update() {
            progress++;
            if (vehicle.getPassengers().isEmpty()) {
                remove();
            } else {
                if (progress >= MAX_PROGRESS) {
                    for (Entity passenger : vehicle.getPassengers()) {
                        passenger.leaveVehicle();

                        if (failedHitLocation != null) {
                            failedHitLocation.setDirection(passenger.getLocation().getDirection());
                            passenger.teleport(failedHitLocation);
                        }
                        passenger.setVelocity(new Vector(0.0, 1.0, 0.0));
                        vehicle.getWorld().playSound(vehicle.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.0f, 1.0f);
                    }
                    remove();
                } else {
                    Location location = vehicle.getLocation().add(vehicle.getLocation().getDirection().multiply(ZIPLINE_SPEED));
                    vehicle.teleport(location);

                }
            }
        }

        private void remove() {
            updateTask.cancel();
            vehicles.remove(getVehicleEntity());
            vehicle.remove();
        }

    }
}
