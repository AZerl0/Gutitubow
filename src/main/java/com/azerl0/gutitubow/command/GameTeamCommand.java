package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.game.SpectatorManager;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.team.TeamManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GameTeamCommand implements CommandExecutor, TabExecutor {
    private final TeamManager teamManager;
    private final SpectatorManager spectatorManager;

    public GameTeamCommand(TeamManager teamManager, SpectatorManager spectatorManager) {
        this.teamManager = teamManager;
        this.spectatorManager = spectatorManager;
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
                    GameTeam gameTeam = teamManager.getPlayerTeam(target);
                    if (gameTeam == null) {
                        message = Component.text(target.getName() + "はチームに属していません");
                    } else {
                        message = Component.text(target.getName() + "が属するチームは").append(gameTeam.getTeamName()).append(Component.text("です"));
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
                if (spectatorManager.isSpectator(target)) {
                    player.sendMessage(Component.text("そのプレイヤーは観戦者に設定されています。\n追加するには対象のプレイヤーを観戦者から削除してください。").color(NamedTextColor.RED));
                    return true;
                }
                GameTeam gameTeam = teamManager.getTeam(args[2]);
                if (gameTeam == null) {
                    player.sendMessage(Component.text("チームの取得中にエラーが発生しました。").color(NamedTextColor.RED));
                } else {
                    if (gameTeam.isFull(teamManager.getGame().getGameSetting().getGameMode())) {
                        player.sendMessage(Component.text("チーム: ").append(gameTeam.getTeamName().color(gameTeam.getTeamColor())).append(Component.text("は満員です")));
                    } else {
                        teamManager.joinTeam(target, gameTeam);
                        player.sendMessage(Component.text(target.getName() + "をチーム: ").append(gameTeam.getTeamName().color(gameTeam.getTeamColor())).append(Component.text("に追加しました")));
                    }
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
                for (GameTeam gameTeam : teamManager.getTeams()) {
                    player.sendMessage(Component.text(" - ").append(gameTeam.getTeamName().color(gameTeam.getTeamColor())));
                }
                return true;
            }
            case "info" -> {
                if (args.length == 3) {
                    player.sendMessage(Component.text("チームIDが不足しています"));
                    return true;
                }
                GameTeam gameTeam = teamManager.getTeam(args[1]);
                if (gameTeam == null) {
                    player.sendMessage(Component.text("チームの取得中にエラーが発生しました。"));
                } else {
                    player.sendMessage(gameTeam.getTeamName().color(gameTeam.getTeamColor()).append(Component.text( "の詳細")));
                    player.sendMessage(Component.text(gameTeam.getPlayers().size() + "人のプレイヤーが属しています"));
                    for (UUID uuid : gameTeam.getPlayers()) {
                        Player teamPlayer = Bukkit.getPlayer(uuid);
                        if (teamPlayer == null) continue;
                        String suffix = "";
                        if (gameTeam.getJumpMaster() != null && uuid == gameTeam.getJumpMaster().getUniqueId()) suffix = " (ジャンプマスター)";
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

    public static void register(Commands registrar) {
        registrar.register(
                literal("gameteam")
                        .requires(source -> source.getSender().hasPermission("gutitubow.gameteam"))
                        .then(literal("gui").executes(ctx -> openGui(ctx.getSource().getSender())))
                        .then(literal("list").executes(ctx -> list(ctx.getSource().getSender())))
                        .then(literal("getteam")
                                .then(argument("target", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestPlayers(builder))
                                        .executes(ctx -> getTeam(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "target")))))
                        .then(literal("leave")
                                .then(argument("target", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestPlayers(builder))
                                        .executes(ctx -> leave(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "target")))))
                        .then(literal("join")
                                .then(argument("target", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestPlayers(builder))
                                        .then(argument("teamId", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    getTeamManager().getTeamIds().forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> join(
                                                        ctx.getSource().getSender(),
                                                        StringArgumentType.getString(ctx, "target"),
                                                        StringArgumentType.getString(ctx, "teamId")
                                                )))))
                        .then(literal("info")
                                .then(argument("teamId", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            getTeamManager().getTeamIds().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> info(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "teamId")))))
                        .build(),
                "Game team command",
                List.of("gt", "gteam")
        );
    }

    private static TeamManager getTeamManager() {
        return Gutitubow.get().getGame().getTeamManager();
    }

    private static SpectatorManager getSpectatorManager() {
        return Gutitubow.get().getGame().getSpectatorManager();
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPlayers(com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(builder::suggest);
        builder.suggest("@s");
        return builder.buildFuture();
    }

    private static Player resolveTarget(Player player, String name) {
        return "@s".equals(name) ? player : Bukkit.getPlayer(name);
    }

    private static int openGui(CommandSender sender) {
        if (!(sender instanceof Player player)) return 0;
        getTeamManager().getTeamGUI().open(player);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int getTeam(CommandSender sender, String targetArg) {
        if (!(sender instanceof Player player)) return 0;
        Player target = resolveTarget(player, targetArg);
        Component message;
        if (target == null) {
            message = Component.text("ターゲットの取得中にエラーが発生しました。");
        } else {
            GameTeam gameTeam = getTeamManager().getPlayerTeam(target);
            if (gameTeam == null) {
                message = Component.text(target.getName() + "はチームに属していません");
            } else {
                message = Component.text(target.getName() + "が属するチームは").append(gameTeam.getTeamName()).append(Component.text("です"));
            }
        }
        player.sendMessage(message);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int join(CommandSender sender, String targetArg, String teamId) {
        if (!(sender instanceof Player player)) return 0;
        Player target = resolveTarget(player, targetArg);
        if (target == null) {
            player.sendMessage(Component.text("ターゲットの取得中にエラーが発生しました。"));
            return 0;
        }
        if (getSpectatorManager().isSpectator(target)) {
            player.sendMessage(Component.text("そのプレイヤーは観戦者に設定されています。\n追加するには対象のプレイヤーを観戦者から削除してください。").color(NamedTextColor.RED));
            return 0;
        }
        GameTeam gameTeam = getTeamManager().getTeam(teamId);
        if (gameTeam == null) {
            player.sendMessage(Component.text("チームの取得中にエラーが発生しました。").color(NamedTextColor.RED));
            return 0;
        }
        if (gameTeam.isFull(getTeamManager().getGame().getGameSetting().getGameMode())) {
            player.sendMessage(Component.text("チーム: ").append(gameTeam.getTeamName().color(gameTeam.getTeamColor())).append(Component.text("は満員です")));
            return 0;
        }
        getTeamManager().joinTeam(target, gameTeam);
        player.sendMessage(Component.text(target.getName() + "をチーム: ").append(gameTeam.getTeamName().color(gameTeam.getTeamColor())).append(Component.text("に追加しました")));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int leave(CommandSender sender, String targetArg) {
        if (!(sender instanceof Player player)) return 0;
        Player target = resolveTarget(player, targetArg);
        if (target == null) {
            player.sendMessage(Component.text("ターゲットの取得中にエラーが発生しました。"));
            return 0;
        }
        getTeamManager().leaveTeam(target);
        player.sendMessage(Component.text(target.getName() + "をチームから離脱させました"));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int list(CommandSender sender) {
        if (!(sender instanceof Player player)) return 0;
        TeamManager teamManager = getTeamManager();
        player.sendMessage(Component.text(teamManager.getTeams().size() + "のチームが存在しています"));
        for (GameTeam gameTeam : teamManager.getTeams()) {
            player.sendMessage(Component.text(" - ").append(gameTeam.getTeamName().color(gameTeam.getTeamColor())));
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int info(CommandSender sender, String teamId) {
        if (!(sender instanceof Player player)) return 0;
        GameTeam gameTeam = getTeamManager().getTeam(teamId);
        if (gameTeam == null) {
            player.sendMessage(Component.text("チームの取得中にエラーが発生しました。"));
            return 0;
        }
        player.sendMessage(gameTeam.getTeamName().color(gameTeam.getTeamColor()).append(Component.text("の詳細")));
        player.sendMessage(Component.text(gameTeam.getPlayers().size() + "人のプレイヤーが属しています"));
        for (UUID uuid : gameTeam.getPlayers()) {
            Player teamPlayer = Bukkit.getPlayer(uuid);
            if (teamPlayer == null) continue;
            String suffix = "";
            if (gameTeam.getJumpMaster() != null && uuid.equals(gameTeam.getJumpMaster().getUniqueId())) suffix = " (ジャンプマスター)";
            player.sendMessage(Component.text(" - " + teamPlayer.getName() + suffix));
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
