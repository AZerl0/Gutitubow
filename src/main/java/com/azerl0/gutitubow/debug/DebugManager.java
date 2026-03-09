package com.azerl0.gutitubow.debug;

import org.bukkit.plugin.java.JavaPlugin;

public class DebugManager {
    private final JavaPlugin plugin;
    private boolean quick_phase;
    private boolean no_start_teleport;

    public DebugManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.quick_phase = false;
        this.no_start_teleport = false;
    }

    public boolean isQuickPhase() {
        return this.quick_phase;
    }

    public void setQuickPhase(boolean quick_phase) {
        this.quick_phase = quick_phase;
    }

    public boolean isNoStartTeleport() {
        return this.no_start_teleport;
    }

    public void setNoStartTeleport(boolean no_start_teleport) {
        this.no_start_teleport = no_start_teleport;
    }

}
