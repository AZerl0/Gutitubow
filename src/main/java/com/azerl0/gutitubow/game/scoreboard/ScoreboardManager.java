package com.azerl0.gutitubow.game.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ScoreboardManager {
    private final Map<UUID, Scoreboard> playerScoreboard = new HashMap<>();
    private final Set<Player> echoPlayers = new HashSet<>();


    public @NotNull Scoreboard getScoreboard(@NotNull Player player) {
        if (!playerScoreboard.containsKey(player.getUniqueId())) playerScoreboard.put(player.getUniqueId(), Bukkit.getScoreboardManager().getNewScoreboard());

        return playerScoreboard.get(player.getUniqueId());
    }

    public @NotNull Team getTeam(@NotNull Scoreboard scoreboard, @NotNull Player player) {
        Team team = scoreboard.getPlayerTeam(player);
        if (team == null) {
            team = scoreboard.registerNewTeam("@" + player.getName());
            team.addPlayer(player);
            for (Player echoPlayer : echoPlayers) {
                Team oldTeam = scoreboard.getPlayerTeam(echoPlayer);
                if (oldTeam != null && oldTeam.hasPlayer(echoPlayer)) oldTeam.removePlayer(echoPlayer);
                team.addPlayer(echoPlayer);
            }

            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setCanSeeFriendlyInvisibles(true);
            team.setAllowFriendlyFire(false);

        }
        return team;

    }

    public @NotNull Team getTeam(@NotNull Player player) {
        Scoreboard board = getScoreboard(player);
        return getTeam(board, player);
    }

    public void addEchoPlayer(Player echoPlayer) {
        echoPlayers.add(echoPlayer);
        for (UUID uuid : playerScoreboard.keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) return;

            Team playerTeam = getTeam(player);

            playerTeam.addEntity(echoPlayer);
        }
    }

    public void removeEchoPlayer(Player echoPlayer) {
        echoPlayers.remove(echoPlayer);
    }
}
