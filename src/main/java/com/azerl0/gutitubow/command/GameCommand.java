package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.game.GameManager;
import com.azerl0.gutitubow.game.GameStartResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
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
                        .then(literal("checkgameover").executes(GameCommand::checkGameOver))
                        .then(literal("gui").executes(GameCommand::gui))
                        .then(literal("resetarena").executes(GameCommand::resetArena))
                        .build(),
                "Gutitubow game command"
        );
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        getGame().reset();
        sender.sendMessage(Component.text("ゲームをリセットしました"));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        GameStartResult result = getGame().start();
        if (result == GameStartResult.MAP_IS_NULL) {
            sender.sendMessage("マップが設定されていません");
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int checkGameOver(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        getGame().checkGameOver();
        sender.sendMessage(Component.text("ゲームオーバー状態をを更新しました"));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }


    private static int gui(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player senderPlayer) {
            Gutitubow.get().getGame().getGameGUI().open(senderPlayer);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetArena(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        getGame().resetArena();
        sender.sendMessage(Component.text("アリーナをリセットしました"));
        return Command.SINGLE_SUCCESS;
    }

    private static GameManager getGameManager() {
        return Gutitubow.get().getGutitubowManagers().gameManager();
    }

    private static Game getGame() {
        return Gutitubow.get().getGame();
    }

}
