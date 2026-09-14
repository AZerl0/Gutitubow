package com.azerl0.gutitubow.entity.grabityhole;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.util.EntityUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class GravityHole extends GutitubowEntity {
    private BukkitTask updateTask;
    private int life;
    private boolean isSmall;
    private Vector direction;
    private UUID ownerUUID;
    private final Set<GravityEffect> effects = new HashSet<>();


    public GravityHole(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.GRAVITY_HOLE;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public void teleport(Location location) {
        this.location = location;
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of();
    }

    @Override
    public void remove() {
        updateTask.cancel();
        removeGutitubowEntity();
    }

    @Override
    protected void summon(Location location) {
        uuid = UUID.randomUUID();
        life = 3 * 20;
        isSmall = true;
        direction = location.getDirection();
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(Gutitubow.get(), 0L, 1L);
    }

    public void setDirection(@NotNull Vector direction) {
        this.direction = direction;
    }

    public void setOwnerUUID(@NotNull UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public void growUp() {
        if (!isSmall) return;
        isSmall = false;
        onGrowUp();
    }

    private void update() {
        life--;
        if (life <= 0) {
            if (isSmall) {
                growUp();
            } else {
                remove();
            }
        }
        if (isSmall) {
            World world = location.getWorld();

            // Step
            double stepSize = 1;
            RayTraceResult blockHit = world.rayTraceBlocks(
                    location,
                    direction,
                    stepSize,
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit != null) {
                teleport(blockHit.getHitPosition().toLocation(world));
                growUp();
            } else {
                teleport(location.clone().add(direction.normalize().multiply(stepSize)));
            }

            // Particle
            Particle.DUST_COLOR_TRANSITION.builder()
                    .location(location)
                    .offset(0, 0, 0)
                    .count(2)
                    .colorTransition(Color.PURPLE, Color.BLACK)
                    .spawn();

        } else {
            getNearEntity(entity -> {
                if (EntityUtil.isInvalidHitEntity(entity)) return;
                Vector sub = location.clone().subtract(entity.getLocation()).toVector().normalize().multiply(0.25);
                entity.setVelocity(entity.getVelocity().add(sub));
            });
            for (GravityEffect effect : effects) {
                effect.step(3);
                effect.spawnParticle(location, 5);
            }
            for (int i = 0; i < 10; i++) {
                Vector offsetV = Vector.getRandom().normalize().multiply(new Random().nextDouble(0, 2.5));
                Particle.DUST_COLOR_TRANSITION.builder()
                        .location(location.clone().add(offsetV))
                        .offset(0, 0, 0)
                        .count(2)
                        .colorTransition(Color.PURPLE, Color.BLACK)
                        .spawn();
            }
            if (life % 3 == 0) effects.add(new GravityEffect());
        }

    }

    private void onGrowUp() {
        life = 7 * 20;
        location.getWorld().playSound(location, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 1.0f);
        location.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
        getNearEntity(entity -> {
            if (entity.getUniqueId() != ownerUUID) return;
            Vector sub = location.clone().subtract(entity.getLocation()).toVector().normalize().multiply(0.75);
            entity.setVelocity(entity.getVelocity().add(sub));

        });
    }

    public boolean isSmall() {
        return isSmall;
    }

    private void getNearEntity(Consumer<Entity> entity) {
        double r = 10;
        for (Entity nearEntity : location.getNearbyEntities(r, r, r)) {
            EntityType entityType = nearEntity.getType();
            Set<EntityType> entityTypeSet = new HashSet<>(List.of(
                    EntityType.PLAYER,
                    EntityType.ARROW,
                    EntityType.SPECTRAL_ARROW
            ));
            if (!entityTypeSet.contains(entityType)) continue;
            entity.accept(nearEntity);
        }
    }

    private static class GravityEffect {
        Vector vector = Vector.getRandom();
        float random = new Random().nextFloat();

        private void step(double stepSize) {
            vector = offsetDirection(vector, random * 360, stepSize);

        }

        private void spawnParticle(Location location, double r) {
            Particle.DUST_COLOR_TRANSITION.builder()
                    .location(location.clone().add(vector.clone().normalize().multiply(r)))
                    .offset(0, 0, 0)
                    .count(2)
                    .colorTransition(Color.PURPLE, Color.BLACK)
                    .spawn();
        }
    }

    public static Vector offsetDirection(
            Vector direction,
            double screenAngle,
            double offsetAngle
    ) {
        direction = direction.clone().normalize();

        // ワールド上方向
        Vector worldUp = new Vector(0, 1, 0);

        // 視線に対する「画面上の上方向」を作る
        Vector up = worldUp.clone()
                .subtract(direction.clone().multiply(worldUp.dot(direction)))
                .normalize();

        // 画面上の右方向
        Vector right = direction.clone()
                .crossProduct(up)
                .normalize();

        // 画面上の角度
        double phi = Math.toRadians(screenAngle);

        // 画面上の円周方向
        Vector screenDirection = right.clone()
                .multiply(Math.cos(phi))
                .add(up.clone().multiply(Math.sin(phi)));

        // 元の視線から何度離すか
        double theta = Math.toRadians(offsetAngle);

        // 円周方向へ theta 度傾ける
        return direction.clone()
                .multiply(Math.cos(theta))
                .add(screenDirection.multiply(Math.sin(theta)))
                .normalize();
    }
}
