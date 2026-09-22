package com.azerl0.gutitubow.gamemap;

import com.azerl0.gutitubow.Gutitubow;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class GameMapManager {
    private final Set<GameMap> gameGameMaps = new HashSet<>();

    public Set<GameMap> getGameMaps() {
        return gameGameMaps;
    }

    public boolean containsMapId(String mapId) {
        for (GameMap gameMap : gameGameMaps) {
            if (mapId.equals(gameMap.getId())) {
                return true;
            }
        }
        return false;
    }

    public @Nullable GameMap getMap(String mapId) {
        for (GameMap gameMap : gameGameMaps) {
            if (mapId.equals(gameMap.getId())) {
                return gameMap;
            }
        }
        return null;
    }

    public @Nullable GameMap createMap(String mapId, Location center) {
        if (containsMapId(mapId)) {
            return null;
        } else {
            GameMap gameMap = new GameMap(mapId, Component.text(mapId), center, center, 300, 300, 150, 105, 63, 31);
            gameGameMaps.add(gameMap);
            saveMaps();
            return gameMap;
        }
    }

    public void deleteMap(String mapId) {
        for (GameMap gameMap : new HashSet<>(gameGameMaps)) {
            if (mapId.equals(gameMap.getId())) {
                saveMaps();
                gameGameMaps.remove(gameMap);
            }
        }
    }

    public void clear() {
        gameGameMaps.clear();
    }

    public boolean addMap(GameMap gameMap) {
        if (containsMapId(gameMap.getId())) {
            return false;
        } else {
            gameGameMaps.add(gameMap);
            return true;
        }
    }

    public void saveMaps() {
        Gutitubow.get().getConfigManager().saveMaps(this);
    }
}
