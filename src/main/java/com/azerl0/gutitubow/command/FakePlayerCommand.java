package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FakePlayerCommand implements CommandExecutor, TabExecutor {
    private final FakePlayerManager fakePlayerManager;

    public FakePlayerCommand(FakePlayerManager fakePlayerManager) {
        this.fakePlayerManager = fakePlayerManager;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return false;

        // No args given -> We spawn command executor as fake player
        if (args.length < 1) {
            fakePlayerManager.createFakePlayer(player.getName(), player.getLocation());
            return true;
        }
        final String targetName = args[0];


        fakePlayerManager.createFakePlayer(targetName, player.getLocation());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
