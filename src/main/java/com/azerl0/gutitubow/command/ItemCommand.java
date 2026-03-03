package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!(sender instanceof Player player)) return true;

        if (args[0].equals("gutitubow")) {
            player.give(GutitubowItem.GUTITUBOW);
            return true;
        }
        return false;
    }
}
