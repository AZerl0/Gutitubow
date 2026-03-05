package com.azerl0.gutitubow.debug;

import org.bukkit.plugin.java.JavaPlugin;

public class DebugManager {
    private final JavaPlugin plugin;
    private boolean quick_phase;
    private boolean can_mob_hit;

    public DebugManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.quick_phase = false;
        this.can_mob_hit = false;
    }

    public boolean isQuickPhase() {
        return this.quick_phase;
    }

    public void setQuickPhase(boolean quick_phase) {
        this.quick_phase = quick_phase;
    }

    public boolean isCanMobHit() {
        return this.can_mob_hit;
    }

    public void setCanMobHit(boolean can_mob_hit) {
        this.can_mob_hit = can_mob_hit;
    }

}
