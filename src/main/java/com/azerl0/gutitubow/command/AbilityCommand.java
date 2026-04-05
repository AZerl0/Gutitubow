package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.gui.guis.AbilitySelectorGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilityCommand implements CommandExecutor, TabExecutor {
    AbilityManager abilityManager;
    AbilitySelectorGUI abilitySelectorGUI;

    public AbilityCommand(AbilityManager abilityManager, AbilitySelectorGUI abilitySelectorGUI) {
        this.abilityManager = abilityManager;
        this.abilitySelectorGUI = abilitySelectorGUI;
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
                    Ability ability = abilityManager.getAbility(getPlayer);
                    if (ability == null) {
                        player.sendMessage(getPlayer.getName() + "は能力を持っていません");
                    } else {
                        player.sendMessage(Component.text(getPlayer.getName() + "の能力は").append(ability.getType().getName()).append(Component.text("です")));
                    }

                }
                return true;


            case ("list"):
                player.sendMessage(Component.text("Abilities (" + AbilityRegistry.getSize() + "): ").color(NamedTextColor.AQUA));

                for (AbilityType type : AbilityRegistry.getTypes()) {
                    player.sendMessage(Component.text(" - ").append(type.getName()).append(Component.text(" " + type.toString().toLowerCase())));
                }

                return true;

            case ("gui"):
                abilitySelectorGUI.open(player);
                return true;

            case ("set"):

                String id = args[1];
                Player setPlayer;
                if (args.length == 2) {
                    setPlayer = player;
                } else {
                    setPlayer = Bukkit.getPlayer(args[2]);
                }

                if (id == null) {
                    player.sendMessage("能力を指定する必要があります");
                    return true;
                }

                AbilityType type = AbilityRegistry.getType(id);

                if (type == null) {
                    player.sendMessage("その能力は存在しません");
                    return true;
                }

                if (setPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {

                    abilityManager.setAbility(setPlayer, type);
                    Component abilityTypeMessage = type.getName().color(NamedTextColor.GOLD).hoverEvent(type.getDisplayItem().asHoverEvent());
                    player.sendMessage(Component.text(setPlayer.getName() + "の能力を ").append(abilityTypeMessage).append(Component.text(" に設定しました！").color(NamedTextColor.WHITE)));
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
            return List.of("list", "get", "set", "reset", "gui");
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
                    List<AbilityType> types = AbilityRegistry.getTypes().stream().toList();
                    List<String> typesList = new ArrayList<>();
                    for (AbilityType type : types) {
                        typesList.add(type.toString().toLowerCase());
                    }
                    return typesList;
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