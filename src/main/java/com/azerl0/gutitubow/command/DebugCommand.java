package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.debug.DebugManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugCommand implements CommandExecutor, TabExecutor {
    private final DebugManager debugManager;

    public DebugCommand(DebugManager debugManager) {
        this.debugManager = debugManager;
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
            case ("quick_phase"):
                if (args.length <= 1) {
                    player.sendMessage(Component.text(args[0] + "は" + this.debugManager.isQuickPhase() + "に設定されています"));

                } else {
                    boolean value;
                    try {
                        value = Boolean.parseBoolean(args[1]);
                    } catch (Error error) {
                        player.sendMessage(error.toString());
                        return true;
                    }
                    this.debugManager.setQuickPhase(value);
                    player.sendMessage(Component.text(args[0] + "が" + args[1] + "に設定されました"));
                }
                return true;

            case ("no_start_teleport"):
                if (args.length <= 1) {
                    player.sendMessage(Component.text(args[0] + "は" + this.debugManager.isNoStartTeleport() + "に設定されています"));

                } else {
                    boolean value;
                    try {
                        value = Boolean.parseBoolean(args[1]);
                    } catch (Error error) {
                        player.sendMessage(error.toString());
                        return true;
                    }
                    this.debugManager.setNoStartTeleport(value);
                    player.sendMessage(Component.text(args[0] + "が" + args[1] + "に設定されました"));
                }
                return true;
            case ("disable_check_game_over"):
                if (args.length <= 1) {
                    player.sendMessage(Component.text(args[0] + "は" + this.debugManager.isDisableCheckGameOver() + "に設定されています"));

                } else {
                    boolean value;
                    try {
                        value = Boolean.parseBoolean(args[1]);
                    } catch (Error error) {
                        player.sendMessage(error.toString());
                        return true;
                    }
                    this.debugManager.setDisableCheckGameOver(value);
                    player.sendMessage(Component.text(args[0] + "が" + args[1] + "に設定されました"));
                }
                return true;
            case ("disable_solo_mode_pin"):
                if (args.length <= 1) {
                    player.sendMessage(Component.text(args[0] + "は" + this.debugManager.isDisableSoloModePin() + "に設定されています"));

                } else {
                    boolean value;
                    try {
                        value = Boolean.parseBoolean(args[1]);
                    } catch (Error error) {
                        player.sendMessage(error.toString());
                        return true;
                    }
                    this.debugManager.setDisableSoloModePin(value);
                    player.sendMessage(Component.text(args[0] + "が" + args[1] + "に設定されました"));
                }
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
        if (args.length == 1) return List.of("quick_phase", "no_start_teleport", "disable_check_game_over", "disable_solo_mode_pin");
        if (args.length == 2) return List.of("true", "false");
        return List.of();
    }
}
