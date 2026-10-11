package com.azerl0.gutitubow.team;

import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.gui.guis.TeamGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamManager {
    private final Game game;
    private final Map<String, GameTeam> teams = new LinkedHashMap<>();
    private final Map<UUID, GameTeam> playerTeams = new HashMap<>();
    private final TeamGUI teamGUI;

    public TeamManager(Game game) {
        this.game = game;
        teamGUI = new TeamGUI(this);
        initTeams();
    }

    public void initTeams() {
        removeAllTeam();
        switch (game.getGameSetting().getGameMode()) {
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

    public void removeAllTeam() {
        for (String id : new ArrayList<>(teams.keySet())) {
            removeTeam(id);
        }

        playerTeams.clear();
        if (teamGUI != null) teamGUI.updateGUI();
    }

    public void removeTeam(String teamId) {
        GameTeam team = getTeam(teamId);
        if (team == null) return;
        team.remove();
        teams.remove(teamId);
    }

    public Set<String> getTeamIds() {
        return teams.keySet();
    }

    public Collection<GameTeam> getTeams() {
        return teams.values();
    }

    public @Nullable GameTeam getPlayerTeam(@NotNull Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public @Nullable GameTeam getPlayerTeam(@NotNull UUID uuid) {
        return playerTeams.get(uuid);
    }

    @Nullable
    public GameTeam getTeam(String teamId) {
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
        teams.put(teamId.toLowerCase(), new GameTeam(teamName, itemStack, teamColor, game));
    }

    public void joinTeam(Player player, String teamId) {
        GameTeam gameTeam = teams.get(teamId);
        if (gameTeam != null) {
            joinTeam(player, gameTeam);
        }
    }

    public void joinTeam(@NotNull Player player, @NotNull GameTeam gameTeam) {
        leaveTeam(player);
        if (game.getSpectatorManager().isSpectator(player)) return;
        if (!teams.containsValue(gameTeam)) return;
        if (gameTeam.isFull(game.getGameSetting().getGameMode())) return;

        playerTeams.put(player.getUniqueId(), gameTeam);
        gameTeam.addPlayer(player);
        teamGUI.updateGUI();
    }

    public void leaveTeam(@NotNull Player player) {
        GameTeam gameTeam = getPlayerTeam(player);
        if (gameTeam != null) gameTeam.removePlayer(player);
        playerTeams.remove(player.getUniqueId());
        teamGUI.updateGUI();
    }

    public boolean isTeammate(Player player, Player otherPlayer) {
        GameTeam gameTeam = getPlayerTeam(player);
        GameTeam otherGameTeam = getPlayerTeam(otherPlayer);

        if (gameTeam == null) return false;
        return gameTeam == otherGameTeam;
    }

    public boolean isJumpMaster(Player player) {
        GameTeam team = getPlayerTeam(player);
        if (team == null) return false;
        return team.isThisTeamJumpMaster(player);
    }

    public Set<GameTeam> getAliveTeams() {
        Set<GameTeam> aliveGameTeams = new HashSet<>(teams.values());
        for (GameTeam gameTeam : teams.values()) if (gameTeam.isEliminated()) aliveGameTeams.remove(gameTeam);
        return aliveGameTeams;
    }

    public void resetTeams() {
        for (GameTeam gameTeam : teams.values()) resetTeam(gameTeam);
    }

    public void resetTeam(GameTeam gameTeam) {
        for (GamePlayer teamPlayer : gameTeam.getPlayers()) {
            playerTeams.remove(teamPlayer.getPlayerUUID());
        }
        gameTeam.reset();
        teamGUI.updateGUI();
    }

    public void randomTeam() {
        List<Player> skipPlayers = List.of();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.removeAll(skipPlayers);
        Collections.shuffle(players);

        for (Player player : players) {
            if (getPlayerTeam(player) != null) continue;
            GameTeam near = getNearCanJoinTeam();
            if (near == null) return;
            joinTeam(player, near);

        }
    }

    @Nullable
    public GameTeam getNearCanJoinTeam() {
        for (GameTeam team : teams.values()) {
            if (!team.isFull(game.getGameSetting().getGameMode())) return team;
        }
        return null;
    }
}
