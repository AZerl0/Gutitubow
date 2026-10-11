package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.command.argument.AbilityTypeArgument;
import com.azerl0.gutitubow.gui.guis.AbilitySetGUI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class AbilityCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("ability")
                        .requires(source -> source.getSender().hasPermission("gutitubow.ability"))
                        .then(literal("list").executes(ctx -> list(ctx.getSource().getSender())))
                        .then(literal("gui").executes(ctx -> openGui(ctx.getSource().getSender(), getAbilitySelectorGUI())))
                        .then(literal("get")
                                .executes(AbilityCommand::get)
                                .then(argument("target", ArgumentTypes.player())
                                        .executes(AbilityCommand::get)))
                        .then(literal("set")
                                .then(argument("ability", new AbilityTypeArgument())
                                        .executes(AbilityCommand::set)
                                        .then(argument("targets", ArgumentTypes.players())
                                                .executes(AbilityCommand::set)
                                        )))
                        .then(literal("reset")
                                .executes(AbilityCommand::reset)
                                .then(argument("targets", ArgumentTypes.players())
                                        .executes(AbilityCommand::reset)
                                ))
                        .build(),
                "Ability command"
        );
    }

    private static AbilityManager getAbilityManager() {
        return Gutitubow.get().getGutitubowManagers().getAbilityManager();
    }

    private static AbilitySetGUI getAbilitySelectorGUI() {
        return Gutitubow.get().getAbilitySelectorGUI();
    }

    private static int list(CommandSender sender) {
        if (!(sender instanceof Player player)) return 0;
        player.sendMessage(Component.text("Abilities (" + AbilityRegistry.getSize() + "): ").color(NamedTextColor.AQUA));
        for (AbilityType type : AbilityRegistry.getTypes()) {
            player.sendMessage(Component.text(" - ").append(type.getName()).append(Component.text(" " + type.toString().toLowerCase())));
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int openGui(CommandSender sender, AbilitySetGUI gui) {
        if (!(sender instanceof Player player)) return 0;
        gui.open(player);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int get(CommandContext<CommandSourceStack> context) {
        Player target = null;
        final CommandSender sender = context.getSource().getSender();
        try {
            final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
            target = targetResolver.resolve(context.getSource()).getFirst();
        } catch (Exception e) {
            if (sender instanceof Player sendPlayer) target = sendPlayer;
        }

        if (target == null) return Command.SINGLE_SUCCESS;

        Ability ability = getAbilityManager().getAbility(target);
        if (ability == null) {
            sender.sendMessage(target.displayName().append(Component.text("は能力を持っていません")));
            return 0;
        } else {
            sender.sendMessage(target.displayName().append(Component.text(target.getName() + "の能力は").append(ability.getType().getName()).append(Component.text("です"))));

        }

        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<CommandSourceStack> context) {
        List<Player> targets = new ArrayList<>();
        final CommandSender sender = context.getSource().getSender();
        try {
            final PlayerSelectorArgumentResolver resolver = context.getArgument("targets", PlayerSelectorArgumentResolver.class);
            targets = resolver.resolve(context.getSource());
        } catch (Exception e) {
            if (sender instanceof Player sendPlayer) {
                targets.add(sendPlayer);
            }
        }
        AbilityType abilityType = context.getArgument("ability", AbilityType.class);

        for (final Player target : targets) {
            getAbilityManager().setAbility(target, abilityType);
            Component abilityTypeMessage = abilityType.getName().color(NamedTextColor.GOLD).hoverEvent(abilityType.getDisplayItem().asHoverEvent());
            sender.sendMessage(target.displayName().append(Component.text("の能力を ")).append(abilityTypeMessage).append(Component.text(" に設定しました！").color(NamedTextColor.WHITE)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        List<Player> targets = new ArrayList<>();
        final CommandSender sender = context.getSource().getSender();
        try {
            final PlayerSelectorArgumentResolver resolver = context.getArgument("targets", PlayerSelectorArgumentResolver.class);
            targets = resolver.resolve(context.getSource());
        } catch (Exception e) {
            if (sender instanceof Player sendPlayer) targets.add(sendPlayer);
        }

        AbilityManager abilityManager = getAbilityManager();
        for (final Player target : targets) {
            abilityManager.resetAbility(target);
            sender.sendMessage(target.displayName().append(Component.text("の能力を削除しました")));
        }
        return Command.SINGLE_SUCCESS;
    }
}