package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.ability.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AbilityCommand implements CommandExecutor, TabExecutor {
    AbilityManager abilityManager;

    public AbilityCommand(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) return true;

        switch (args[0]) {
            case ("get"):
                Player getPlayer;
                if (args.length == 1) {
                    getPlayer = player;
                } else {
                    getPlayer = Bukkit.getPlayer(args[1]);
                }

                if (getPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {
                    AbilityType type = abilityManager.getAbilityType(getPlayer);
                    if (type == null) {
                        player.sendMessage(getPlayer.getName() + "は能力を持っていません");
                    } else {
                        player.sendMessage(Component.text(getPlayer.getName() + "の能力は").append(type.getName()).append(Component.text("です")));
                    }

                }
                return true;


            case ("list"):
                player.sendMessage("Abilities:");

                for (String id : AbilityRegistry.getAllIds()) {
                    player.sendMessage("- " + id);
                }

                return true;

            case ("set"):

                AbilityType id = AbilityType.valueOf(args[1].toUpperCase());
                Player setPlayer;
                if (args.length == 2) {
                    setPlayer = player;
                } else {
                    setPlayer = Bukkit.getPlayer(args[2]);
                }

                if (id == null) {
                    player.sendMessage("Abilityを指定する必要があります");
                    return true;
                }

                if (AbilityRegistry.get(id) == null) {
                    player.sendMessage("そのAbilityは存在しません");
                    return true;
                }

                if (setPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {

                    abilityManager.setAbility(player, id);
                    player.sendMessage(Component.text(setPlayer.getName() + "のAbilityを ").append(id.getName()).append(Component.text(" に設定しました！")));
                }
                return true;

            case ("reset"):
                Player resetPlayer;
                if (args.length == 1) {
                     resetPlayer = player;
                } else {
                     resetPlayer = Bukkit.getPlayer(args[1]);
                }

                if (resetPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {
                    abilityManager.resetAbility(resetPlayer);
                    player.sendMessage(resetPlayer.getName() + "の能力を削除しました");
                }
                return true;


        }

        return false;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args
    ) {
        if (args.length == 1) {
            return List.of("list", "get", "set", "reset");
        }


        switch (args[0]) {
            case ("get"):
            case ("reset"):
                if (args.length == 2) {
                    return Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList();
                }
                break;

            case ("set"):
                if (args.length == 2) {
                    return AbilityRegistry.getAllIds().stream().toList();
                } else if (args.length == 3) {
                    return Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList();
                }
                break;
        }
        return Collections.emptyList();
    }
}