package com.azerl0.gutitubow.gamemap;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class GameMap {
    private final String id;
    private final Component name;
    private final Location center;
    private @NotNull Location lobby;
    private final double size;

    private final double phase1Size;
    private final double phase2Size;
    private final double phase3Size;
    private final double phase4Size;
    private final double phase5Size;

    public GameMap(
            String id,
            Component name,
            @NotNull Location center,
            @NotNull Location lobby,
            double size,
            double phase1Size,
            double phase2Size,
            double phase3Size,
            double phase4Size,
            double phase5Size
    ) {
        this.id = id;
        this.name = name;
        this.center = center;
        this.lobby = lobby;
        this.size = size;
        this.phase1Size = phase1Size;
        this.phase2Size = phase2Size;
        this.phase3Size = phase3Size;
        this.phase4Size = phase4Size;
        this.phase5Size = phase5Size;

    }

    public String getId() {
        return id;
    }

    public Component getName() {
        return name;
    }

    public Location getCenter() {
        return this.center.clone();
    }

    public @NotNull Location getLobby() {
        return lobby.clone();
    }

    public void setLobby(@NotNull Location lobby) {
        this.lobby = lobby;
    }

    public WorldBorder getBorder() {
        return this.center.getWorld().getWorldBorder();
    }

    public double getSize() {
        return this.size;
    }

    public @NotNull Location getRandomSafeArea() {
        Random random = new Random();
        Location area = this.center.clone();
        double sizeOffset = size / 2 - 75;
        area.setX(center.clone().getX() + random.nextDouble(-sizeOffset, sizeOffset));
        area.setZ(center.clone().getZ() + random.nextDouble(-sizeOffset, sizeOffset));
        return area;
    }

    public double getPhase1Size() {
        return phase1Size;
    }

    public double getPhase2Size() {
        return phase2Size;
    }

    public double getPhase3Size() {
        return phase3Size;
    }

    public double getPhase4Size() {
        return phase4Size;
    }

    public double getPhase5Size() {
        return phase5Size;
    }
}
