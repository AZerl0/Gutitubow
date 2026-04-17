package com.azerl0.gutitubow.command;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.carepackage.CarePackageManager;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class CarePackageCommand implements CommandExecutor, TabExecutor {
    private final CarePackageManager carePackageManager;

    public CarePackageCommand(CarePackageManager cpm) {
        carePackageManager = cpm;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!(sender instanceof Player player)) return true;


        switch (args[0]) {
            case ("s"):
            case ("spawn"):
                carePackageManager.summonPackage(player.getLocation());
                break;
            case ("killall"):
                carePackageManager.removeAllCarePackage();
                break;

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (args.length == 1) {
            return List.of("spawn", "killall");
        }
        return List.of();
    }

    public static void register(Commands registrar) {
        registrar.register(
                literal("carepackage")
                        .requires(source -> source.getSender().hasPermission("gutitubow.carepackage"))
                        .then(literal("spawn").executes(ctx -> summon(ctx.getSource().getSender(), getManager())))
                        .then(literal("s").executes(ctx -> summon(ctx.getSource().getSender(), getManager())))
                        .then(literal("killall").executes(ctx -> killAll(ctx.getSource().getSender(), getManager())))
                        .build(),
                "Care package command",
                List.of("cpack", "care")
        );
    }

    private static CarePackageManager getManager() {
        return Gutitubow.get().getGame().getCarePackageManager();
    }

    private static int summon(CommandSender sender, CarePackageManager carePackageManager) {
        if (!(sender instanceof Player player)) return 0;
        carePackageManager.summonPackage(player.getLocation());
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int killAll(CommandSender sender, CarePackageManager carePackageManager) {
        if (!(sender instanceof Player)) return 0;
        carePackageManager.removeAllCarePackage();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
