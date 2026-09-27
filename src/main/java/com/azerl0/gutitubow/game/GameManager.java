package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.debug.DebugManager;
import com.azerl0.gutitubow.gamemap.GameMapManager;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class GameManager {
    private final Gutitubow gutitubow;
    private final GameMapManager gameMapManager;
    private final DebugManager debugManager;
    private final GutitubowManagers gutitubowManagers;
    private final Set<Game> gameSet = new HashSet<>();
    private BukkitTask gameTask;

    public GameManager(
            Gutitubow gutitubow,
            GameMapManager gameMapManager,
            DebugManager debugManager,
            GutitubowManagers gutitubowManagers
    ) {
        this.gutitubow = gutitubow;
        this.gameMapManager = gameMapManager;
        this.debugManager = debugManager;
        this.gutitubowManagers = gutitubowManagers;

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(gutitubow, 0L, 1L);
    }

    public Game newGame() {
        Game game = new Game(gutitubow, debugManager, gutitubowManagers);
        gameSet.add(game);
        return game;
    }

    public void resetGame(Game game) {
        game.reset();
    }

    public boolean isStarted(Game game) {
        return game.isStarted();
    }

    private void tick() {
        for (Game game : gameSet) {
            game.update();
        }
    }
}
