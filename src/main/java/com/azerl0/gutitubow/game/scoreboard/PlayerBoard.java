package com.azerl0.gutitubow.game.scoreboard;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePlayer;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.ScoreboardManager;

public class PlayerBoard {
    private final GamePlayer gamePlayer;
    private final Game game;
    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private final Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
    private final Objective obj;
    private final  Scoreboard lobbyScoreboard = scoreboardManager.getNewScoreboard();
    private final Objective lobbyObj;
    private final Team _playerName;
    private final Team _alivePlayerCount;
    private final Team _insideSafeArea;
    private final Team _abilityName;

    public PlayerBoard(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        this.game = gamePlayer.getGame();
        Component scoreboardName = Component.text("ぐちつボウ").color(NamedTextColor.GREEN);
        obj = scoreboard.registerNewObjective("gutitubow.info", Criteria.DUMMY, scoreboardName);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.numberFormat(NumberFormat.blank());

        lobbyObj = lobbyScoreboard.registerNewObjective("gutitubow.info", Criteria.DUMMY, scoreboardName);
        lobbyObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        lobbyObj.numberFormat(NumberFormat.blank());

        _playerName = scoreboard.registerNewTeam("line1");
        String entry = "§1";
        _playerName.addEntry(entry);
        setPlayerName(Component.text("Players: "));
        obj.getScore(entry).setScore(-1);

        _alivePlayerCount = scoreboard.registerNewTeam("line2");
        entry = "§2";
        _alivePlayerCount.addEntry(entry);
        obj.getScore(entry).setScore(-2);

        _insideSafeArea = scoreboard.registerNewTeam("line3");
        entry = "§3";
        _insideSafeArea.addEntry(entry);
        obj.getScore(entry).setScore(-3);

        _abilityName = scoreboard.registerNewTeam("line4");
        entry = "§4";
        _abilityName.addEntry(entry);
        obj.getScore(entry).setScore(-4);
    }

    public void update() {
        Player player = this.gamePlayer.getPlayer();
        if (game.isStarted() && game.getPhase() != -1) {
            GamePlayer gamePlayer = game.getGamePlayer(player);
            Ability ability = gamePlayer.getAbility();
            Component abilityName = ability == null ? Component.text("None") : ability.getType().getName();
            setPlayerName(Component.text("Player: " + player.getName()).color(NamedTextColor.GREEN));
            updateAlivePlayerCount();
            updateInsideSafeArea(player);
            setAbilityName(abilityName.color(NamedTextColor.GOLD));
            player.setScoreboard(scoreboard);
        } else {
            player.setScoreboard(lobbyScoreboard);
        }
    }

    private void setPlayerName(Component component) {
        _playerName.prefix(component);
    }

    private void updateAlivePlayerCount() {
        int max = game.getPlayers().size();
        int current = game.getAlivePlayers().size();
        _alivePlayerCount.prefix(Component.text(" - 生存人数.").color(NamedTextColor.GREEN)
                .append(Component.text(" " + current).color(NamedTextColor.GOLD))
                .append(Component.text("/" + max).color(NamedTextColor.GREEN))
        );
    }

    private void updateInsideSafeArea(Player player) {
        boolean isInside =  game.isInsideNextSafeArea(player.getLocation());

        Component insideState = isInside ? Component.text("内").color(NamedTextColor.GOLD) : Component.text("外").color(NamedTextColor.RED);

        _insideSafeArea.prefix(
                Component.text(" - セーフエリア: [").color(NamedTextColor.GREEN)
                        .append(insideState)
                        .append(Component.text("]").color(NamedTextColor.GREEN))
        );
    }

    private void setAbilityName(Component abilityName) {
        _abilityName.prefix(
                Component.text(" - 能力: ").color(NamedTextColor.GREEN).append(abilityName)
        );
    }
}
