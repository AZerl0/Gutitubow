package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.command.argument.GameDamageTypeArgument;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GamePlayerCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("gameplayer")
                        .requires(source -> source.getSender().hasPermission("gutitubow.gameplayer"))
                        .then(literal("data")
                                .then(literal("get").then(argument("target", ArgumentTypes.player()).then(dataFieldArg(false)
                                        .executes(GamePlayerCommand::dataGet))))
                                .then(literal("set").then(argument("target", ArgumentTypes.player()).then(dataFieldArg(false)
                                        .then(argument("value", IntegerArgumentType.integer()).executes(GamePlayerCommand::dataSet)))))
                                .then(literal("reset").then(argument("target", ArgumentTypes.player()).then(dataFieldArg(true)
                                        .executes(GamePlayerCommand::dataReset)))))
                        .then(literal("arrow")
                                .then(literal("set").then(argument("target", ArgumentTypes.player())
                                        .then(argument("value", IntegerArgumentType.integer(0)).executes(GamePlayerCommand::arrowSet))))
                                .then(literal("add").then(argument("target", ArgumentTypes.player())
                                        .executes(GamePlayerCommand::arrowAdd)
                                        .then(argument("value", IntegerArgumentType.integer()).executes(GamePlayerCommand::arrowAdd))))
                                .then(literal("remove").then(argument("target", ArgumentTypes.player())
                                        .executes(GamePlayerCommand::arrowRemove)
                                        .then(argument("value", IntegerArgumentType.integer()).executes(GamePlayerCommand::arrowRemove)))))
                        .then(literal("damage")
                                .then(argument("target", ArgumentTypes.player())
                                        .then(argument("amount", DoubleArgumentType.doubleArg(0))
                                                .executes(GamePlayerCommand::damage)
                                                .then(argument("damageType", new GameDamageTypeArgument())
                                                        .executes(GamePlayerCommand::damage)
                                                        .then(argument("delay", LongArgumentType.longArg(0))
                                                                .executes(GamePlayerCommand::damage)
                                                                .then(argument("attacker", ArgumentTypes.players())
                                                                        .executes(GamePlayerCommand::damage))
                                                        )))))
                        .then(literal("revive")
                                .then(argument("target", ArgumentTypes.player())
                                        .executes(GamePlayerCommand::revive)
                                        .then(argument("reviver", ArgumentTypes.player())
                                                .suggests((ctx, builder) -> suggestPlayersAndSelf(builder))
                                                .executes(GamePlayerCommand::revive))))
                        .build(),
                "Gutitubow game player command",
                List.of("gplayer", "gp")
        );
    }

    private static JavaPlugin getPlugin() {
        return Gutitubow.get();
    }

    private static Game getGame() {
        return Gutitubow.get().getGame();
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> dataFieldArg(boolean includeAll) {
        return argument("field", StringArgumentType.word()).suggests((ctx, builder) -> {
            if (includeAll) builder.suggest("*");
            builder.suggest("playerTotalHit");
            builder.suggest("playerArrowReloadTick");
            builder.suggest("playerMaxArrowStackSize");
            return builder.buildFuture();
        });
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPlayersAndSelf(com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(builder::suggest);
        builder.suggest("@s");
        return builder.buildFuture();
    }

    private static Player senderPlayer(CommandSender sender) {
        return sender instanceof Player player ? player : null;
    }

    private static int dataGet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        String field = StringArgumentType.getString(context, "field");

        Player player = senderPlayer(sender);
        if (player == null) return 0;
        GamePlayer gamePlayer = getGame().getGamePlayer(target);
        return switch (field) {
            case "playerTotalHit" -> {
                player.sendMessage(target.getName() + "の" + field + "は" + gamePlayer.getTotalHit() + "です");
                yield com.mojang.brigadier.Command.SINGLE_SUCCESS;
            }
            case "playerArrowReloadTick" -> {
                player.sendMessage(target.getName() + "の" + field + "は" + gamePlayer.getArrowReloadTick() + "です");
                yield com.mojang.brigadier.Command.SINGLE_SUCCESS;
            }
            case "playerMaxArrowStackSize" -> {
                player.sendMessage(target.getName() + "の" + field + "は" + gamePlayer.getMaxArrowStackSize() + "です");
                yield com.mojang.brigadier.Command.SINGLE_SUCCESS;
            }
            default -> 0;
        };
    }

    private static int dataSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        String field = StringArgumentType.getString(context, "field");
        int value = IntegerArgumentType.getInteger(context, "value");

        Player player = senderPlayer(sender);
        if (player == null) return 0;
        GamePlayer gamePlayer = getGame().getGamePlayer(target);
        switch (field) {
            case "playerTotalHit" -> gamePlayer.setTotalHit(value);
            case "playerArrowReloadTick" -> gamePlayer.setArrowReloadTick(value);
            case "playerMaxArrowStackSize" -> gamePlayer.setMaxArrowStackSize(value);
            default -> {
                return 0;
            }
        }
        player.sendMessage(target.getName() + "の" + field + "を" + value + "に設定しました");
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int dataReset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        String field = StringArgumentType.getString(context, "field");

        Player player = senderPlayer(sender);
        if (player == null) return 0;

        GamePlayer gamePlayer = getGame().getGamePlayer(target);
        switch (field) {
            case "*" -> {
                gamePlayer.resetTotalHit();
                gamePlayer.resetArrowReloadTick();
                gamePlayer.resetMaxArrowStackSize();
                player.sendMessage(target.getName() + "のデータをリセットしました");
            }
            case "playerTotalHit" -> {
                gamePlayer.resetTotalHit();
                player.sendMessage(target.getName() + "の" + field + "を" + gamePlayer.getTotalHit() + "にリセットしました");
            }
            case "playerArrowReloadTick" -> {
                gamePlayer.resetArrowReloadTick();
                player.sendMessage(target.getName() + "の" + field + "を" + gamePlayer.getArrowReloadTick() + "にリセットしました");
            }
            case "playerMaxArrowStackSize" -> {
                gamePlayer.resetMaxArrowStackSize();
                player.sendMessage(target.getName() + "の" + field + "を" + gamePlayer.getMaxArrowStackSize() + "にリセットしました");
            }
            default -> {
                return 0;
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int arrowSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        int value = IntegerArgumentType.getInteger(context, "value");

        Player player = senderPlayer(sender);
        if (player == null) return 0;
        getGame().getGamePlayer(target).setArrowAmount(value);
        return Command.SINGLE_SUCCESS;
    }

    private static int arrowAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        int value = IntegerArgumentType.getInteger(context, "value");

        Player player = senderPlayer(sender);
        if (player == null) return 0;
        getGame().getGamePlayer(target).addArrow(value);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int arrowRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        int value = IntegerArgumentType.getInteger(context, "value");

        Player player = senderPlayer(sender);
        if (player == null) return 0;

        getGame().getGamePlayer(target).removeArrow(value);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int damage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        double amount = DoubleArgumentType.getDouble(context, "amount");
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        Player attacker;
        GutitubowDamageType damageType;
        try {
            damageType = context.getArgument("damageType", GutitubowDamageType.class);
        } catch (Exception e) {
            damageType = GutitubowDamageType.UNKNOWN;
        }
        long delay;
        try {
            delay = LongArgumentType.getLong(context, "delay");
        } catch (Exception e) {
            delay = 0;
        }
        try {
            PlayerSelectorArgumentResolver attackerResolver = context.getArgument("attacker", PlayerSelectorArgumentResolver.class);
             attacker = attackerResolver.resolve(context.getSource()).getFirst();
        } catch (Exception e) {
            attacker = null;
        }


        Player player = senderPlayer(sender);
        if (player == null) return 0;
        GutitubowDamageType finalDamageType = damageType;
        Player finalAttacker = attacker;
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            DamageSource damageSource = DamageSource.builder(finalDamageType).directPlayer(finalAttacker).build();
            getGame().getGamePlayer(target).damage(damageSource, amount);
        }, delay * 20);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int revive(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(context.getSource()).getFirst();
        final PlayerSelectorArgumentResolver reviverResolver = context.getArgument("reviver", PlayerSelectorArgumentResolver.class);
        final Player reviver = reviverResolver.resolve(context.getSource()).getFirst();

        CommandSender sender = context.getSource().getSender();
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        getGame().getGamePlayer(target).revive(reviver);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
