package com.azerl0.gutitubow.gameplayer;


import com.azerl0.gutitubow.game.Game;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GamePlayerManager {
    private final JavaPlugin plugin;
    private final Game game;
    private final Map<UUID, GamePlayer> gamePlayers = new HashMap<>();


    public GamePlayerManager(JavaPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    public @NotNull GamePlayer getGamePlayer(Player player) {
        if (!gamePlayers.containsKey(player.getUniqueId())) {
            gamePlayers.put(player.getUniqueId(), new GamePlayer(this.plugin, player, game));
        }
        return gamePlayers.get(player.getUniqueId());
    }
}
