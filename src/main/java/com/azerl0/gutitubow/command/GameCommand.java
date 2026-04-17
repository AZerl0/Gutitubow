package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GameManager;
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
                        .then(literal("start").executes(ctx -> start(ctx.getSource().getSender(), getGameManager(), getGame())))
                        .then(literal("reset").executes(ctx -> reset(ctx.getSource().getSender(), getGameManager(), getGame())))
                        .build(),
                "Gutitubow game command"
        );
    }

    private static GameManager getGameManager() {
        return Gutitubow.get().getGameManager();
    }

    private static Game getGame() {
        return Gutitubow.get().getGame();
    }

    private static int start(CommandSender sender, GameManager gameManager, Game game) {
        if (!(sender instanceof Player)) return 0;
        if (!gameManager.isStarted(game)) {
            gameManager.startGame(game);
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int reset(CommandSender sender, GameManager gameManager, Game game) {
        if (!(sender instanceof Player)) return 0;
        gameManager.resetGame(game);
        sender.sendMessage(Component.text("ゲームをリセットしました"));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
