package com.azerl0.gutitubow.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityUtil {
    static Scoreboard glowBoard = Bukkit.getScoreboardManager().getNewScoreboard();

    public static boolean isInvalidHitEntity(Entity entity) {
        if (entity instanceof Enderman) return true;
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.CREATIVE) return true;
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) return true;
        return false;
    }

    public static void setGlow(Entity entity, NamedTextColor color) {

        String entry = entity.getScoreboardEntryName();
        String teamName = "glow_" + color.toString().toLowerCase();

        for (Player player : Bukkit.getOnlinePlayers()) {

            Scoreboard board = player.getScoreboard();

            Team team = board.getTeam(teamName);

            if (team == null) {
                team = board.registerNewTeam(teamName);
                team.color(color);
            }

            team.addEntry(entry);
        }

        entity.setGlowing(true);
    }

    public static void removeAllGlowTeam() {
        for (NamedTextColor color : NamedTextColor.NAMES.values()) {

            Team team = glowBoard.getTeam("glow_" + color.toString().toLowerCase());
            if (team != null) {
                team.unregister();
            }
        }
    }
}
