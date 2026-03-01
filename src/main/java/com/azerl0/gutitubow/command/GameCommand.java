package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.manager.GameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GameCommand implements CommandExecutor, TabExecutor {
    private final GameManager.Game game;

    public GameCommand(GameManager.Game game) {
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

        switch (args[0]) {
            case ("start"):
                if (!this.game.isStarted()) {
                    this.game.start();
                    return true;
                }
                break;
            case ("reset"):
                this.game.reset();
                sender.sendMessage(Component.text("ゲームをリセットしました"));
                return true;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (args.length == 1) {
            return List.of("start", "reset");
        }
        return List.of();
    }
}
