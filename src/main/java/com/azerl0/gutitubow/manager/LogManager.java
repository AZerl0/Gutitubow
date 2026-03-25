package com.azerl0.gutitubow.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class LogManager {
    public static void broadcastHitLog(Player shooter, Player target, double distance) {

        Component message =
                Component.text("")
                        .append(Component.text("[")
                                .color(NamedTextColor.WHITE)
                                .decorate(TextDecoration.BOLD)
                                .append(
                                        Component.text("HIT")
                                                .color(NamedTextColor.RED)
                                ).append(
                                        Component.text("] ")
                                                .color(NamedTextColor.WHITE)
                                ).append(
                                        Component.text(shooter.getName())
                                                .color(NamedTextColor.WHITE)
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" ➽ ")
                                ).append(
                                        Component.text(target.getName())
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" (" + Math.floor(distance * 10) / 10 + "m)")
                                                .color(NamedTextColor.GRAY)
                                                .decoration(TextDecoration.BOLD, false)
                                ));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }

    }

    public static void broadcastKillLog(Player shooter, Player target, double distance) {

        Component message =
                Component.text("")
                        .append(Component.text("[")
                                .color(NamedTextColor.WHITE)
                                .decorate(TextDecoration.BOLD)
                                .append(
                                        Component.text("KILL")
                                                .color(NamedTextColor.DARK_RED)
                                ).append(
                                        Component.text("] ")
                                                .color(NamedTextColor.WHITE)
                                ).append(
                                        Component.text(shooter.getName())
                                                .color(NamedTextColor.WHITE)
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" ➽ ")
                                ).append(
                                        Component.text(target.getName()).color(NamedTextColor.DARK_RED)
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" (" + Math.floor(distance * 10) / 10 + "m)")
                                                .color(NamedTextColor.GRAY)
                                                .decoration(TextDecoration.BOLD, false)
                                ));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }

    }

    public static void broadcastDeathLog(Player player) {

        Component message =
                Component.text("")
                        .append(Component.text("[")
                                .color(NamedTextColor.WHITE)
                                .decorate(TextDecoration.BOLD)
                                .append(
                                        Component.text("DEATH")
                                                .color(NamedTextColor.DARK_RED)
                                ).append(
                                        Component.text("] ")
                                                .color(NamedTextColor.WHITE)
                                ).append(
                                        Component.text(player.getName())
                                                .color(NamedTextColor.WHITE)
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" (エリア外)")
                                                .color(NamedTextColor.GRAY)
                                ));

        for (Player toPlayer : Bukkit.getOnlinePlayers()) {
            toPlayer.sendMessage(message);
        }
    }

    public static void broadcastReviveLog(Player player, Player reviver) {
        Component message =
                Component.text("§f[§aREVIVE§f] " + reviver.getName() + " §2≫≫≫≫ §a" + player.getName());
        for (Player toPlayer : Bukkit.getOnlinePlayers()) {
            toPlayer.sendMessage(message);
        }
    }
}

