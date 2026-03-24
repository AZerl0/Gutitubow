package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.carepackage.CarePackageManager;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.map.MapManager;

import com.azerl0.gutitubow.packet.EntityGlowManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameManager {
    private final JavaPlugin plugin;
    private final MapManager mapManager;
    private final DebugManager debugManager;
    private final EntityGlowManager glowManager;
    private BukkitTask gameTask;

    public GameManager(
            JavaPlugin plugin,
            MapManager mapManager,
            DebugManager debugManager,
            EntityGlowManager glowManager
    ) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.debugManager = debugManager;
        this.glowManager = glowManager;
    }

    public Game newGame() {
        return new Game(plugin, mapManager.getTestMap(), glowManager, debugManager);
    }

    public void resetGame(Game game) {
        game.reset();
    }

    public boolean isStarted(Game game) {
        return game.isStarted();
    }

    public void startGame(Game game) {
        if (game == null || game.isStarted()) return;

        Bukkit.getServer().broadcast(Component.text("start"));

        game.start();

        if (gameTask != null && !gameTask.isCancelled()) return;

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!game.isStarted()) {
                    this.cancel();
                    return;
                }
                game.tick();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
