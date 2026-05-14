package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.command.argument.GameEventControllerGameEventTypeArgument;
import com.azerl0.gutitubow.command.argument.GameEventTypeArgument;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventController;
import com.azerl0.gutitubow.gameevent.GameEventControllerGameEventType;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GameEventCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("gameevent").requires(source -> source.getSender().hasPermission("gutitubow.gameevent"))
                        .then(literal("get")
                                .then(argument("controller_gameevent_type", new GameEventControllerGameEventTypeArgument())
                                        .executes(ctx -> getEvent(
                                                ctx.getSource().getSender(),
                                                ctx.getArgument("controller_gameevent_type", GameEventControllerGameEventType.class)
                                        ))))
                        .then(literal("activate")
                                .then(argument("controller_gameevent_type", new GameEventControllerGameEventTypeArgument())
                                        .executes(ctx -> activateEvent(
                                                ctx.getSource().getSender(),
                                                ctx.getArgument("controller_gameevent_type", GameEventControllerGameEventType.class)
                                        ))))
                        .then(literal("set")
                                .then(argument("controller_gameevent_type", new GameEventControllerGameEventTypeArgument())
                                        .then(argument("event_type", new GameEventTypeArgument())
                                                .executes(ctx -> setEvent(
                                                        ctx.getSource().getSender(),
                                                        ctx.getArgument("controller_gameevent_type", GameEventControllerGameEventType.class),
                                                        ctx.getArgument("event_type", GameEventType.class)
                                                )))))
                        .then(literal("deactivate")
                                .then(argument("controller_gameevent_type", new GameEventControllerGameEventTypeArgument()))
                                .executes(ctx -> deactivateEvent(
                                        ctx.getSource().getSender(),
                                        ctx.getArgument("controller_gameevent_type", GameEventControllerGameEventType.class)
                                )))
                        .then(literal("gui").executes(ctx -> openGUI(ctx.getSource().getSender())))
                        .build(),
                List.of("ge", "event")
        );
    }

    private static GameEventController getEventController() {
        return Gutitubow.get().getGame().getGameSetting().getGameEventController();
    }

    private static int getEvent(CommandSender sender, GameEventControllerGameEventType controllerGameEventType) {
        if (sender instanceof Player player) {
            GameEvent event = getEventController().getEvent(controllerGameEventType);
            Component message = Component.empty();
            if (event == null) {
                message = message.append(Component.text(controllerGameEventType + "のイベントは設定されていません"));
            } else {
                GameEventType eventType = event.getEventType();
                message = message.append(Component.text(controllerGameEventType + "のイベントは").append(eventType.getName().color(NamedTextColor.AQUA)).append(Component.text("に設定されています").color(NamedTextColor.WHITE)));
                message = message.append(Component.text("\n状態: ")).append(
                        event.isActivated() ?
                                Component.text("起動中").color(NamedTextColor.GREEN) :
                                Component.text("停止中").color(NamedTextColor.RED)
                );
            }
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setEvent(CommandSender sender, GameEventControllerGameEventType controllerGameEventType, GameEventType eventType) {
        getEventController().setEvent(controllerGameEventType, eventType);
        if (sender instanceof Player player) {
            Component message = Component.text(controllerGameEventType.toString() + "のイベントを").append(eventType.getName()).append(Component.text("に設定しました"));
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int activateEvent(CommandSender sender, GameEventControllerGameEventType controllerGameEventType) {
        getEventController().activateEvent(controllerGameEventType);
        if (sender instanceof Player player) {
            Component message = Component.text(controllerGameEventType + "を起動しました");
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deactivateEvent(CommandSender sender, GameEventControllerGameEventType controllerGameEventType) {
        getEventController().deactivateEvent(controllerGameEventType);
        if (sender instanceof Player player) {
            Component message = Component.text("現在のイベントを終了しました");
            player.sendMessage(message);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int openGUI(CommandSender sender) {
        if (sender instanceof Player player) {
            getEventController().openGameEventGUI(player);
        }
        return Command.SINGLE_SUCCESS;
    }
}
