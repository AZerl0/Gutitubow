package com.azerl0.gutitubow.game.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final Map<UUID, Scoreboard> playerScoreboard = new HashMap<>();


    public @NotNull Scoreboard getScoreboard(Player player) {
        if (!playerScoreboard.containsKey(player.getUniqueId())) playerScoreboard.put(player.getUniqueId(), Bukkit.getScoreboardManager().getNewScoreboard());
        return playerScoreboard.get(player.getUniqueId());
    }

}
