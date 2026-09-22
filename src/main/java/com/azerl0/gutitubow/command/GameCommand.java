package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GameManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class GameCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("game")
                        .requires(source -> source.getSender().hasPermission("gutitubow.game"))
                        .then(literal("start").executes(GameCommand::start))
                        .then(literal("reset").executes(GameCommand::reset))
                        .then(literal("gui").executes(GameCommand::gui))
                        .then(literal("resetarena").executes(GameCommand::resetArena))
                        .build(),
                "Gutitubow game command"
        );
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        getGameManager().resetGame(getGame());
        sender.sendMessage(Component.text("ゲームをリセットしました"));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        getGameManager().startGame(getGame());
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int gui(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player senderPlayer) {
            Gutitubow.get().getAllGUI().gameGUI().open(senderPlayer);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetArena(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        getGame().resetArena();
        return Command.SINGLE_SUCCESS;
    }

    private static GameManager getGameManager() {
        return Gutitubow.get().getGameManager();
    }

    private static Game getGame() {
        return Gutitubow.get().getGame();
    }

}
