package com.azerl0.gutitubow.entity.zipline;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.util.ParticleUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ZiplineNode extends GutitubowEntity {
    private ArmorStand armorStand;
    private LeashHitch leash;
    private int health;
    private int life;
    private int connectionLostTick;
    private final Set<ZiplineNode> connectionNodes = new HashSet<>();

    public ZiplineNode(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.ZIPLINE_NODE;
    }

    @Override
    public @NotNull Location getLocation() {
        return leash.getLocation();
    }

    @Override
    public void teleport(Location location) {
        leash.teleport(location);
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of(
                leash.getUniqueId(),
                armorStand.getUniqueId()
        );
    }

    @Override
    protected void onRemove() {
        leash.remove();
        armorStand.remove();
    }

    @Override
    protected void summon(Location location) {
        leash = location.getWorld().spawn(location, LeashHitch.class);
        leash.setInvulnerable(true);
        Location armorStandLoc = new Location(leash.getWorld(), leash.getLocation().getBlockX(), leash.getLocation().getBlockY(), leash.getLocation().getBlockZ());
        armorStandLoc.add(0.5, 0.0, 0.5);
        armorStand = location.getWorld().spawn(armorStandLoc.clone().add(0.0, -1.0, 0.0), ArmorStand.class);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        health = 3;
        life = 20;
        connectionLostTick = 2 * 20;
    }

    @Override
    protected void onTick() {
        if (!leash.isValid()) {
            remove();
            return;
        }
        life--;
        if (life <= 0) {
            remove();
            return;
        }
        if (connectionNodes.isEmpty()) {
            connectionLostTick--;
            if (connectionLostTick <= 0) {
                remove();
            }
        } else {
            connectionLostTick = 2 * 20;
            if (leash.getWorld().getGameTime() % 15 == 0) {
                for (ZiplineNode ziplineNode : new HashSet<>(connectionNodes)) {
                    if (ziplineNode.isRemoved()) {
                        connectionNodes.remove(ziplineNode);
                        continue;
                    }
                    if (ziplineNode.getUniqueId().compareTo(getUniqueId()) < 0) {
                        double particleOffset = 0.05;
                        ParticleUtil.spawnParticleLine(ziplineNode.getLocation(), getLocation(), 0.15, point -> point.getWorld().spawnParticle(
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
                        ));
                    }
                }
            }
        }
    }

    @Override
    protected void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        if (connectionNodes.isEmpty()) return;

        Entity clicked = event.getRightClicked();
        if (clicked.getUniqueId().equals(leash.getUniqueId()) || clicked.getUniqueId().equals(armorStand.getUniqueId())) {
            ZiplineVehicle vehicle = entityManager.spawnGutitubowEntity(getLocation(), ZiplineVehicle.class);
            vehicle.addPassenger(event.getPlayer());
            ZiplineNode nextNode = connectionNodes.iterator().next();
            vehicle.setNextZiplineNode(nextNode);
            clicked.getWorld().playSound(clicked.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1.0f, 1.0f);

            Vector offset = nextNode.getLocation().toVector().subtract(getLocation().toVector()).normalize().multiply(0.3);
            Location offsetLoc = getLocation().clone().add(offset);
            vehicle.teleport(offsetLoc);
        }
    }

    @Override
    protected void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity().getUniqueId().equals(leash.getUniqueId())) event.setCancelled(true);
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;

        if (event.getEntity().getUniqueId().equals(armorStand.getUniqueId())) {
            health--;
            Location soundLocation = armorStand.getLocation();
            World world = soundLocation.getWorld();
            if (health <= 0) {
                remove();
                world.playSound(soundLocation, Sound.ENTITY_ARMOR_STAND_BREAK, 1.0f, 1.5f);
                world.playSound(soundLocation, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                world.playSound(soundLocation, Sound.BLOCK_COPPER_GRATE_BREAK, 1.0f, 1.0f);
            } else {
                world.playSound(soundLocation, Sound.ENTITY_ARMOR_STAND_HIT, 1.0f, 1.0f);
                world.playSound(soundLocation, Sound.BLOCK_COPPER_GRATE_HIT, 1.0f, 1.0f);

            }
            event.setDamage(0.0);
        }
    }

    public void setLife(int life) {
        this.life = life;
    }

    public void addConnectionNode(@NotNull ZiplineNode node) {
        connectionNodes.add(node);
    }
}
