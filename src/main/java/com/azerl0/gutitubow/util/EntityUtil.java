package com.azerl0.gutitubow.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityUtil {
    public static boolean isInvalidHitEntity(Entity entity) {
        if (entity instanceof Enderman) return true;
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.CREATIVE) return true;
        return false;
    }
}
