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
        switch (args[0]) {
            case ("gutitubow"):
                player.give(GutitubowItem.GUTITUBOW);
                break;
            case ("gamemaster"):
                player.give(GutitubowItem.GAME_MASTER);
                break;
            case ("recovery_potion"):
                player.give(GutitubowItem.RECOVERY_POTION);
                break;
            case ("respawn_beacon"):
                player.give(GutitubowItem.RESPAWN_BEACON);
                break;
        }

        return true;
    }
}
