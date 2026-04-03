package com.azerl0.gutitubow.game.scoreboard;

import com.azerl0.gutitubow.game.GamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBoardManager {
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, PlayerBoard> playerBoards = new HashMap<>();

    public PlayerBoardManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }



    public @Nullable PlayerBoard getPlayerBoard(@NotNull GamePlayer gamePlayer) {
        UUID uuid = gamePlayer.getPlayerUUID();
        if (playerBoards.containsKey(uuid)) {
            return playerBoards.get(uuid);
        } else {
            Player player = gamePlayer.getPlayer();
            if (player == null) return null;
            PlayerBoard playerBoard = new PlayerBoard(gamePlayer, scoreboardManager.getScoreboard(player));
            playerBoards.put(uuid, playerBoard);
            return playerBoard;
        }
    }
}
