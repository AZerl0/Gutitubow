package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventController;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class GameEventCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("gameevent").requires(source -> source.getSender().hasPermission("gutitubow.gameevent"))
                        .then(literal("getnext").executes(ctx -> getNext(ctx.getSource().getSender())))
                        .then(literal("activate").executes(ctx -> activateNext(ctx.getSource().getSender())))
                        .then(literal("setnext").executes(ctx -> setNext(ctx.getSource().getSender(), GameEventType.RICOCHET)))
                        .then(literal("deactivate").executes(ctx -> deactivateCurrent(ctx.getSource().getSender())))
                        .build(),
                List.of("ge", "event")
        );
    }

    private static GameEventController getEventController() {
        return Gutitubow.get().getGame().getGameSetting().getGameEventController();
    }

    private static int getNext(CommandSender sender) {
        if (sender instanceof Player player) {
            GameEvent nextEvent = getEventController().getNextEvent();
            Component message;
            if (nextEvent == null) {
                message = Component.text("次のイベントはありません");
            } else {
                message = Component.text("次のイベントは\"" + nextEvent.getEventType().toString().toUpperCase() + "\"です");
            }
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setNext(CommandSender sender, GameEventType eventType) {
        getEventController().setNextEvent(eventType);
        if (sender instanceof Player player) {
            Component message = Component.text("次のイベントを").append(eventType.getName()).append(Component.text("に設定しました"));
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int activateNext(CommandSender sender) {
        if (sender instanceof Player player) {
            Component message;
            if (getEventController().hasNextEvent()) {
                getEventController().activateNextEvent();
                message = Component.text("イベントを発生させました。");
            } else {
                message = Component.text("イベントがありません。");
            }
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deactivateCurrent(CommandSender sender) {
        getEventController().deactivateCurrentEvent();
        if (sender instanceof Player player) {
            Component message = Component.text("現在のイベントを終了しました");
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }
}
