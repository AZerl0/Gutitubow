package com.azerl0.gutitubow.manager;

import org.bukkit.plugin.java.JavaPlugin;

public class DebugManager {
    private final JavaPlugin plugin;
    private boolean quick_phase;

    public DebugManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.quick_phase = false;
    }

    public boolean isQuickPhase() {
        return this.quick_phase;
    }

    public void setQuickPhase(boolean quick_phase) {
        this.quick_phase = quick_phase;
    }

}
