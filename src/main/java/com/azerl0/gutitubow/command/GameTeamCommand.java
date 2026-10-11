package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.game.SpectatorManager;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.team.TeamManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GameTeamCommand {

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
        for (GamePlayer gamePlayer : gameTeam.getPlayers()) {
            Player teamPlayer = gamePlayer.getPlayer();
            if (teamPlayer == null) continue;
            String suffix = "";
            if (gameTeam.isThisTeamJumpMaster(player)) suffix = " (ジャンプマスター)";
            player.sendMessage(Component.text(" - " + teamPlayer.getName() + suffix));
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
