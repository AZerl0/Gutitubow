package com.azerl0.gutitubow.game.scoreboard;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private Game game;
    private final Map<UUID, PlayerBoard> playerBoards = new HashMap<>();

    public ScoreboardManager(Game game) {
        this.game = game;
    }

    public PlayerBoard getPlayerBoard(GamePlayer gamePlayer) {
        UUID uuid = gamePlayer.getPlayer().getUniqueId();
        gamePlayer.getPlayer().getUniqueId();
        if (playerBoards.containsKey(uuid)) {
            return playerBoards.get(uuid);
        } else {
            PlayerBoard playerBoard = new PlayerBoard(gamePlayer);
            playerBoards.put(uuid, playerBoard);
            return playerBoard;
        }
    }
}
