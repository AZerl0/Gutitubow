package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class ItemCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("gutitubowitem")
                        .requires(source -> source.getSender().hasPermission("gutitubow.gutitubowitem"))
                        .then(literal("gutitubow").executes(ctx -> give(ctx.getSource().getSender(), GutitubowItem.GUTITUBOW)))
                        .then(literal("gamemaster").executes(ctx -> give(ctx.getSource().getSender(), GutitubowItem.GAME_MASTER)))
                        .then(literal("recovery_potion").executes(ctx -> give(ctx.getSource().getSender(), GutitubowItem.RECOVERY_POTION)))
                        .then(literal("respawn_beacon").executes(ctx -> give(ctx.getSource().getSender(), GutitubowItem.RESPAWN_BEACON)))
                        .build(),
                "Gutitubow item command",
                java.util.List.of("gitem", "gi")
        );
    }

    private static int give(CommandSender sender, org.bukkit.inventory.ItemStack itemStack) {
        if (!(sender instanceof Player player)) return 0;
        player.give(itemStack);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
