package com.azerl0.gutitubow.team;

import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.gui.guis.TeamGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamManager {
    private final Game game;
    private final Map<String, Team> teams = new LinkedHashMap<>();
    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private final TeamGUI teamGUI;

    public TeamManager(Game game) {
        this.game = game;
        initTeams();
        teamGUI = new TeamGUI(this);
    }

    public void initTeams() {
        removeAllTeams();
        switch (game.getGameMode()) {
            case DUO -> {
                addTeam("team-a", Component.text("Aチーム"), Material.RED_STAINED_GLASS_PANE, NamedTextColor.RED);
                addTeam("team-b", Component.text("Bチーム"), Material.ORANGE_STAINED_GLASS_PANE, NamedTextColor.GOLD);
                addTeam("team-c", Component.text("Cチーム"), Material.YELLOW_STAINED_GLASS_PANE, NamedTextColor.YELLOW);
                addTeam("team-d", Component.text("Dチーム"), Material.LIME_STAINED_GLASS_PANE, NamedTextColor.GREEN);
                addTeam("team-e", Component.text("Eチーム"), Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.DARK_GREEN);
                addTeam("team-f", Component.text("Fチーム"), Material.CYAN_STAINED_GLASS_PANE, NamedTextColor.DARK_BLUE);
                addTeam("team-g", Component.text("Gチーム"), Material.LIGHT_BLUE_STAINED_GLASS_PANE, NamedTextColor.AQUA);
                addTeam("team-h", Component.text("Hチーム"), Material.BLUE_STAINED_GLASS_PANE, NamedTextColor.BLUE);
                addTeam("team-i", Component.text("Iチーム"), Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.DARK_PURPLE);
                addTeam("team-j", Component.text("Jチーム"), Material.MAGENTA_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE);
                addTeam("team-k", Component.text("Kチーム"), Material.PINK_STAINED_GLASS_PANE, NamedTextColor.WHITE);
                addTeam("team-l", Component.text("Lチーム"), Material.WHITE_STAINED_GLASS_PANE, NamedTextColor.WHITE);
                addTeam("team-m", Component.text("Mチーム"), Material.GRAY_STAINED_GLASS_PANE, NamedTextColor.DARK_GRAY);
                addTeam("team-n", Component.text("Nチーム"), Material.LIGHT_GRAY_STAINED_GLASS_PANE, NamedTextColor.GRAY);
                addTeam("team-o", Component.text("Oチーム"), Material.BLACK_STAINED_GLASS_PANE, NamedTextColor.BLACK);
                addTeam("team-p", Component.text("Pチーム"), Material.BROWN_STAINED_GLASS_PANE, NamedTextColor.WHITE);
            }
            case TRIO -> {
                addTeam("team-a", Component.text("Aチーム"), Material.RED_STAINED_GLASS_PANE, NamedTextColor.RED);
                addTeam("team-b", Component.text("Bチーム"), Material.YELLOW_STAINED_GLASS_PANE, NamedTextColor.YELLOW);
                addTeam("team-c", Component.text("Cチーム"), Material.LIME_STAINED_GLASS_PANE, NamedTextColor.GREEN);
                addTeam("team-d", Component.text("Dチーム"), Material.LIGHT_BLUE_STAINED_GLASS_PANE, NamedTextColor.AQUA);
                addTeam("team-e", Component.text("Eチーム"), Material.BLUE_STAINED_GLASS_PANE, NamedTextColor.BLUE);
                addTeam("team-f", Component.text("Fチーム"), Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE);
                addTeam("team-g", Component.text("Gチーム"), Material.PINK_STAINED_GLASS_PANE, NamedTextColor.WHITE);
                addTeam("team-h", Component.text("Hチーム"), Material.WHITE_STAINED_GLASS_PANE, NamedTextColor.WHITE);

            }
        }
    }

    public Game getGame() {
        return game;
    }

    public TeamGUI getTeamGUI() {
        return teamGUI;
    }

    public void removeAllTeams() {
        teams.clear();
        playerTeams.clear();
    }

    public Set<String> getTeamIds() {
        return teams.keySet();
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    @Nullable
    public Team getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    @Nullable
    public Team getTeam(String teamId) {
        return teams.get(teamId);
    }

    public void addTeam(String teamId, Component teamName, Material teamIconItem, NamedTextColor teamColor) {
        if (teams.containsKey(teamId.toLowerCase())) return;
        ItemStack itemStack = ItemStack.of(teamIconItem);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(teamName.color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }
        itemStack.setItemMeta(meta);
        teams.put(teamId.toLowerCase(), new Team(teamName, itemStack, teamColor));
    }

    public void removeTeam(String teamId) {
        teams.remove(teamId);
    }

    public void joinTeam(Player player, String teamId) {
        Team team = teams.get(teamId);
        if (team != null) {
            joinTeam(player, team);
        }
    }

    public void joinTeam(Player player, Team team) {
        leaveTeam(player);
        if (!teams.containsValue(team)) return;

        playerTeams.put(player.getUniqueId(), team);
        team.addPlayer(player);
        teamGUI.updateGUI();
    }

    public void leaveTeam(Player player) {
        Team team = getPlayerTeam(player);
        if (team != null) team.removePlayer(player);
        playerTeams.remove(player.getUniqueId());
        teamGUI.updateGUI();
    }

    public boolean isTeammate(Player player, Player otherPlayer) {
        Team team = getPlayerTeam(player);
        Team otherTeam = getPlayerTeam(otherPlayer);

        if (team == null) return false;
        return team == otherTeam;
    }

    public void resetTeams() {
        for (Team team : teams.values()) resetTeam(team);
    }

    public void resetTeam(Team team) {
        for (UUID uuid : team.getPlayers()) {
            playerTeams.remove(uuid);
        }
        team.clear();
        teamGUI.updateGUI();
    }

    public void setRandomTeam() {
        resetTeams();

    }
}
