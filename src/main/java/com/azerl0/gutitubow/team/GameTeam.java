package com.azerl0.gutitubow.team;

import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GameTeam {
    private final Component teamName;
    private final ItemStack teamIconItem;
    private final NamedTextColor teamColor;
    private final Team team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(this.toString());
    private final Game game;
    private @NotNull
    final List<GamePlayer> players = new ArrayList<>();

    public GameTeam(Component teamName, ItemStack teamIconItem, NamedTextColor teamColor, Game game) {
        this.teamName = teamName;
        this.teamIconItem = teamIconItem;
        this.teamColor = teamColor;
        this.game = game;
        team.color(NamedTextColor.GREEN);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setCanSeeFriendlyInvisibles(true);
        team.setAllowFriendlyFire(false);
    }

    public Component getTeamName() {
        return teamName;
    }

    public ItemStack getTeamIconItem() {
        return teamIconItem;
    }

    public NamedTextColor getTeamColor() {
        return teamColor;
    }

    public void reset() {
        players.clear();
    }


    public @NotNull List<GamePlayer> getPlayers() {
        return players;
    }

    public @NotNull List<GamePlayer> getPlayersWithout(UUID uuid) {
        GamePlayer gamePlayer = game.getGamePlayer(uuid);
        List<GamePlayer> withoutPlayers = new ArrayList<>(players);
        withoutPlayers.remove(gamePlayer);
        return withoutPlayers;
    }

    public @NotNull List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alivePlayers = new ArrayList<>(players);
        alivePlayers.retainAll(game.getAlivePlayers());
        return alivePlayers;
    }

    public @NotNull List<GamePlayer> getAlivePlayersWithout(UUID uuid) {
        GamePlayer gamePlayer = game.getGamePlayer(uuid);
        List<GamePlayer> alivePlayers = new ArrayList<>(getAlivePlayers());
        alivePlayers.remove(gamePlayer);
        return alivePlayers;
    }

    public List<GamePlayer> getDeadPlayers() {
        List<GamePlayer> deadPlayers = new ArrayList<>(getPlayers());
        deadPlayers.removeAll(game.getAlivePlayers());
        return deadPlayers;
    }

    public void addPlayer(Player player) {
        GamePlayer gamePlayer = game.getGamePlayer(player);
        if (players.contains(gamePlayer)) return;
        players.add(gamePlayer);
        team.addPlayer(player);
    }

    public void removePlayer(Player player) {
        players.remove(game.getGamePlayer(player));
        team.removePlayer(player);
    }

    public boolean containsPlayer(Player player) {
        return players.contains(game.getGamePlayer(player));
    }

    public boolean isTeammate(Player target) {
        GameTeam playerTeam = game.getGameTeam(target);
        return playerTeam == this;
    }

    public boolean isTeammate(UUID uuid) {
        GameTeam playerTeam = game.getGameTeam(uuid);
        return playerTeam == this;
    }

    @Nullable
    public Player getJumpMaster() {
        if (players.isEmpty()) return null;
        return Bukkit.getPlayer(players.getFirst().getPlayerUUID());
    }

    public void setJumpMaster(Player player) {
        if (!containsPlayer(player)) return;
        GamePlayer gamePlayer = game.getGamePlayer(player);
        players.remove(gamePlayer);
        players.addFirst(gamePlayer);
    }

    public boolean isThisTeamJumpMaster(Player player) {
        Player jumpMaster = getJumpMaster();
        if (jumpMaster == null) return false;
        return jumpMaster.getUniqueId().equals(player.getUniqueId());
    }

    public boolean isEliminated() {
        return getAlivePlayers().isEmpty();
    }

    public void remove() {
        team.unregister();
    }

    public boolean isFull(GutitubowGameMode gameMode) {
        int max =
                switch (gameMode) {
                    case SOLO -> 1;
                    case DUO -> 2;
                    case TRIO -> 3;
                    case null -> 0;
                };
        return players.size() >= max;
    }
}
