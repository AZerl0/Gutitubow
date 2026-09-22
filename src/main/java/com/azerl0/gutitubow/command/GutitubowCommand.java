package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class GutitubowCommand {
    public static void register(Commands registrar) {
        registrar.register(
                literal("gutitubow")
                        .requires(source -> source.getSender().hasPermission("gutitubow.gutitubow"))
                        .then(literal("reloadconfig").executes(GutitubowCommand::reloadConfig))
                        .build(),
                "Gutitubow command"
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        Gutitubow.get().getConfigManager().reload();
        return Command.SINGLE_SUCCESS;
    }
}
