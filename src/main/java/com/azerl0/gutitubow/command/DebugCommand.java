package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.debug.DebugManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

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

    public static void register(Commands registrar) {
        registrar.register(
                literal("gutitubowdebug")
                        .requires(source -> source.getSender().hasPermission("gutitubow.gutitubowdebug"))
                        .then(debugToggle("quick_phase", () -> getDebugManager().isQuickPhase(), value -> getDebugManager().setQuickPhase(value)))
                        .then(debugToggle("no_start_teleport", () -> getDebugManager().isNoStartTeleport(), value -> getDebugManager().setNoStartTeleport(value)))
                        .then(debugToggle("disable_check_game_over", () -> getDebugManager().isDisableCheckGameOver(), value -> getDebugManager().setDisableCheckGameOver(value)))
                        .then(debugToggle("disable_solo_mode_pin", () -> getDebugManager().isDisableSoloModePin(), value -> getDebugManager().setDisableSoloModePin(value)))
                        .build(),
                "Gutitubow debug command",
                List.of("gdebug")
        );
    }

    private static DebugManager getDebugManager() {
        return Gutitubow.get().getDebugManager();
    }

    private interface BoolGetter {
        boolean get();
    }

    private interface BoolSetter {
        void set(boolean value);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> debugToggle(
            String key,
            BoolGetter getter,
            BoolSetter setter
    ) {
        return literal(key)
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 0;
                    player.sendMessage(Component.text(key + "は" + getter.get() + "に設定されています"));
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
                .then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return 0;
                            boolean value = BoolArgumentType.getBool(ctx, "value");
                            setter.set(value);
                            player.sendMessage(Component.text(key + "が" + value + "に設定されました"));
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        }));
    }
}
