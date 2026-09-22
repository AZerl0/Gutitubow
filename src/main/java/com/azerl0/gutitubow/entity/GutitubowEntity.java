package com.azerl0.gutitubow.entity;

import com.azerl0.gutitubow.entity.teleportable.Teleportable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public abstract class GutitubowEntity implements Teleportable {

    private final GutitubowEntityManager entityManager;
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
        entityManager.onGutitubowEntityRemove(uuid);
        isRemoved = true;
        onRemove();
    }

    protected void onRemove() {}

    protected abstract void summon(Location location);

    public final boolean isRemoved() {
        return isRemoved;
    }

    protected void onTick() {}
}
