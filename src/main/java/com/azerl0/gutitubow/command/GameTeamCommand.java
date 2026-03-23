package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.team.Team;
import com.azerl0.gutitubow.team.TeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameTeamCommand implements CommandExecutor, TabExecutor {
    private final TeamManager teamManager;

    public GameTeamCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return true;

        @Nullable Player target = null;
        if (args.length >= 2) {
            if (args[1].equals("@s")) {
                target = player;
            } else {
                target = Bukkit.getPlayer(args[1]);
            }
        }
        switch (args[0]) {
            case "gui" -> {
                teamManager.getTeamGUI().open(player);
                return true;
            }
            case "getteam" -> {
                Component message;
                if (target == null) {
                    message = Component.text("ターゲットの取得中にエラーが発生しました。");
                } else {
                    Team team = teamManager.getPlayerTeam(target);
                    if (team == null) {
                        message = Component.text(target.getName() + "はチームに属していません");
                    } else {
                        message = Component.text(target.getName() + "が属するチームは").append(team.getTeamName()).append(Component.text("です"));
                    }
                }
                player.sendMessage(message);
                return true;
            }
            case "join" -> {
                if (target == null) {
                    player.sendMessage(Component.text("ターゲットの取得中にエラーが発生しました。"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(Component.text("引数が不足しています"));
                    return true;
                }
                Team team = teamManager.getTeam(args[2]);
                if (team == null) {
                    player.sendMessage(Component.text("チームの取得中にエラーが発生しました。"));
                } else {
                    teamManager.joinTeam(target, team);
                    player.sendMessage(Component.text(target.getName() + "をチーム: ").append(team.getTeamName().color(team.getTeamColor())).append(Component.text("に追加しました")));
                }
                return true;
            }
            case "leave" -> {
                if (target == null) {
                    player.sendMessage(Component.text("ターゲットの取得中にエラーが発生しました。"));
                    return true;
                }
                teamManager.leaveTeam(target);
                player.sendMessage(Component.text(target.getName() + "をチームから離脱させました"));
                return true;
            }
            case "list" -> {
                player.sendMessage(Component.text(teamManager.getTeams().size() + "のチームが存在しています" ));
                for (Team team : teamManager.getTeams()) {
                    player.sendMessage(Component.text(" - ").append(team.getTeamName().color(team.getTeamColor())));
                }
                return true;
            }
            case "info" -> {
                if (args.length == 3) {
                    player.sendMessage(Component.text("チームIDが不足しています"));
                    return true;
                }
                Team team = teamManager.getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(Component.text("チームの取得中にエラーが発生しました。"));
                } else {
                    player.sendMessage(team.getTeamName().color(team.getTeamColor()).append(Component.text( "の詳細")));
                    player.sendMessage(Component.text(team.getPlayers().size() + "人のプレイヤーが属しています"));
                    for (UUID uuid : team.getPlayers()) {
                        Player teamPlayer = Bukkit.getPlayer(uuid);
                        if (teamPlayer == null) continue;
                        String suffix = "";
                        if (team.getJumpMaster() != null && uuid == team.getJumpMaster().getUniqueId()) suffix = " (ジャンプマスター)";
                        player.sendMessage(Component.text(" - " + teamPlayer.getName() + suffix));
                    }
                }
                return true;

            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("gui", "join", "leave", "list", "info", "getteam");
        if (args.length == 2 && args[0].equals("leave")) return getPlayerList();
        if (args.length == 2 && args[0].equals("join")) return getPlayerList();


        if (args.length == 3 && args[0].equals("join")) return getTeamIds();
        if (args.length == 2 && args[0].equals("info")) return getTeamIds();
        return List.of();
    }

    private List<String> getTeamIds() {
        return teamManager.getTeamIds().stream().toList();
    }

    private List<String> getPlayerList() {
        List<String> list = new ArrayList<String>();
        list = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        list.add("@s");
        return list;
    }
}
