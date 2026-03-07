package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.carepackage.CarePackageManager;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.map.MapManager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameManager {
    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;
    private final MapManager mapManager;
    private final DebugManager debugManager;
    private final CarePackageManager carePackageManager;
    private final Game game;
    private BukkitTask gameTask;

    public GameManager(
            JavaPlugin plugin,
            AbilityManager abilityManager,
            MapManager mapManager,
            DebugManager debugManager,
            CarePackageManager carePackageManager
    ) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.abilityManager = abilityManager;
        this.debugManager = debugManager;
        this.carePackageManager = carePackageManager;
        game = newGame();
    }

    public Game newGame() {
        return new Game(plugin, mapManager.getTestMap(), carePackageManager, abilityManager, debugManager);
    }

    public void resetGame(Game game) {
        game.reset();
    }

    public boolean isStarted() {
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
