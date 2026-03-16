package com.azerl0.gutitubow.packet;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityGlowManager {
    private final EntityFlagManager flagManager;

    public EntityGlowManager(EntityFlagManager flagManager) {
        this.flagManager = flagManager;
    }

    public void addGlow(Player viewer, Entity target) {
        flagManager.addFlag(viewer, target, (byte) 0x40);
    }
    public void removeGlow(Player viewer, Entity target) {
        flagManager.removeFlag(viewer, target, (byte) 0x40);
    }
}
