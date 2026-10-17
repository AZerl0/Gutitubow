package com.azerl0.gutitubow.entity.zipline;

import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ZiplineVehicle extends GutitubowEntity {
    private Snowball snowball;
    private int life;
    private ZiplineNode nextZiplineNode;

    public ZiplineVehicle(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.ZIPLINE_VEHICLE;
    }

    @Override
    public @NotNull Location getLocation() {
        return snowball.getLocation();
    }

    @Override
    public void teleport(Location location) {
        snowball.teleport(location);
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        return Set.of(snowball.getUniqueId());
    }

    @Override
    protected void onRemove() {
        snowball.remove();
    }

    @Override
    protected void summon(Location location) {
        snowball = location.getWorld().spawn(location, Snowball.class);
        snowball.setInvisible(true);
        snowball.setGravity(false);
        snowball.setItem(ItemStack.of(Material.AIR));

    }

    @Override
    protected void onTick() {
        if (!snowball.isValid()) {
            remove();
            return;
        }

        if (snowball.getPassengers().isEmpty()) {
            remove();
            return;
        }

        if (nextZiplineNode != null && !nextZiplineNode.isRemoved()) {
            life = 20;

            double nextNodeDistance = nextZiplineNode.getLocation().clone().distance(getLocation());
            if (nextNodeDistance < 0.5) {
                for (Entity passenger : snowball.getPassengers()) {
                    passenger.leaveVehicle();
                    passenger.teleport(nextZiplineNode.getLocation().setDirection(passenger.getLocation().getDirection()));
                    passenger.setVelocity(new Vector(0.0, 1.0, 0.0));
                }
                snowball.getWorld().playSound(getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.0f, 1.0f);
                remove();
                return;
            }

            Vector velocity = nextZiplineNode.getLocation().toVector().subtract(getLocation().toVector());
            double length = velocity.length();
            double MAX_LENGTH = 1.1;
            if (length > MAX_LENGTH) length = MAX_LENGTH;
            snowball.setVelocity(velocity.normalize().multiply(length));



        } else {
            life--;
            if (life <= 0) remove();
        }
    }

    @Override
    protected void onAnyProjectileHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;

        if (event.getEntity().getUniqueId().equals(snowball.getUniqueId())) {
            for (Entity passenger : snowball.getPassengers()) {
                passenger.leaveVehicle();
                passenger.teleport(event.getEntity().getLocation().setDirection(passenger.getLocation().getDirection()));
                passenger.setVelocity(new Vector(0.0, 1.0, 0.0));
            }
            snowball.getWorld().playSound(getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 1.0f, 1.0f);
            remove();
        }
    }

    public void setNextZiplineNode(ZiplineNode nextZiplineNode) {
        this.nextZiplineNode = nextZiplineNode;
    }

    public void addPassenger(@NotNull Entity entity) {
        snowball.addPassenger(entity);
    }
}
