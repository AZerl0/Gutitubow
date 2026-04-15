package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.data.damage.DamageSource;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GamePlayerCommand implements CommandExecutor, TabExecutor {
    private final JavaPlugin plugin;
    private final Game game;

    public GamePlayerCommand(JavaPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
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
            case ("data"): {
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
                        gamePlayer = game.getGamePlayer(target);
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

                        gamePlayer = game.getGamePlayer(target);
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
                        gamePlayer = game.getGamePlayer(target);
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
            }
            case ("arrow"): {
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

                gamePlayer = game.getGamePlayer(target);
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
            case ("damage"): {
                // /gp damage @s 1 border
                if (args.length < 3) {
                    sender.sendMessage("引数が不足しています");
                    return true;
                }
                if (args[1].equals("@s")) {
                    target = player;
                } else {
                    target = Bukkit.getPlayer(args[1]);
                }
                if (target == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                    return true;
                }
                double damageAmount = Double.parseDouble(args[2]);
                GutitubowDamageType damageType;
                if (args.length >= 4) damageType = GutitubowDamageType.valueOf(args[3].toUpperCase());
                else {
                    damageType = GutitubowDamageType.UNKNOWN;
                }

                long delay = 0L;
                if (args.length == 5) delay = Long.parseLong(args[4]);

                Player finalTarget = target;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    DamageSource damageSource = DamageSource.builder()
                            .damageType(damageType)
                            .build();
                    game.getGamePlayer(finalTarget).damage(finalTarget, damageAmount, damageSource);
                }, delay * 20);
                return true;
            }
            case ("revive"): {
                if (args[1].equals("@s")) {
                    target = player;
                } else {
                    target = Bukkit.getPlayer(args[1]);
                }

                if (target == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                    return true;
                }
                Player reviver;
                if (args.length < 3) {
                    reviver = player;
                } else {
                    if (args[1].equals("@s")) {
                        reviver = player;
                    } else {
                        reviver = Bukkit.getPlayer(args[1]);
                    }

                    if (reviver == null) {
                        player.sendMessage("プレイヤーが見つかりませんでした");
                        return true;
                    }
                }

                GamePlayer gamePlayerTarget = game.getGamePlayer(target);

                gamePlayerTarget.revive(reviver);
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

        if (args.length == 1) {
            return List.of("data", "arrow", "damage", "revive");
        }

        switch (args[0]) {
            case ("data"): {
                if (args.length == 2) return List.of("get", "set", "reset");
                if (args.length == 3) {
                    return getPlayerList();
                }
                if (args.length == 4) {
                    if (args[1].equals("reset")) {
                        return List.of("*", "playerTotalHit", "playerArrowReloadTick", "playerMaxArrowStackSize");
                    } else return List.of("playerTotalHit", "playerArrowReloadTick", "playerMaxArrowStackSize");
                }
            }
            case ("arrow"): {
                if (args.length == 2) return List.of("set", "add", "remove");
                if (args.length == 3) {
                    return getPlayerList();

                }
            }
            case ("damage"): {
                switch (args.length) {
                    case (2):
                        return getPlayerList();
                    case (3):
                        return List.of();
                    case (4):
                        List<String> damageTypeList = new ArrayList<>();
                        for (GutitubowDamageType damageType : GutitubowDamageType.values()) {
                            damageTypeList.add(damageType.toString().toLowerCase());
                        }
                        return damageTypeList;
                }
            }
            case ("revive"): {
                if (args.length == 2 || args.length == 3) {
                    return getPlayerList();
                }
            }
        }

        return List.of();
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
