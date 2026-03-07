package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.game.GamePlayerManager;
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
import java.util.stream.Collectors;

public class GamePlayerCommand implements CommandExecutor, TabExecutor {
    private final GamePlayerManager gamePlayerManager;

    public GamePlayerCommand(GamePlayerManager gamePlayerManager) {
        this.gamePlayerManager = gamePlayerManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!(sender instanceof Player player)) return true;
        Player target;
        GamePlayer gamePlayer;

        switch (args[0]) {
            case ("data"):
                if (args[2].equals("@s")) {
                    target = player;
                } else {
                    target = Bukkit.getPlayer(args[2]);
                }

                if (target == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                    return true;
                }

                switch (args[1]) {
                    case ("get"):
                        gamePlayer = this.gamePlayerManager.getGamePlayer(target);
                        switch (args[3]) {
                            case ("playerTotalHit"):
                                player.sendMessage(target.getName() + "の" + args[3] + "は" + gamePlayer.getTotalHit() + "です");
                                break;
                            case ("playerArrowReloadTick"):
                                player.sendMessage(target.getName() + "の" + args[3] + "は" + gamePlayer.getArrowReloadTick() + "です");
                                break;
                            case ("playerMaxArrowStackSize"):
                                player.sendMessage(target.getName() + "の" + args[3] + "は" + gamePlayer.getMaxArrowStackSize() + "です");
                                break;
                        }
                        return true;

                    case ("set"):
                        int value;
                        try {
                            value = Integer.parseInt(args[4]);
                        } catch (Error error) {
                            player.sendMessage(error.toString());
                            return true;
                        }

                        gamePlayer = this.gamePlayerManager.getGamePlayer(target);
                        switch (args[3]) {
                            case ("playerTotalHit"):
                                gamePlayer.setTotalHit(value);
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + value + "に設定しました");
                                break;
                            case ("playerArrowReloadTick"):
                                gamePlayer.setArrowReloadTick(value);
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + value + "に設定しました");
                                break;
                            case ("playerMaxArrowStackSize"):
                                gamePlayer.setMaxArrowStackSize(value);
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + value + "に設定しました");
                                break;

                        }
                        return true;
                    case ("reset"):
                        gamePlayer = this.gamePlayerManager.getGamePlayer(target);
                        switch (args[3]) {
                            case ("*"):
                                gamePlayer.resetTotalHit();
                                gamePlayer.resetArrowReloadTick();
                                gamePlayer.resetMaxArrowStackSize();
                                player.sendMessage(target.getName() + "のデータをリセットしました");
                                break;
                            case ("playerTotalHit"):
                                gamePlayer.resetTotalHit();
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + gamePlayer.getTotalHit() + "にリセットしました");
                                break;
                            case ("playerArrowReloadTick"):
                                gamePlayer.resetArrowReloadTick();
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + gamePlayer.getArrowReloadTick() + "にリセットしました");
                                break;
                            case ("playerMaxArrowStackSize"):
                                gamePlayer.resetMaxArrowStackSize();
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + gamePlayer.getMaxArrowStackSize() + "にリセットしました");
                                break;

                        }
                        return true;
                }
                break;
            case ("arrow"):
                boolean canSet = args[1].equals("set") && args.length == 4;
                boolean canAdd = args[1].equals("add") && args.length >= 3;
                boolean canRemove = args[1].equals("remove") && args.length >= 3;
                if (!(canSet || canAdd || canRemove)) {
                    sender.sendMessage("引数が不足しています");
                    return true;
                }
                if (args[2].equals("@s")) {
                    target = player;
                } else {
                    target = Bukkit.getPlayer(args[2]);
                }

                if (target == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                    return true;
                }
                int value;
                try {
                    if ((args[1].equals("add") || args[1].equals("remove")) && args.length != 4) {
                        value = 1;
                    } else {
                        value = Integer.parseInt(args[3]);
                    }
                } catch (Error error) {
                    player.sendMessage(error.toString());
                    return true;
                }

                gamePlayer = this.gamePlayerManager.getGamePlayer(target);
                switch (args[1]) {
                    case ("set"):
                        gamePlayer.setArrowAmount(value);
                        return true;
                    case ("add"):
                        gamePlayer.addArrow(value);
                        return true;
                    case ("remove"):
                        gamePlayer.removeArrow(value);
                        return true;
                }

        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String
            @NotNull [] args
    ) {
        List<String> list = new ArrayList<String>();
        ;
        if (args.length == 1) {
            return List.of("data", "arrow");

        } else if (args[0].equals("data")) {
            if (args.length == 2) return List.of("get", "set", "reset");
            if (args.length == 3) {
                list = Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .collect(Collectors.toCollection(ArrayList::new));
                list.add("@s");
                return list;
            }
            if (args.length == 4) {
                if (args[1].equals("reset")) {
                    return List.of("*", "playerTotalHit", "playerArrowReloadTick", "playerMaxArrowStackSize");

                } else return List.of("playerTotalHit", "playerArrowReloadTick", "playerMaxArrowStackSize");
            }

        } else if (args[0].equals("arrow")) {
            if (args.length == 2) return List.of("set","add", "remove");
            if (args.length == 3) {
                list = Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .collect(Collectors.toCollection(ArrayList::new));
                list.add("@s");
                return list;

            }

        }
        return List.of();
    }
}
