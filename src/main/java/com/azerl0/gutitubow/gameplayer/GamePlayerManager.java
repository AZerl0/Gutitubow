package com.azerl0.gutitubow.gameplayer;


import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GamePlayerManager {
    private final Gutitubow gutitubow;
    private final Game game;
    private final Map<UUID, GamePlayer> gamePlayers = new HashMap<>();
    private final BukkitTask updateTask;


    public GamePlayerManager(Gutitubow gutitubow, Game game) {
        this.gutitubow = gutitubow;
        this.game = game;
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(gutitubow, 0L, 1L);
    }

    public @NotNull GamePlayer getGamePlayer(Player player) {
        if (!gamePlayers.containsKey(player.getUniqueId())) {
            gamePlayers.put(player.getUniqueId(), new GamePlayer(gutitubow, player, game));
        }
        return gamePlayers.get(player.getUniqueId());
    }

    public Set<GamePlayer> getGamePlayers() {
        return new HashSet<>(gamePlayers.values());
    }

    private void tick() {
        for (GamePlayer gamePlayer : new HashSet<>(getGamePlayers())) {
            gamePlayer.tick();
        }
    }
}
