package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.command.argument.AbilityTypeArgument;
import com.azerl0.gutitubow.gui.guis.AbilitySelectorGUI;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

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
                                .executes(ctx -> {
                                    if (ctx.getSource().getSender() instanceof Player sender) {
                                        get(ctx.getSource().getSender(), getAbilityManager(), sender);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("target", ArgumentTypes.player())
                                        .executes(ctx -> {
                                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                            return get(ctx.getSource().getSender(), getAbilityManager(), target);
                                        })))
                        .then(literal("set")
                                .then(argument("ability", new AbilityTypeArgument())
                                        .executes(ctx -> {
                                            if (ctx.getSource().getSender() instanceof Player sender) {
                                                set(
                                                        ctx.getSource().getSender(),
                                                        getAbilityManager(),
                                                        ctx.getArgument("ability", AbilityType.class),
                                                        sender
                                                );
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(argument("target", ArgumentTypes.player())
                                                .executes(ctx -> {
                                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                    set(
                                                            ctx.getSource().getSender(), getAbilityManager(),
                                                            ctx.getArgument("ability", AbilityType.class),
                                                            target
                                                    );
                                                    return Command.SINGLE_SUCCESS;
                                                }))))
                        .then(literal("reset")
                                .executes(ctx -> {
                                    if (ctx.getSource().getSender() instanceof Player sender) {
                                        reset(ctx.getSource().getSender(), getAbilityManager(), sender);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("target", ArgumentTypes.player())
                                        .executes(ctx -> {
                                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                    reset(
                                                            ctx.getSource().getSender(),
                                                            getAbilityManager(),
                                                            target
                                                    );
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                        )))
                        .build(),
                "Ability command"
        );
    }

    private static AbilityManager getAbilityManager() {
        return Gutitubow.get().getGame().getAbilityManager();
    }

    private static AbilitySelectorGUI getAbilitySelectorGUI() {
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

    private static int openGui(CommandSender sender, AbilitySelectorGUI gui) {
        if (!(sender instanceof Player player)) return 0;
        gui.open(player);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int get(CommandSender sender, AbilityManager abilityManager, Player target) {
        if (!(sender instanceof Player player)) return 0;
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        Ability ability = abilityManager.getAbility(target);
        if (ability == null) {
            player.sendMessage(target.getName() + "は能力を持っていません");
            return 0;
        }
        player.sendMessage(Component.text(target.getName() + "の能力は").append(ability.getType().getName()).append(Component.text("です")));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static void set(CommandSender sender, AbilityManager abilityManager, AbilityType abilityType, Player target) {
        if (!(sender instanceof Player player)) return;
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return;
        }
        if (abilityType == null) {
            player.sendMessage("その能力は存在しません");
            return;
        }
        abilityManager.setAbility(target, abilityType);
        Component abilityTypeMessage = abilityType.getName().color(NamedTextColor.GOLD).hoverEvent(abilityType.getDisplayItem().asHoverEvent());
        player.sendMessage(Component.text(target.getName() + "の能力を ").append(abilityTypeMessage).append(Component.text(" に設定しました！").color(NamedTextColor.WHITE)));
    }

    private static int reset(CommandSender sender, AbilityManager abilityManager, Player target) {
        if (!(sender instanceof Player player)) return 0;
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        abilityManager.resetAbility(target);
        player.sendMessage(target.getName() + "の能力を削除しました");
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}