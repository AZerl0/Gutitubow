package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.map.MapManager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameManager {
    private final Gutitubow gutitubow;
    private final MapManager mapManager;
    private final DebugManager debugManager;
    private final GutitubowManagers gutitubowManagers;
    private BukkitTask gameTask;

    public GameManager(
            Gutitubow gutitubow,
            MapManager mapManager,
            DebugManager debugManager,
            GutitubowManagers gutitubowManagers
    ) {
        this.gutitubow = gutitubow;
        this.mapManager = mapManager;
        this.debugManager = debugManager;
        this.gutitubowManagers = gutitubowManagers;
    }

    public Game newGame() {
        return new Game(gutitubow, mapManager.getTestMap(), debugManager, gutitubowManagers);
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
        }.runTaskTimer(gutitubow, 0L, 1L);
    }
}
