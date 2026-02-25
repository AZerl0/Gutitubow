package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.manager.CarePackageManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CarePackageCommand implements CommandExecutor, TabExecutor {
    private final CarePackageManager carePackageManager;

    public CarePackageCommand(CarePackageManager cpm) {
        carePackageManager = cpm;
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
            case ("s"):
            case ("spawn"):
                carePackageManager.summonPackage(player.getLocation());
                break;

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (args.length == 0) {
            return List.of("spawn");
        }
        return List.of();
    }
}
