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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class AbilityCommand implements CommandExecutor, TabExecutor {
    AbilityManager abilityManager;
    AbilitySelectorGUI abilitySelectorGUI;

    public AbilityCommand(AbilityManager abilityManager, AbilitySelectorGUI abilitySelectorGUI) {
        this.abilityManager = abilityManager;
        this.abilitySelectorGUI = abilitySelectorGUI;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) return true;

        switch (args[0]) {
            case ("get"):
                Player getPlayer;
                if (args.length == 1) {
                    getPlayer = player;
                } else {
                    getPlayer = Bukkit.getPlayer(args[1]);
                }

                if (getPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {
                    Ability ability = abilityManager.getAbility(getPlayer);
                    if (ability == null) {
                        player.sendMessage(getPlayer.getName() + "は能力を持っていません");
                    } else {
                        player.sendMessage(Component.text(getPlayer.getName() + "の能力は").append(ability.getType().getName()).append(Component.text("です")));
                    }

                }
                return true;


            case ("list"):
                player.sendMessage(Component.text("Abilities (" + AbilityRegistry.getSize() + "): ").color(NamedTextColor.AQUA));

                for (AbilityType type : AbilityRegistry.getTypes()) {
                    player.sendMessage(Component.text(" - ").append(type.getName()).append(Component.text(" " + type.toString().toLowerCase())));
                }

                return true;

            case ("gui"):
                abilitySelectorGUI.open(player);
                return true;

            case ("set"):

                String id = args[1];
                Player setPlayer;
                if (args.length == 2) {
                    setPlayer = player;
                } else {
                    setPlayer = Bukkit.getPlayer(args[2]);
                }

                if (id == null) {
                    player.sendMessage("能力を指定する必要があります");
                    return true;
                }

                AbilityType type = AbilityRegistry.getType(id);

                if (type == null) {
                    player.sendMessage("その能力は存在しません");
                    return true;
                }

                if (setPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {

                    abilityManager.setAbility(setPlayer, type);
                    Component abilityTypeMessage = type.getName().color(NamedTextColor.GOLD).hoverEvent(type.getDisplayItem().asHoverEvent());
                    player.sendMessage(Component.text(setPlayer.getName() + "の能力を ").append(abilityTypeMessage).append(Component.text(" に設定しました！").color(NamedTextColor.WHITE)));
                }
                return true;

            case ("reset"):
                Player resetPlayer;
                if (args.length == 1) {
                     resetPlayer = player;
                } else {
                     resetPlayer = Bukkit.getPlayer(args[1]);
                }

                if (resetPlayer == null) {
                    player.sendMessage("プレイヤーが見つかりませんでした");
                } else {
                    abilityManager.resetAbility(resetPlayer);
                    player.sendMessage(resetPlayer.getName() + "の能力を削除しました");
                }
                return true;


        }

        return false;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args
    ) {
        if (args.length == 1) {
            return List.of("list", "get", "set", "reset", "gui");
        }


        switch (args[0]) {
            case ("get"):
            case ("reset"):
                if (args.length == 2) {
                    return Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList();
                }
                break;

            case ("set"):
                if (args.length == 2) {
                    List<AbilityType> types = AbilityRegistry.getTypes().stream().toList();
                    List<String> typesList = new ArrayList<>();
                    for (AbilityType type : types) {
                        typesList.add(type.toString().toLowerCase());
                    }
                    return typesList;
                } else if (args.length == 3) {
                    return Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList();
                }
                break;
        }
        return Collections.emptyList();
    }

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