package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.game.Game;
import org.bukkit.plugin.java.JavaPlugin;

public class AbilityContext {
    private final JavaPlugin plugin;
    private final Game game;

    public AbilityContext(JavaPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    public JavaPlugin plugin() { return plugin; }
    public Game game() { return game; }
}
