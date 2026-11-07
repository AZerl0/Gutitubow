package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.gamemap.GameMap;
import com.azerl0.gutitubow.gamemap.GameMapManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class MapCommand {
    public static void register(Commands registrar) {
        registrar.register(
                literal("map")
                        .requires(source -> source.getSender().hasPermission("gutitubow.map"))
                        .then(literal("list").executes(MapCommand::list))
                        .then(literal("create")
                                .then(argument("mapId", StringArgumentType.word())
                                        .executes(MapCommand::create))
                        )
                        .then(literal("delete")
                                .then(argument("mapId", StringArgumentType.word())
                                        .executes(MapCommand::delete))
                        )
                        .then(literal("info")
                                .then(argument("mapId", StringArgumentType.word())
                                        .executes(MapCommand::info)))
                        .then(literal("setlobby")
                                .then(argument("mapId", StringArgumentType.word())
                                        .executes(MapCommand::setLobby)))
                        .then(literal("reload").executes(MapCommand::reload))
                        .build(),
                "Gutitubow map command"
        );
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage("Maps(" + getMapManager().getGameMaps().size() + "): ");
        for (GameMap gameMap : getMapManager().getGameMaps()) {
            sender.sendMessage(" - " + gameMap.getId());
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int create(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getSender() instanceof Player senderPlayer) {
            String id = context.getArgument("mapId", String.class);
            Location location = senderPlayer.getLocation().clone();
            location.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            getMapManager().createMap(id, location);
            senderPlayer.sendMessage("Map: " + id + " を作成しました");
        } else {
            context.getSource().getSender().sendMessage("このコマンドはプレイヤーからしか実行できません");
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        String id = context.getArgument("mapId", String.class);
        if (getMapManager().containsMapId(id)) {
            getMapManager().deleteMap(id);
            sender.sendMessage("Map: " + id + " を削除しました");
        } else {
            sender.sendMessage("Map: " + id + " が存在しません");
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        String id = context.getArgument("mapId", String.class);
        GameMap gameMap = getMapManager().getMap(id);
        String message;
        if (gameMap != null) {
            Location center = gameMap.getCenter();
            message = "Map: " + id;
            message = message + "\n- Center:\n  - World: " + center.getWorld().getName() + "\n  - Pos: [" + center.getX() + ", " + center.getY() + ", " + center.getZ() + "]";
            message = message + "\n- Size: " + gameMap.getSize();
            message = message + "\n- SpawnHeight: " + gameMap.getSpawnHeight();
        } else {
            message = "Map: " + id + " が存在しません";
        }
        sender.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    private static int setLobby(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player sendPlayer)) return Command.SINGLE_SUCCESS;
        String id = context.getArgument("mapId", String.class);
        GameMap gameMap = getMapManager().getMap(id);
        String message;
        if (gameMap != null) {
            Location lobby = sendPlayer.getLocation();
            gameMap.setLobby(lobby);
            GameMapManager gameMapManager = getMapManager();
            gameMapManager.deleteMap(gameMap.getId());
            gameMapManager.addMap(gameMap);
            gameMapManager.saveMaps();
            message = "Map: " + id + "のロビーを";
            message = message + "\nWorld: " + lobby.getWorld().getName() + "\n  - Pos: [" + lobby.getX() + ", " + lobby.getY() + ", " + lobby.getZ() + "]";
            message = message + "\nに設定しました";
        } else {
            message = "Map: " + id + " が存在しません";
        }
        sender.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        Gutitubow.get().getConfigManager().reload();
        context.getSource().getSender().sendMessage("Reload maps");
        return Command.SINGLE_SUCCESS;
    }

    private static GameMapManager getMapManager() {
        return Gutitubow.get().getGutitubowManagers().getGameMapManager();
    }
}