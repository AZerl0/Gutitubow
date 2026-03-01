package com.azerl0.gutitubow.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class LogManager {
    public static void broadcastHitLog(Entity shooter, Entity target, double distance) {

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
                                        Component.text(getDisplayName(shooter))
                                                .color(NamedTextColor.WHITE)
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" ➽ ")
                                ).append(
                                        Component.text(getDisplayName(target))
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

    public static void broadcastHitLog(Player shooter, Player target, double distance) {
        broadcastHitLog(((Entity) shooter), ((Entity) target), distance);
    }

    public static void broadcastHitLog(Player shooter, Entity target, double distance) {
        broadcastHitLog(((Entity) shooter), target, distance);
    }

    public static void broadcastHitLog(Entity shooter, Player target, double distance) {
        broadcastHitLog(shooter, ((Entity) target), distance);
    }

    public static void broadcastKillLog(Entity shooter, Entity target, double distance) {

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
                                        Component.text(getDisplayName(shooter))
                                                .color(NamedTextColor.WHITE)
                                                .decoration(TextDecoration.BOLD, false)
                                ).append(
                                        Component.text(" ➽ ")
                                ).append(
                                        Component.text(getDisplayName(target)).color(NamedTextColor.DARK_RED)
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
        broadcastKillLog(((Entity) shooter), ((Entity) target), distance);
    }

    public static void broadcastKillLog(Player shooter, Entity target, double distance) {
        broadcastKillLog(((Entity) shooter), target, distance);
    }

    public static void broadcastKillLog(Entity shooter, Player target, double distance) {
        broadcastKillLog(shooter, ((Entity) target), distance);
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
                                        Component.text(getDisplayName(player))
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


    private static String getDisplayName(Player player) {
        return player.getName();
    }

    private static String getDisplayName(Entity entity) {
        return entity.getName();
    }
}

