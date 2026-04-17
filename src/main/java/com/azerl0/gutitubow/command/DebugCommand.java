package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.debug.DebugManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class DebugCommand{
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
