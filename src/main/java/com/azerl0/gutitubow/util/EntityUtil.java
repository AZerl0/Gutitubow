package com.azerl0.gutitubow.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class EntityUtil {
    static Scoreboard glowBoard = Bukkit.getScoreboardManager().getMainScoreboard();

    public static boolean isInvalidHitEntity(Entity entity) {
        if (entity instanceof Enderman) return true;
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.CREATIVE) return true;
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) return true;
        return false;
    }
    public static void setGlow(Entity entity, NamedTextColor color) {

        String teamName = "glow_" + color.toString().toLowerCase();
        Team team = glowBoard.getTeam(teamName);

        if (team == null) {
            team = glowBoard.registerNewTeam(teamName);
            team.color(color);
        }

        team.addEntry(entity.getUniqueId().toString());
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
