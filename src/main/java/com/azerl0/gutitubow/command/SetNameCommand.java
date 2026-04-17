package com.azerl0.gutitubow.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class SetNameCommand {

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
