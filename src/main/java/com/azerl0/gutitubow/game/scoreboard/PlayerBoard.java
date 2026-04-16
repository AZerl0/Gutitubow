package com.azerl0.gutitubow.game.scoreboard;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.PlayerUtil;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerBoard {
    private final GamePlayer gamePlayer;
    private final Game game;
    private final Scoreboard scoreboard;
    private final Objective obj;
    private final List<String> entries = List.of("§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f", "§g", "§k", "§l", "§m", "§n", "§o", "§r");

    private final scoreboardComponent _playerName;
    private final scoreboardComponent _alivePlayerCount;
    private final scoreboardComponent _insideSafeArea;
    private final scoreboardComponent _abilityName;
    private final scoreboardComponent _teammate1name;
    private final scoreboardComponent _teammate1ability;
    private final scoreboardComponent _teammate1health;
    private final scoreboardComponent _teammate1potion;
    private final scoreboardComponent _teammate2name;
    private final scoreboardComponent _teammate2ability;
    private final scoreboardComponent _teammate2health;
    private final scoreboardComponent _teammate2potion;

    public PlayerBoard(GamePlayer gamePlayer, Scoreboard scoreboard) {
        this.gamePlayer = gamePlayer;
        this.game = gamePlayer.getGame();
        this.scoreboard = scoreboard;
        Component scoreboardName = Component.text("ぐちつボウ").color(NamedTextColor.GREEN);
        obj = scoreboard.registerNewObjective("gutitubow.info", Criteria.DUMMY, scoreboardName);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.numberFormat(NumberFormat.blank());


        _playerName = new scoreboardComponent(1);
        _alivePlayerCount = new scoreboardComponent(2);
        _insideSafeArea = new scoreboardComponent(3);
        _abilityName = new scoreboardComponent(4);
        _teammate1name = new scoreboardComponent(5);
        _teammate1ability = new scoreboardComponent(6);
        _teammate1health = new scoreboardComponent(7);
        _teammate1potion = new scoreboardComponent(8);
        _teammate2name = new scoreboardComponent(9);
        _teammate2ability = new scoreboardComponent(10);
        _teammate2health = new scoreboardComponent(11);
        _teammate2potion = new scoreboardComponent(12);
    }

    private class scoreboardComponent {
        private final int index;
        private final String entry;
        private final Team name;

        private scoreboardComponent(int index) {
            this.index = index;
            name = scoreboard.registerNewTeam("line" + index);
            entry = entries.get(index);
            obj.getScore(entry).setScore(-index);
            setHide(false);
        }

        private void setDisplay(Component display) {
            name.prefix(display);
        }

        private void setHide(boolean hide) {
            if (hide) {
                name.removeEntry(entry);
                scoreboard.resetScores(entry);
            } else {
                name.addEntry(entry);
                obj.getScore(entry).setScore(-index);
            }
        }
    }

    public void update() {
        Player player = this.gamePlayer.getPlayer();
        if (player == null) return;
        if (game.isStarted() && game.getPhase() != -1) {
            _playerName.setDisplay(Component.text("Player: " + player.getName()).color(NamedTextColor.GREEN));
            updateAlivePlayerCount();
            updateInsideSafeArea(player);
            setAbilityName(getAbilityName(player).color(NamedTextColor.GOLD));

            _playerName.setHide(false);
            _alivePlayerCount.setHide(false);
            _insideSafeArea.setHide(false);
            _abilityName.setHide(false);
            updateTeammateInfo();
        } else {
            hideAll();
        }
        player.setScoreboard(scoreboard);
    }

    private void updateTeammateInfo() {
        GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
        if (gamePlayer.getPlayer() == null) return;
        GameTeam team = gamePlayer.getTeam();

        if (gameMode == GutitubowGameMode.DUO) {
            if (team == null) return;
            List<UUID> teammates = team.getPlayersWithout(gamePlayer.getPlayer().getUniqueId());
            if (!teammates.isEmpty()) {
                Player teammate1 = Bukkit.getPlayer(teammates.getFirst());
                if (teammate1 != null) {
                    updateTeammate1(teammate1);
                    setHideTeammate1(false);
                }
            }

            setHideTeammate2(true);

        } else if (gameMode == GutitubowGameMode.TRIO) {
            if (team == null) return;
            List<UUID> teammates = team.getPlayersWithout(gamePlayer.getPlayer().getUniqueId());

            if (!teammates.isEmpty()) {
                Player teammate1 = Bukkit.getPlayer(teammates.getFirst());
                if (teammate1 != null) {
                    updateTeammate1(teammate1);
                    setHideTeammate1(false);
                }
            }

            if (teammates.size() >= 2) {
                Player teammate2 = Bukkit.getPlayer(teammates.get(1));
                if (teammate2 != null) {
                    updateTeammate2(teammate2);
                    setHideTeammate2(false);
                }
            }


        } else {
            setHideTeammate1(true);
            setHideTeammate2(true);
        }
    }

    private void updateTeammate1(@NotNull Player teammate) {
        if (!teammate.isOnline()) return;
        GamePlayer gamePlayer = game.getGamePlayer(teammate);
        _teammate1name.setDisplay(Component.text("Team: " + teammate.getName()).color(NamedTextColor.GOLD));
        _teammate1ability.setDisplay(Component.text("能力: ").append(getAbilityName(teammate)).color(NamedTextColor.GOLD));
        _teammate1health.setDisplay(Component.text("体力: ").color(NamedTextColor.GOLD).append(PlayerUtil.getHealthComponent(teammate)));
        _teammate1potion.setDisplay(Component.text("ポーション所持数: ").color(NamedTextColor.GOLD).append(Component.text(gamePlayer.getPotionAmount() + "個").color(NamedTextColor.RED)));
    }

    private void updateTeammate2(@NotNull Player teammate) {
        if (!teammate.isOnline()) return;
        GamePlayer gamePlayer = game.getGamePlayer(teammate);
        _teammate2name.setDisplay(Component.text("Team: " + teammate.getName()).color(NamedTextColor.GOLD));
        _teammate2ability.setDisplay(Component.text("能力: ").append(getAbilityName(teammate)).color(NamedTextColor.GOLD));
        _teammate2health.setDisplay(Component.text("体力: ").color(NamedTextColor.GOLD).append(PlayerUtil.getHealthComponent(teammate)));
        _teammate2potion.setDisplay(Component.text("ポーション所持数: ").color(NamedTextColor.GOLD).append(Component.text(gamePlayer.getPotionAmount() + "個").color(NamedTextColor.RED)));
    }

    private void setHideTeammate1(boolean hide) {
        _teammate1name.setHide(hide);
        _teammate1ability.setHide(hide);
        _teammate1health.setHide(hide);
        _teammate1potion.setHide(hide);
    }

    private void setHideTeammate2(boolean hide) {
        _teammate2name.setHide(hide);
        _teammate2ability.setHide(hide);
        _teammate2health.setHide(hide);
        _teammate2potion.setHide(hide);
    }

    private Component getAbilityName(@NotNull Player player) {
        GamePlayer gamePlayer = game.getGamePlayer(player);
        Ability ability = gamePlayer.getAbility();
        if (ability == null) return Component.text("None");
        return ability.getType().getName();
    }

    private void hideAll() {
        _playerName.setHide(true);
        _alivePlayerCount.setHide(true);
        _insideSafeArea.setHide(true);
        _abilityName.setHide(true);
        setHideTeammate1(true);
        setHideTeammate2(true);
    }

    private void updateAlivePlayerCount() {
        int max = game.getPlayers().size();
        int current = game.getAlivePlayers().size();
        _alivePlayerCount.setDisplay(Component.text(" - 生存人数.").color(NamedTextColor.GREEN)
                .append(Component.text(" " + current).color(NamedTextColor.GOLD))
                .append(Component.text("/" + max).color(NamedTextColor.GREEN))
        );
    }

    private void updateInsideSafeArea(Player player) {
        boolean isInside = game.isInsideNextSafeArea(player.getLocation());

        Component insideState = isInside ? Component.text("内").color(NamedTextColor.GOLD) : Component.text("外").color(NamedTextColor.RED);

        _insideSafeArea.setDisplay(
                Component.text(" - セーフエリア: [").color(NamedTextColor.GREEN)
                        .append(insideState)
                        .append(Component.text("]").color(NamedTextColor.GREEN))
        );
    }

    private void setAbilityName(Component abilityName) {
        _abilityName.setDisplay(
                Component.text(" - 能力: ").color(NamedTextColor.GREEN).append(abilityName)
        );
    }
}
