package com.azerl0.gutitubow.config;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.gamemap.GameMap;
import com.azerl0.gutitubow.gamemap.GameMapManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Gutitubow gutitubow;
    private FileConfiguration config;
    private Config defaultConfig;

    public ConfigManager(Gutitubow gutitubow) {
        this.gutitubow = gutitubow;
    }

    public void processConfigs() {
        config = gutitubow.getConfig();
        defaultConfig = new Config(config);
        loadMaps(Gutitubow.get().getGameMapManager());
    }

    public void reload() {
        gutitubow.reloadConfig();
        processConfigs();
    }

    public Config getDefaultConfig() {
        return defaultConfig;
    }

    public void loadMaps(GameMapManager gameMapManager) {

        gameMapManager.clear();

        ConfigurationSection maps = config.getConfigurationSection("maps");
        if (maps == null) return;

        for (String id : maps.getKeys(false)) {

            ConfigurationSection section = maps.getConfigurationSection(id);
            if (section == null) continue;

            Component name = Component.text(id);
            String legacy = section.getString("name");
            if (legacy != null) name = LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
            Location center = section.getLocation("center");
            double size = section.getDouble("size");
            double phase1size = section.getDouble("phase1size");
            double phase2size = section.getDouble("phase2size");
            double phase3size = section.getDouble("phase3size");
            double phase4size = section.getDouble("phase4size");
            double phase5size = section.getDouble("phase5size");

            GameMap gameMap = new GameMap(
                    id,
                    name,
                    center,
                    size,
                    phase1size,
                    phase2size,
                    phase3size,
                    phase4size,
                    phase5size
            );

            gameMapManager.addMap(gameMap);
        }
    }

    public void saveMaps(GameMapManager gameMapManager) {

        config.set("maps", null);

        for (GameMap gameMap : gameMapManager.getGameMaps()) {

            String path = "maps." + gameMap.getId();

            config.set(path + ".name", LegacyComponentSerializer.legacyAmpersand().serialize(gameMap.getName()));
            config.set(path + ".center", gameMap.getCenter());
            config.set(path + ".size", gameMap.getSize());
            config.set(path + ".phase1size", gameMap.getPhase1Size());
            config.set(path + ".phase2size", gameMap.getPhase2Size());
            config.set(path + ".phase3size", gameMap.getPhase3Size());
            config.set(path + ".phase4size", gameMap.getPhase4Size());
            config.set(path + ".phase5size", gameMap.getPhase5Size());
        }

        gutitubow.saveConfig();
    }
}
