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
    public static boolean isInvalidHitEntity(Entity entity) {
        if (entity instanceof Enderman) return true;
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.CREATIVE) return true;
        return false;
    }
    public static void setGlow(Entity entity, NamedTextColor color) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        String teamName = "glow_" + color.toString().toLowerCase();
        Team team = board.getTeam(teamName);

        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.color(color);
        }

        team.addEntry(entity.getUniqueId().toString());
        entity.setGlowing(true);
    }
}
