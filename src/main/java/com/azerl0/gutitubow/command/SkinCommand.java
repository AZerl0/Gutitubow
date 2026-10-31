package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.GutitubowManagers;
import com.azerl0.gutitubow.profiler.Profile;
import com.azerl0.gutitubow.profiler.ProfileChangeReason;
import com.azerl0.gutitubow.profiler.SkinData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class SkinCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("skin")
                        .requires(source -> source.getSender().hasPermission("gutitubow.skin"))
                        .then(literal("set")
                                .then(argument("target", ArgumentTypes.player())
                                        .then(argument("skinName", StringArgumentType.word())
                                                .executes(SkinCommand::setSkin))))
                        .then(literal("reset")
                                .then(argument("target", ArgumentTypes.player())
                                        .executes(SkinCommand::resetSkin)))
                        .then(literal("cache")
                                .then(literal("clear")
                                        .then(argument("playerName", StringArgumentType.word())
                                                .executes(SkinCommand::clearCache))))
                        .build(),
                "Skin command"
        );
    }

    private static GutitubowManagers gutitubowManagers() {
        return Gutitubow.get().getGutitubowManagers();
    }

    private static int setSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        Player target = targetResolver.resolve(context.getSource()).getFirst();

        Profile profile = gutitubowManagers().getProfileManager().getProfile(target);
        String skinName = context.getArgument("skinName", String.class);
        SkinData skinData = gutitubowManagers().getSkinDataManager().getSkinData(skinName);
        if (skinData != null) profile.setSkin(skinData, ProfileChangeReason.COMMAND);

        return Command.SINGLE_SUCCESS;
    }

    private static int resetSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        Player target = targetResolver.resolve(context.getSource()).getFirst();

        Profile profile = gutitubowManagers().getProfileManager().getProfile(target);
        profile.resetSkin(ProfileChangeReason.COMMAND);

        return Command.SINGLE_SUCCESS;
    }

    private static int clearCache(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = context.getArgument("playerName", String.class);

        gutitubowManagers().getSkinDataManager().clearCache(name);

        return Command.SINGLE_SUCCESS;
    }
}
