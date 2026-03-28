package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AbilityContext {
    private final JavaPlugin plugin;
    private final Game game;
    private final EntityGlowManager glowManager;

    public AbilityContext(JavaPlugin plugin, Game game, EntityGlowManager glowManager) {
        this.plugin = plugin;
        this.game = game;
        this.glowManager = glowManager;
    }

    public JavaPlugin plugin() { return plugin; }
    public Game game() { return game; }
    public EntityGlowManager glowManager() { return glowManager; }
}
