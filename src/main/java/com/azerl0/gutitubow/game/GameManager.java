package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.game.scoreboard.ScoreboardManager;
import com.azerl0.gutitubow.map.MapManager;

import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
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
    private final ScoreboardManager scoreboardManager;
    private final PlayerSkinChanger playerSkinChanger;
    private BukkitTask gameTask;

    public GameManager(
            JavaPlugin plugin,
            MapManager mapManager,
            DebugManager debugManager,
            EntityGlowManager glowManager,
            ScoreboardManager scoreboardManager,
            PlayerSkinChanger playerSkinChanger
    ) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.debugManager = debugManager;
        this.glowManager = glowManager;
        this.scoreboardManager = scoreboardManager;
        this.playerSkinChanger = playerSkinChanger;
    }

    public Game newGame() {
        return new Game(plugin, mapManager.getTestMap(), glowManager, debugManager, scoreboardManager, playerSkinChanger);
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
