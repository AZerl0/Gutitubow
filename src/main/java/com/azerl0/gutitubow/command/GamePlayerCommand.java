package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.manager.GamePlayerManager;
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

        switch (args[0]) {
            case ("data"):
                Player target;
                GamePlayerManager.GamePlayer gamePlayer;
                switch (args[1]) {
                    case ("get"):
                        if (args[2].equals("@s")) {
                            target = player;
                        } else {
                            target = Bukkit.getPlayer(args[2]);
                        }

                        if (target == null) {
                            player.sendMessage("プレイヤーが見つかりませんでした");
                            return true;
                        }


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
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + value + "に設定しました");
                                gamePlayer.setArrowReloadTick(value);
                                break;
                            case ("playerMaxArrowStackSize"):
                                player.sendMessage(target.getName() + "の" + args[3] + "を" + value + "に設定しました");
                                gamePlayer.setMaxArrowStackSize(value);
                                break;

                        }
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
        List<String> list = new ArrayList<String>();;
        if (args.length == 1) {
            return List.of("data");

        } else if (args[0].equals("data")) {
            if (args.length == 2) return List.of("get", "set");
            if (args.length == 3) {
                list =  Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .collect(Collectors.toCollection(ArrayList::new));
                list.add("@s");
                return list;
            }
            if (args.length == 4) return List.of("playerTotalHit", "playerArrowReloadTick", "playerMaxArrowStackSize");
        }

        return List.of();
    }
}
