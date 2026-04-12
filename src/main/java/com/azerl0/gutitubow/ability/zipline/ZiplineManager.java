package com.azerl0.gutitubow.ability.zipline;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ZiplineManager {
    private final JavaPlugin plugin;
    private final Set<Zipline> ziplines = new HashSet<>();

    public ZiplineManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Zipline createZipline(Location point1, Location point2) {
        Zipline zipline = new Zipline(plugin, this, point1, point2);
        ziplines.add(zipline);
        return zipline;
    }

    public void removeZipline(@NotNull Zipline zipline) {
        zipline.remove();
        ziplines.remove(zipline);
    }

    public void removeAllZiplines() {
        for (Zipline zipline : new HashSet<>(ziplines)) {
            zipline.remove();
        }
        ziplines.clear();
    }

    public @Nullable Zipline getZipline(Entity entity) {
        for (Zipline zipline : ziplines) {
            if (zipline.isThisZiplineEntity(entity)) return zipline;
        }
        return null;
    }
}
