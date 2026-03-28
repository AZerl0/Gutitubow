package com.azerl0.gutitubow.game.scoreboard;

import com.azerl0.gutitubow.game.GamePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBoardManager {
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, PlayerBoard> playerBoards = new HashMap<>();

    public PlayerBoardManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }



    public PlayerBoard getPlayerBoard(GamePlayer gamePlayer) {
        UUID uuid = gamePlayer.getPlayer().getUniqueId();
        gamePlayer.getPlayer().getUniqueId();
        if (playerBoards.containsKey(uuid)) {
            return playerBoards.get(uuid);
        } else {
            PlayerBoard playerBoard = new PlayerBoard(gamePlayer, scoreboardManager.getScoreboard(gamePlayer.getPlayer()));
            playerBoards.put(uuid, playerBoard);
            return playerBoard;
        }
    }
}
