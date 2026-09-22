package com.azerl0.gutitubow.arena;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.gamemap.GameMap;
import com.azerl0.gutitubow.util.WorldFileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaManager {

    private final Gutitubow gutitubow;

    private final Map<UUID, Arena> arenas = new HashMap<>();

    public ArenaManager(Gutitubow gutitubow) {
        this.gutitubow = gutitubow;
    }

    public @NotNull Arena create(@NotNull GameMap gameMap) throws IOException {

        String worldName = gameMap.getId() + "_" + UUID.randomUUID();

        Path template = gameMap.getCenter().getWorld().getWorldPath();

        if (!Files.exists(template)) {
            throw new IllegalArgumentException(
                    "Template world does not exist: " + gameMap.getId());
        }

        Path target = Path.of(worldName);

        if (Files.exists(target)) {
            WorldFileUtil.deleteDirectory(target);
        }

        WorldFileUtil.copyDirectory(template, target);


        World world = Bukkit.createWorld(new WorldCreator(worldName));

        if (world == null) {
            throw new IllegalStateException(
                    "Failed to load world: " + worldName);
        }

        Arena arena = new Arena(
                UUID.randomUUID(),
                gameMap,
                world
        );


        arenas.put(arena.getId(), arena);

        return arena;
    }

    public void unload(Arena arena) {
        World world = Bukkit.getWorld(arena.getWorldName());
        if (world != null) {
            for (Player player : world.getPlayers()) {
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            }
            Bukkit.unloadWorld(world, false);
        }
    }


    public void destroy(@Nullable Arena arena) {
        if (arena == null) return;
        unload(arena);
        try {
            deleteWorld(arena.getWorldName());
        } catch (IOException e) {
            Bukkit.getServer().getLogger().warning("failed delete world. worldName: " + arena.getWorldName());
        }
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Arena getArena(UUID id) {
        return arenas.get(id);
    }


    private void deleteWorld(String worldName) throws IOException {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Location safe = Bukkit.getWorlds().getFirst().getSpawnLocation();
            for (Player player : world.getPlayers()) {
                player.teleport(safe);
            }
        }
        File worldContainer = Bukkit.getWorldContainer();
        Path worldPath = worldContainer.toPath().resolve(worldName);
        WorldFileUtil.deleteDirectory(worldPath);
    }

    public boolean isArenaWorld(World world) {
        for (Arena arena : arenas.values()) {
            if (world == arena.getWorld()) {
                return true;
            }
        }
        return false;
    }
}