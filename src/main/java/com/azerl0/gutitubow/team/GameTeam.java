package com.azerl0.gutitubow.team;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GutitubowGameMode;
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
    private @NotNull final List<UUID> players = new ArrayList<>();

    public GameTeam(Component teamName, ItemStack teamIconItem, NamedTextColor teamColor, Game game) {
        this.teamName = teamName;
        this.teamIconItem = teamIconItem;
        this.teamColor = teamColor;
        this.game = game;
        team.color(NamedTextColor.GREEN);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
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

    public @NotNull List<UUID> getPlayers() {
        return players;
    }

    public @NotNull List<UUID> getPlayersWithout(UUID uuid) {
        List<UUID> withoutPlayers = new ArrayList<>(players);
        withoutPlayers.remove(uuid);
        return withoutPlayers;
    }

    public @NotNull List<UUID> getAlivePlayers() {
        List<UUID> alivePlayers = new ArrayList<>(players);
        alivePlayers.retainAll(game.getAlivePlayers());
        return alivePlayers;
    }

    public @NotNull List<UUID> getAlivePlayersWithout(UUID uuid) {
        List<UUID> alivePlayers = new ArrayList<>(getAlivePlayers());
        alivePlayers.remove(uuid);
        return alivePlayers;
    }

    public void reset() {
        players.clear();
    }

    public void addPlayer(Player player) {
        if (players.contains(player.getUniqueId())) return;
        players.add(player.getUniqueId());
        team.addPlayer(player);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        team.removePlayer(player);
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    @Nullable
    public Player getJumpMaster() {
        if (players.isEmpty()) return null;
        return Bukkit.getPlayer(players.getFirst());
    }

    public void setJumpMaster(Player player) {
        if (!hasPlayer(player)) return;
        players.remove(player.getUniqueId());
        players.addFirst(player.getUniqueId());
    }

    public boolean isThisTeamJumpMaster(Player player) {
        Player jumpMaster = getJumpMaster();
        if (jumpMaster == null) return false;
        return jumpMaster.getUniqueId() == player.getUniqueId();
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
