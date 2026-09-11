package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.player.PlayerSkinChanger;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class SkinChangerCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("skinchanger")
                        .requires(source -> source.getSender().hasPermission("gutitubow.skinchanger"))
                        .then(literal("cachelist").executes(SkinChangerCommand::cacheList))
                        .then(literal("cache")
                                .then(literal("players").then(argument("targets", ArgumentTypes.players()).executes(SkinChangerCommand::cachePlayers)))
                                .then(literal("name").then(argument("targetName", StringArgumentType.word()).executes(SkinChangerCommand::cachePlayerName)))
                        )
                        .build(),
                "Set skin command"
        );
    }

    private static PlayerSkinChanger getSkinChanger() {
        return Gutitubow.get().getPlayerSkinChanger();
    }

    private static int cacheList(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder("CachedPlayers:");
        for (String cachedName : getSkinChanger().getCachePlayers()) {
            message.append("\n- ").append(cachedName);
        }
        context.getSource().getSender().sendMessage(message.toString());
        return Command.SINGLE_SUCCESS;
    }


    private static int cachePlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("targets", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = targetResolver.resolve(context.getSource());
        final PlayerSkinChanger skinChanger = getSkinChanger();
        for (Player target : targets) {
            skinChanger.cacheSkin(target);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int cachePlayerName(CommandContext<CommandSourceStack> context) {
        String playerName = context.getArgument("targetName", String.class);

        getSkinChanger().cacheSkin(playerName);
        return Command.SINGLE_SUCCESS;
    }
}
