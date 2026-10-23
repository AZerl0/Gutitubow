package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class GameManager {
    private final Gutitubow gutitubow;
    private final Set<GameImpl> gameImplSet = new HashSet<>();
    private final BukkitTask gameTask;

    public GameManager(
            Gutitubow gutitubow
    ) {
        this.gutitubow = gutitubow;

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(gutitubow, 0L, 1L);
    }

    public Game newGame() {
        GameImpl gameImpl = new GameImpl(gutitubow);
        gameImplSet.add(gameImpl);
        return gameImpl;
    }

    public boolean isStarted(GameImpl gameImpl) {
        return gameImpl.isStarted();
    }

    private void tick() {
        for (GameImpl gameImpl : gameImplSet) {
            gameImpl.update();
        }
    }
}
