package com.azerl0.gutitubow.fakeplayer;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class FakePlayerManager {
    private JavaPlugin plugin;

    public FakePlayerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FakePlayer createFakePlayer(final String name, final Location spawnLocation) {
        return new FakePlayer(plugin, name, spawnLocation);
    }
}
