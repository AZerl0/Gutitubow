package com.azerl0.gutitubow.entity.teleportable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class BukkitTeleportableEntity implements Teleportable{
    private final Entity entity;

    public BukkitTeleportableEntity(Entity entity) {
        this.entity = entity;
    }
    @Override
    public @NotNull Location getLocation() {
        return entity.getLocation();
    }

    @Override
    public void teleport(Location location) {
        entity.teleport(location);
    }
}
