package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.data.damage.DamageSource;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePlayer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GamePlayerCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("gameplayer")
                        .requires(source -> source.getSender().hasPermission("gutitubow.gameplayer"))
                        .then(literal("data")
                                .then(literal("get").then(dataTargetArg().then(dataFieldArg(false)
                                        .executes(ctx -> dataGet(ctx.getSource().getSender(), getGame(),
                                                StringArgumentType.getString(ctx, "target"), StringArgumentType.getString(ctx, "field"))))))
                                .then(literal("set").then(dataTargetArg().then(dataFieldArg(false)
                                        .then(argument("value", IntegerArgumentType.integer()).executes(ctx -> dataSet(ctx.getSource().getSender(), getGame(),
                                                StringArgumentType.getString(ctx, "target"), StringArgumentType.getString(ctx, "field"), IntegerArgumentType.getInteger(ctx, "value")))))))
                                .then(literal("reset").then(dataTargetArg().then(dataFieldArg(true)
                                        .executes(ctx -> dataReset(ctx.getSource().getSender(), getGame(),
                                                StringArgumentType.getString(ctx, "target"), StringArgumentType.getString(ctx, "field")))))))
                        .then(literal("arrow")
                                .then(literal("set").then(dataTargetArg()
                                        .then(argument("value", IntegerArgumentType.integer()).executes(ctx -> arrowSet(ctx.getSource().getSender(), getGame(),
                                                StringArgumentType.getString(ctx, "target"), IntegerArgumentType.getInteger(ctx, "value"))))))
                                .then(literal("add").then(dataTargetArg()
                                        .executes(ctx -> arrowAdd(ctx.getSource().getSender(), getGame(), StringArgumentType.getString(ctx, "target"), 1))
                                        .then(argument("value", IntegerArgumentType.integer()).executes(ctx -> arrowAdd(ctx.getSource().getSender(), getGame(),
                                                StringArgumentType.getString(ctx, "target"), IntegerArgumentType.getInteger(ctx, "value"))))))
                                .then(literal("remove").then(dataTargetArg()
                                        .executes(ctx -> arrowRemove(ctx.getSource().getSender(), getGame(), StringArgumentType.getString(ctx, "target"), 1))
                                        .then(argument("value", IntegerArgumentType.integer()).executes(ctx -> arrowRemove(ctx.getSource().getSender(), getGame(),
                                                StringArgumentType.getString(ctx, "target"), IntegerArgumentType.getInteger(ctx, "value")))))))
                        .then(literal("damage")
                                .then(dataTargetArg()
                                        .then(argument("amount", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> damage(ctx.getSource().getSender(), getPlugin(), getGame(), StringArgumentType.getString(ctx, "target"),
                                                        DoubleArgumentType.getDouble(ctx, "amount"), "unknown", 0))
                                                .then(argument("type", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            Stream.of(GutitubowDamageType.values()).map(type -> type.name().toLowerCase(Locale.ROOT)).forEach(builder::suggest);
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> damage(ctx.getSource().getSender(), getPlugin(), getGame(), StringArgumentType.getString(ctx, "target"),
                                                                DoubleArgumentType.getDouble(ctx, "amount"), StringArgumentType.getString(ctx, "type"), 0))
                                                        .then(argument("delay", LongArgumentType.longArg(0))
                                                                .executes(ctx -> damage(ctx.getSource().getSender(), getPlugin(), getGame(), StringArgumentType.getString(ctx, "target"),
                                                                        DoubleArgumentType.getDouble(ctx, "amount"), StringArgumentType.getString(ctx, "type"),
                                                                        LongArgumentType.getLong(ctx, "delay"))))))))
                        .then(literal("revive")
                                .then(dataTargetArg()
                                        .executes(ctx -> revive(ctx.getSource().getSender(), getGame(), StringArgumentType.getString(ctx, "target"), "@s"))
                                        .then(argument("reviver", StringArgumentType.word())
                                                .suggests((ctx, builder) -> suggestPlayersAndSelf(builder))
                                                .executes(ctx -> revive(ctx.getSource().getSender(), getGame(), StringArgumentType.getString(ctx, "target"),
                                                        StringArgumentType.getString(ctx, "reviver"))))))
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

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> dataTargetArg() {
        return argument("target", StringArgumentType.word()).suggests((ctx, builder) -> suggestPlayersAndSelf(builder));
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

    private static Player resolveTarget(Player sender, String targetName) {
        if ("@s".equals(targetName)) return sender;
        return Bukkit.getPlayer(targetName);
    }

    private static int dataGet(CommandSender sender, Game game, String targetName, String field) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        GamePlayer gamePlayer = game.getGamePlayer(target);
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

    private static int dataSet(CommandSender sender, Game game, String targetName, String field, int value) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        GamePlayer gamePlayer = game.getGamePlayer(target);
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

    private static int dataReset(CommandSender sender, Game game, String targetName, String field) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        GamePlayer gamePlayer = game.getGamePlayer(target);
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
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int arrowSet(CommandSender sender, Game game, String targetName, int value) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        game.getGamePlayer(target).setArrowAmount(value);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int arrowAdd(CommandSender sender, Game game, String targetName, int value) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        game.getGamePlayer(target).addArrow(value);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int arrowRemove(CommandSender sender, Game game, String targetName, int value) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        game.getGamePlayer(target).removeArrow(value);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int damage(CommandSender sender, JavaPlugin plugin, Game game, String targetName, double amount, String damageTypeString, long delay) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        GutitubowDamageType damageType = GutitubowDamageType.valueOf(damageTypeString.toUpperCase(Locale.ROOT));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DamageSource damageSource = DamageSource.builder().damageType(damageType).build();
            game.getGamePlayer(target).damage(target, amount, damageSource);
        }, delay * 20);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int revive(CommandSender sender, Game game, String targetName, String reviverName) {
        Player player = senderPlayer(sender);
        if (player == null) return 0;
        Player target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        Player reviver = resolveTarget(player, reviverName);
        if (reviver == null) {
            player.sendMessage("プレイヤーが見つかりませんでした");
            return 0;
        }
        game.getGamePlayer(target).revive(reviver);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
