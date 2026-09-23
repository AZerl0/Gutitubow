package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.profiler.Profile;
import com.azerl0.gutitubow.profiler.ProfileChangeReason;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class NameCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("name")
                        .requires(source -> source.getSender().hasPermission("gutitubow.name"))
                        .then(literal("set").then(argument("targets", ArgumentTypes.players())
                                .then(argument("name", StringArgumentType.word())
                                        .executes(NameCommand::setName))))
                        .then(literal("reset").then(argument("targets", ArgumentTypes.players())
                                .executes(NameCommand::resetName)))
                        .build(),
                "Name command"
        );
    }

    private static Profile getProfile(Player player) {
        return Gutitubow.get().getGutitubowManagers().getProfileManager().getProfile(player);
    }

    private static int setName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = context.getArgument("targets", PlayerSelectorArgumentResolver.class);
        List<Player> targets = resolver.resolve(context.getSource());
        String nameArg = context.getArgument("name", String.class);
        Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(nameArg);
        for (Player player : targets) {
            getProfile(player).setName(name, ProfileChangeReason.COMMAND);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = context.getArgument("targets", PlayerSelectorArgumentResolver.class);
        List<Player> targets = resolver.resolve(context.getSource());
        for (Player player : targets) {
            getProfile(player).resetName(ProfileChangeReason.COMMAND);
        }
        return Command.SINGLE_SUCCESS;
    }
}
