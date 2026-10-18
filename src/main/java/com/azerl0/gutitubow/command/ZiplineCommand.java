package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.zipline.ZiplineNode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ZiplineCommand {
    public static void register(Commands registrar) {
        registrar.register(
                literal("zipline")
                        .requires(source -> source.getSender().hasPermission("gutitubow.zipline"))
                        .then(literal("create")
                                .then(argument("startPosition", ArgumentTypes.blockPosition())
                                        .then(argument("endPosition", ArgumentTypes.blockPosition())
                                                .executes(ZiplineCommand::create)
                                                .then(argument("life", IntegerArgumentType.integer(-1))
                                                        .executes(ZiplineCommand::create)))))
                        .then(literal("removenear")
                                .then(argument("radius", DoubleArgumentType.doubleArg(0))
                                        .executes(ZiplineCommand::removenear)))
                        .build(),
                "Zipline command",
                List.of("zip", "zl")
        );
    }

    private static GutitubowEntityManager getEntityManager() {
        return Gutitubow.get().getGutitubowEntityManager();
    }

    private static ZiplineNode createNode(@NotNull Location location) {
        return getEntityManager().spawnGutitubowEntity(location, ZiplineNode.class);
    }

    private static int create(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final BlockPositionResolver startPosResolver = context.getArgument("startPosition", BlockPositionResolver.class);
        final BlockPosition startPos = startPosResolver.resolve(context.getSource());

        final BlockPositionResolver endPosResolver = context.getArgument("endPosition", BlockPositionResolver.class);
        final BlockPosition endPos = endPosResolver.resolve(context.getSource());

        int life;
        try {
            life = context.getArgument("life", Integer.class);
        } catch (Exception e) {
            life = 120 * 20;
        }

        World world = context.getSource().getLocation().getWorld();
        ZiplineNode startNode = createNode(startPos.toLocation(world));
        ZiplineNode endNode = createNode(endPos.toLocation(world));
        startNode.addConnectionNode(endNode);
        startNode.setLife(life);
        endNode.addConnectionNode(startNode);
        endNode.setLife(life);

        return Command.SINGLE_SUCCESS;
    }

    private static int removenear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        double r = context.getArgument("radius", Double.class);

        Location location = context.getSource().getLocation();
        for (GutitubowEntity gutitubowEntity : getEntityManager().getGutitubowEntities()) {
            if (gutitubowEntity instanceof ZiplineNode node) {
                if (node.getLocation().getWorld().equals(location.getWorld())) {
                    double distance = node.getLocation().distance(location);
                    if (distance <= r) {
                        node.remove();
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
