package com.azerl0.gutitubow.arena;

import com.azerl0.gutitubow.gamemap.GameMap;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Arena {

    private final UUID id;

    private final GameMap gameMap;

    private final String worldName;

    private final World world;

    public Arena(UUID id, GameMap gameMap, World world) {
        this.id = id;
        this.gameMap = gameMap;
        this.world = world;
        this.worldName = world.getName();
    }

    public @NotNull UUID getId() {
        return id;
    }

    public @NotNull GameMap getGameMap() {
        return gameMap;
    }

    public @NotNull String getWorldName() {
        return worldName;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public WorldBorder getBorder() {
        return world.getWorldBorder();
    }
}