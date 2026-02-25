package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.manager.GamePlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AbilityContext {
    private final JavaPlugin plugin;
    private final GamePlayerManager gamePlayerManager;

    public AbilityContext(JavaPlugin plugin, GamePlayerManager gamePlayerManager) {
        this.plugin = plugin;
        this.gamePlayerManager = gamePlayerManager;
    }

    public JavaPlugin plugin() { return plugin; }
    public GamePlayerManager gamePlayerManager() { return gamePlayerManager; }
}
