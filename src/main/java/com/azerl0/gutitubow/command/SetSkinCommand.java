package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class SetSkinCommand {

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
