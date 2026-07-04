package com.azerl0.gutitubow.packet;

import com.azerl0.gutitubow.scoreboard.ScoreboardManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class EntityGlowManager {
    private final EntityFlagManager flagManager;
    private final ScoreboardManager scoreboardManager;

    public EntityGlowManager(EntityFlagManager flagManager, ScoreboardManager scoreboardManager) {
        this.flagManager = flagManager;
        this.scoreboardManager = scoreboardManager;
    }

    public void addGlow(@NotNull Player viewer,@NotNull Entity target) {
        flagManager.addFlag(viewer, target, (byte) 0x40);
    }

    public void removeGlow(@NotNull Player viewer,@NotNull Entity target) {
        flagManager.removeFlag(viewer, target, (byte) 0x40);
    }

    public void setGlowColor(@NotNull Player viewer, @NotNull Entity entity, @NotNull NamedTextColor color) {
        Scoreboard scoreboard = scoreboardManager.getScoreboard(viewer);
        Team team = scoreboard.getEntityTeam(entity);
        if (team == null) {
            team = scoreboard.registerNewTeam(entity.getScoreboardEntryName());
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.addEntity(entity);
        }
        team.color(color);
    }
}
