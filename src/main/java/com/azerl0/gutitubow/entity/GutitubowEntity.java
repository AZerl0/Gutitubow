package com.azerl0.gutitubow.entity;

import com.azerl0.gutitubow.entity.teleportable.Teleportable;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public abstract class GutitubowEntity implements Teleportable {

    protected final GutitubowEntityManager entityManager;
    protected final UUID uuid;
    protected Location location;
    protected boolean isRemoved = false;

    public GutitubowEntity(GutitubowEntityManager gutitubowEntityManager, Location location) {
        entityManager = gutitubowEntityManager;
        this.location = location;
        uuid = UUID.randomUUID();
    }

    public final @NotNull UUID getUniqueId() {
        return uuid;
    }

    public abstract @NotNull GutitubowEntityType getType();

    public abstract @NotNull Location getLocation();

    public abstract void teleport(Location location);

    public abstract Set<UUID> getPartEntitiesUUID();

    public final void remove() {
        onRemove();
        entityManager.onGutitubowEntityRemove(uuid);
        isRemoved = true;
    }

    protected abstract void onRemove();

    protected abstract void summon(Location location);

    public final boolean isRemoved() {
        return isRemoved;
    }

    protected void onTick() {}

    protected void onAnyProjectileHit(ProjectileHitEvent event) {}

    protected void onInteractEntity(PlayerInteractEntityEvent event) {}

    protected void onHangingBreak(HangingBreakEvent event) {}

    protected void onDamage(EntityDamageEvent event) {}
}
