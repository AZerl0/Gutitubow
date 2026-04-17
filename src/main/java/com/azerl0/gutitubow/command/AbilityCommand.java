package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.gui.guis.AbilitySelectorGUI;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

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
                                .executes(ctx -> get(ctx.getSource().getSender(), getAbilityManager(), "@s"))
                                .then(argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestPlayers(builder))
                                        .executes(ctx -> get(ctx.getSource().getSender(), getAbilityManager(), StringArgumentType.getString(ctx, "player")))))
                        .then(literal("set")
                                .then(argument("ability", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            AbilityRegistry.getTypes().forEach(type -> builder.suggest(type.toString().toLowerCase()));
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> set(ctx.getSource().getSender(), getAbilityManager(), StringArgumentType.getString(ctx, "ability"), "@s"))
                                        .then(argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> suggestPlayers(builder))
                                                .executes(ctx -> set(ctx.getSource().getSender(), getAbilityManager(), StringArgumentType.getString(ctx, "ability"), StringArgumentType.getString(ctx, "player"))))))
                        .then(literal("reset")
                                .executes(ctx -> reset(ctx.getSource().getSender(), getAbilityManager(), "@s"))
                                .then(argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestPlayers(builder))
                                        .executes(ctx -> reset(ctx.getSource().getSender(), getAbilityManager(), StringArgumentType.getString(ctx, "player")))))
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

    private static int get(CommandSender sender, AbilityManager abilityManager, String targetName) {
        if (!(sender instanceof Player player)) return 0;
        Player target = "@s".equals(targetName) ? player : Bukkit.getPlayer(targetName);
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

    private static int set(CommandSender sender, AbilityManager abilityManager, String id, String targetName) {
        if (!(sender instanceof Player player)) return 0;
        Player target = "@s".equals(targetName) ? player : Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        AbilityType type = AbilityRegistry.getType(id);
        if (type == null) {
            player.sendMessage("その能力は存在しません");
            return 0;
        }
        abilityManager.setAbility(target, type);
        Component abilityTypeMessage = type.getName().color(NamedTextColor.GOLD).hoverEvent(type.getDisplayItem().asHoverEvent());
        player.sendMessage(Component.text(target.getName() + "の能力を ").append(abilityTypeMessage).append(Component.text(" に設定しました！").color(NamedTextColor.WHITE)));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandSender sender, AbilityManager abilityManager, String targetName) {
        if (!(sender instanceof Player player)) return 0;
        Player target = "@s".equals(targetName) ? player : Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        abilityManager.resetAbility(target);
        player.sendMessage(target.getName() + "の能力を削除しました");
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestPlayers(SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(builder::suggest);
        return builder.buildFuture();
    }
}