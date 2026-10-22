package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.jumppad.JumpPad;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class JumpPadCommand {
    public static void register(Commands registrar) {
        registrar.register(
                literal("jumppad")
                        .requires(source -> source.getSender().hasPermission("gutitubow.zipline"))
                        .then(literal("create")
                                .then(argument("size", DoubleArgumentType.doubleArg(0.0))
                                        .then(argument("yaw", FloatArgumentType.floatArg())
                                                .then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
                                                        .then(argument("power", FloatArgumentType.floatArg(0.0f))
                                                                .executes(JumpPadCommand::create))))))
                        .then(literal("removenear")
                                .then(argument("radius", DoubleArgumentType.doubleArg(0))
                                        .executes(JumpPadCommand::removenear)))
                        .build(),
                "Zipline command",
                List.of("pad", "jp")
        );
    }

    private static GutitubowEntityManager getEntityManager() {
        return Gutitubow.get().getGutitubowEntityManager();
    }

    private static JumpPad createNode(@NotNull Location location) {
        return getEntityManager().spawnGutitubowEntity(location, JumpPad.class);
    }

    private static int create(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Location location = context.getSource().getLocation();

        double size = context.getArgument("size", Double.class);
        float yaw = context.getArgument("yaw", Float.class);
        float pitch = context.getArgument("pitch", Float.class);
        float power = context.getArgument("power", Float.class);

        JumpPad jumpPad = createNode(location);
        jumpPad.setSize(size);
        jumpPad.setYaw(yaw);
        jumpPad.setPitch(pitch);
        jumpPad.setPower(power);

        return Command.SINGLE_SUCCESS;
    }

    private static int removenear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        double r = context.getArgument("radius", Double.class);

        Location location = context.getSource().getLocation();
        for (GutitubowEntity gutitubowEntity : getEntityManager().getGutitubowEntities()) {
            if (gutitubowEntity instanceof JumpPad pad) {
                if (pad.getLocation().getWorld().equals(location.getWorld())) {
                    double distance = pad.getLocation().distance(location);
                    if (distance <= r) {
                        pad.remove();
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
