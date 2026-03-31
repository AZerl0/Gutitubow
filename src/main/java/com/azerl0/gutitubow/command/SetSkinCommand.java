package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.player.PlayerSkinChanger;
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

public class SetSkinCommand implements CommandExecutor, TabExecutor {
    private final PlayerSkinChanger playerSkinChanger;

    public SetSkinCommand(PlayerSkinChanger playerSkinChanger) {
        this.playerSkinChanger = playerSkinChanger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof final Player senderPlayer)) {
            sender.sendMessage("§cOnly players can execute this command!");
            return true;
        }

        if (args.length == 0) {
            senderPlayer.sendMessage("§cUsage: /skin <player> <skinPlayer>");
            return true;
        }
        Player target;
        if (args[0].equals("@s")) {
            target = senderPlayer;
        } else {
            target = Bukkit.getPlayer(args[0]);
        }

        if (args.length == 1) {
            playerSkinChanger.resetSkin(target);
        }

        String skinTarget;
        if (args[1].equals("@s")) {
            skinTarget = senderPlayer.getName();
        } else {
            skinTarget = args[1];
        }

        if (target == null) return true;


        playerSkinChanger.setSkin(target, skinTarget);


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1 || args.length == 2) return getPlayerList();
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
