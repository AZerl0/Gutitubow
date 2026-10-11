package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.fakeplayer.FakePlayerManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class FakePlayerCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("fakeplayer")
                        .requires(source -> source.getSender().hasPermission("gutitubow.fakeplayer"))
                        .executes(ctx -> spawn(ctx.getSource().getSender(), null))
                        .then(argument("name", StringArgumentType.word())
                                .executes(ctx -> spawn(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "name"))))
                        .build(),
                "Fake player command"
        );
    }

    private static FakePlayerManager getManager() {
        return Gutitubow.get().getGutitubowManagers().getFakePlayerManager();
    }

    private static int spawn(CommandSender sender, @Nullable String name) {
        if (!(sender instanceof Player player)) return 0;
        String targetName = name == null ? player.getName() : name;
        getManager().createFakePlayer(targetName, player.getLocation());
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
