package com.azerl0.gutitubow.entity.teleportable;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Teleportable {

    @NotNull Location getLocation();

    void teleport(Location location);
}
