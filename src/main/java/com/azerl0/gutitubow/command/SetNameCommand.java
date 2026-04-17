package com.azerl0.gutitubow.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

public class SetNameCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof final Player senderPlayer)) return true;

        if (args.length != 2) {
            senderPlayer.sendMessage("usage: /setname <player> <name>");
            return true;
        }

        Player target;
        if (args[0].equals("@s")) {
            target = senderPlayer;
        } else {
            target = Bukkit.getPlayer(args[0]);
        }

        if (target == null) return true;


        Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(args[1]);
        target.displayName(name);
        target.playerListName(name);
        target.customName(name);
        PlayerProfile profile = target.getPlayerProfile();
        target.setPlayerProfile(profile);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return getPlayerList();
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
                literal("setname")
                        .requires(source -> source.getSender().hasPermission("gutitubow.setname"))
                        .then(argument("target", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(builder::suggest);
                                    builder.suggest("@s");
                                    return builder.buildFuture();
                                })
                                .then(argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> setName(
                                                ctx.getSource().getSender(),
                                                StringArgumentType.getString(ctx, "target"),
                                                StringArgumentType.getString(ctx, "name")
                                        ))))
                        .build(),
                "Set name command"
        );
    }

    private static int setName(CommandSender sender, String targetArg, String nameArg) {
        if (!(sender instanceof Player senderPlayer)) return 0;
        Player target = "@s".equals(targetArg) ? senderPlayer : Bukkit.getPlayer(targetArg);
        if (target == null) return 0;
        Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(nameArg);
        target.displayName(name);
        target.playerListName(name);
        target.customName(name);
        PlayerProfile profile = target.getPlayerProfile();
        target.setPlayerProfile(profile);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
