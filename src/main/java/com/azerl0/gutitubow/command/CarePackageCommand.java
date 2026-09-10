package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.carepackage.CarePackage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class CarePackageCommand {

    public static void register(Commands registrar) {
        registrar.register(
                literal("carepackage")
                        .requires(source -> source.getSender().hasPermission("gutitubow.carepackage"))
                        .then(literal("spawn").executes(CarePackageCommand::summon))
                        .then(literal("s").executes(CarePackageCommand::summon))
                        .then(literal("killall").executes(CarePackageCommand::killAll))
                        .build(),
                "Care package command",
                List.of("cpack", "care")
        );
    }


    private static GutitubowEntityManager getEntityManager() {
        return Gutitubow.get().getGutitubowEntityManager();
    }

    private static int summon(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getSender() instanceof Player player) {
            getEntityManager().spawnGutitubowEntity(
                    player.getLocation(),
                    CarePackage.class
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int killAll(CommandContext<CommandSourceStack> context) {
        for (GutitubowEntity gutitubowEntity : getEntityManager().getGutitubowEntities()) {
            if (gutitubowEntity instanceof CarePackage carePackage) {
                carePackage.remove();
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
