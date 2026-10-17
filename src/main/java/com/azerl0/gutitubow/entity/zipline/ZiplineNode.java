package com.azerl0.gutitubow.entity.zipline;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ZiplineNode extends GutitubowEntity {
    private LeashHitch leash;
    private int life;
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
        return Set.of(leash.getUniqueId());
    }

    @Override
    protected void onRemove() {
        leash.remove();
    }

    @Override
    protected void summon(Location location) {
        leash = location.getWorld().spawn(location, LeashHitch.class);
        leash.setGlowing(true);
        leash.setInvulnerable(true);
        life = 20;
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
        if (leash.getWorld().getGameTime() % 15 == 0) {
            for (ZiplineNode ziplineNode : connectionNodes) {
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

    @Override
    protected void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        if (connectionNodes.isEmpty()) return;

        Entity clicked = event.getRightClicked();
        if (clicked.getUniqueId().equals(leash.getUniqueId())) {
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

    public void setLife(int life) {
        this.life = life;
    }

    public void addConnectionNode(@NotNull ZiplineNode node) {
        connectionNodes.add(node);
    }
}
