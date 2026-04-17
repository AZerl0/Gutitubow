package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class SetSkinCommand implements CommandExecutor, TabExecutor {
    private final PlayerSkinChanger playerSkinChanger;

    public SetSkinCommand(PlayerSkinChanger playerSkinChanger) {
        this.playerSkinChanger = playerSkinChanger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof final Player senderPlayer)) {
            sender.sendMessage("§cOnly players can execute this command!");
            return true;
        }

        if (args.length == 0) {
            senderPlayer.sendMessage("§cUsage: /skin <player> <skinPlayer>");
            return true;
        }
        Player target;
        if (args[0].equals("@s")) {
            target = senderPlayer;
        } else {
            target = Bukkit.getPlayer(args[0]);
        }

        if (args.length == 1) {
            playerSkinChanger.resetSkin(target);
        }

        String skinTarget;
        if (args[1].equals("@s")) {
            skinTarget = senderPlayer.getName();
        } else {
            skinTarget = args[1];
        }

        if (target == null) return true;


        playerSkinChanger.setSkin(target, skinTarget);


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1 || args.length == 2) return getPlayerList();
        return List.of();
    }

    private List<String> getPlayerList() {
        List<String> list = new ArrayList<String>();
        list = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        list.add("@s");
        return list;
    }

    public static void register(Commands registrar) {
        registrar.register(
                literal("setskin")
                        .requires(source -> source.getSender().hasPermission("gutitubow.setskin"))
                        .then(argument("target", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(builder::suggest);
                                    builder.suggest("@s");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> resetSkin(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "target")))
                                .then(argument("skinPlayer", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(builder::suggest);
                                            builder.suggest("@s");
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> setSkin(
                                                ctx.getSource().getSender(),
                                                StringArgumentType.getString(ctx, "target"),
                                                StringArgumentType.getString(ctx, "skinPlayer")
                                        ))))
                        .build(),
                "Set skin command"
        );
    }

    private static PlayerSkinChanger getChanger() {
        return Gutitubow.get().getPlayerSkinChanger();
    }

    private static int resetSkin(CommandSender sender, String targetArg) {
        if (!(sender instanceof Player senderPlayer)) return 0;
        Player target = "@s".equals(targetArg) ? senderPlayer : Bukkit.getPlayer(targetArg);
        if (target == null) return 0;
        getChanger().resetSkin(target);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int setSkin(CommandSender sender, String targetArg, String skinArg) {
        if (!(sender instanceof Player senderPlayer)) return 0;
        Player target = "@s".equals(targetArg) ? senderPlayer : Bukkit.getPlayer(targetArg);
        if (target == null) return 0;
        String skinTarget = "@s".equals(skinArg) ? senderPlayer.getName() : skinArg;
        getChanger().setSkin(target, skinTarget);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
